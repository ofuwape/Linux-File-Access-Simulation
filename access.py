import re
import sys
import os
import subprocess

class FileComponents:
    filename = ''
    owner = ''
    group = ''
    permissions = [ 'r', 'w', '-', '-', '-', '-', '-', '-', '-' ]

    def __init__(self, nfilename, nowner, ngroup):
        self.filename = nfilename
        self.owner = nowner
        self.group = ngroup 

    def getOwner(self):
        return self.owner
    

    def getFileName(self):
        return self.filename
    
    def getGroup(self):
        return self.group


    def getPermission(self):
        return self.permissions

    def setGroup(self, groupName):
        self.group = groupName 

    def setOwner(self, owner):
        self.owner = owner

    def setPermission(self, newPerm):
        self.permissions = newPerm

# global variables
global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
newProgram = True
accounts = {};
groups = {};
files = [];

userLoggedIn = ''
isAUserLoggedIn = False
oldFiles = []

def useradd(userName, password):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if newProgram and userName == ('root'):
        addToAccounts(userName, password)
    elif not(userLoggedIn == ('root')):
        outputAndLog('Error: only root may issue useradd command')
    else:
        addToAccounts(userName, password)

def login(userName, password):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if (isAUserLoggedIn):
        outputAndLog('Login Failed: ' + userLoggedIn + ' is already logged in')
    elif (checkUsername(userName) and checkPassowrd(password, userName)):
        outputAndLog('User ' + userName + ' logged in')
        userLoggedIn = userName
        isAUserLoggedIn = True
    else:
        outputAndLog('Login failed: invalid username or password')

def logout():
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(isAUserLoggedIn):
        outputAndLog('Error: A User must be logged in')
    else:
        isAUserLoggedIn = False
        outputAndLog('User ' + userLoggedIn + ' logged out')
        userLoggedIn = ''

def groupadd(groupName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(isAUserLoggedIn):
        outputAndLog('Error: A User must be logged In')
    elif not(userLoggedIn == ('root')):
        outputAndLog('Error: only root may issue groupadd command')
    elif (groupName == ('nil')):
        outputAndLog('Error: nil is not a valid group name')
    else:
        addGroup(groupName)

def usergrp(userName, groupName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(isAUserLoggedIn):
        outputAndLog('Error: A User must be logged in')
    elif not(userLoggedIn == ('root')):
        outputAndLog('Error: The root user is not logged in')
    elif (groupName == ('nil')):
        outputAndLog('Error: nil is not a valid group name')
    else:
        addUserToGroup(groupName, userName) 

def  mkfile(fileName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(isAUserLoggedIn):
        outputAndLog('Error: A User must be logged In')
    elif (isAdminFile(fileName)):
        outputAndLog('Error: Users cannot modify admin files')
    else:
        makeFile(fileName, userLoggedIn)

def chmod(fileName, permissions):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(isAUserLoggedIn):
        outputAndLog('Error with chmod: A User must be logged In')
    elif not(fileExists(fileName)):
        outputAndLog('Error with chmod: ' + fileName + ' does not exist')
    elif (not(isOwner(fileName, userLoggedIn)) and not(userLoggedIn == ('root'))):
        outputAndLog('Error with chmod: Current user is not the owner and not root')
    elif (isAdminFile(fileName)):
        outputAndLog('Error with chmod: Users cannot modify admin files')
    else:
        changePermissions(fileName, permissions)

def   chown(fileName, userName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(isAUserLoggedIn):
        outputAndLog('Error with chown: A User must be logged In')
    elif not(fileExists(fileName)):
        outputAndLog('Error with chown: ' + fileName + ' does not exist')
    elif (not(isOwner(fileName, userLoggedIn))and not(userLoggedIn == ('root'))):
        outputAndLog('Error with chown: Current user is not the owner and not root')
    elif (isAdminFile(fileName)):
        outputAndLog('Error with chown: Users cannot modify admin files')
    elif not(fileExists(fileName)):
        outputAndLog('Error with read: ' + fileName + ' does not exist')
    else:
        changeOwner(userName, fileName)

def   chgrp(fileName, groupName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(isAUserLoggedIn):
        outputAndLog('Error with chgrp: A User must be logged In')
    elif not(fileExists(fileName)):
        outputAndLog('Error with chgrp: ' + fileName + ' does not exist')
    elif (not(isOwner(fileName, userLoggedIn))and not(userLoggedIn == ('root'))):
        outputAndLog('Error with chgrp: Current user is not the owner and not root')
    elif (isAdminFile(fileName)):
        outputAndLog('Error with chgrp: Users cannot modify admin files')
    elif not(checkGroup(groupName)):
        outputAndLog('Error with chgrp: Group ' + groupName + ' does not exist')
    else:
        changeGroup(userLoggedIn, groupName, fileName)

def   read(fileName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(isAUserLoggedIn):
        outputAndLog('Error with read: A User must be logged In')
    elif (isAdminFile(fileName)):
        outputAndLog('Error with read: Users cannot modify admin files')
    elif not(fileExists(fileName)):
        outputAndLog('Error with read: ' + fileName + ' does not exist')
    elif (userLoggedIn == ('root')):
        readFile(fileName)
    elif (canRead(fileName, userLoggedIn)):
        readFile(fileName)
    else:
        outputAndLog('User ' + userLoggedIn + ' denied read access to file ' + fileName)

def   write(fileName, text):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(isAUserLoggedIn):
        outputAndLog('Error with write: A User must be logged In')
    elif (isAdminFile(fileName)):
        outputAndLog('Error with write: Users cannot modify admin files')
    elif not(fileExists(fileName)):
        outputAndLog('Error with write: ' + fileName + ' does not exist')
    elif (userLoggedIn == ('root')):
        updateFile(fileName, text)
        outputAndLog('User ' + userLoggedIn + ' wrote to ' + fileName + ': ' + text)
    elif (canWrite(fileName, userLoggedIn)):
        updateFile(fileName, text)
        outputAndLog('User ' + userLoggedIn + ' wrote to ' + fileName + ': ' + text)
    else:
        outputAndLog('User ' + userLoggedIn + ' denied write access to file ' + fileName)

def   execute(fileName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(isAUserLoggedIn):
        outputAndLog('Error with execute: A User must be logged In')
    elif (isAdminFile(fileName)):
        outputAndLog('Error with execute: Users cannot modify admin files')
    elif not(fileExists(fileName)):
        outputAndLog('Error with execute: ' + fileName + ' does not exist')
    elif (userLoggedIn == ('root')):
        outputAndLog('User ' + userLoggedIn + ' executed ' + fileName)
    elif (canExecute(fileName, userLoggedIn)):
        outputAndLog('User ' + userLoggedIn + ' executed ' + fileName)
    else:
        outputAndLog('User ' + userLoggedIn + ' denied execute access to file ' + fileName)

def   ls(fileName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(isAUserLoggedIn):
        outputAndLog('Error with ls: A User must be logged In')
    elif (isAdminFile(fileName)):
        outputAndLog('Error with ls: Users cannot modify admin files')
    elif not(fileExists(fileName)):
        outputAndLog('Error with ls: ' + fileName + ' does not exist')
    else:
        outputAndLog(getFileInfo(fileName))

def   end():
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    updateFile('files.txt', getFiles())
    updateFile('groups.txt', getGroups())
    sys.exit(0)


def   updateFile(fileName, text):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if (oldFiles.__contains__(fileName)):
        file = open(fileName, 'a+')
        file.write(text + '\n') 
        file.close() 
    else:
        file = open(fileName, 'w+')
        file.write(text + '\n') 
        file.close() 
        newProgram = False
        oldFiles.append(fileName)

def   readFile(fileName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    file = open(fileName, 'r')
    text = 'User ' + userLoggedIn + ' reads ' + fileName + ' as: \n'
    for line in file:
        text = text + line.strip() + '\n'
    outputAndLog(text.strip())

def   checkUsername(userName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    return accounts.__contains__(userName)


def   checkGroup(groupName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    return groups.__contains__(groupName)

def   checkPassowrd(password, userName) :
     global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
     return getPassword(userName) == password

def   getPassword(userName):
    file = open('accounts.txt', 'r')
    password=''
    for line in file:
         param = re.split('\s+', line)
         correctUser=(param[0]==userName)
         if correctUser:
            password=decode(param[1])
            break
    return password

def   fileExists(fileName):
     global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
     result = False
     for eachFile in files:
         if eachFile.getFileName() == fileName:
            result = True
            break
     return result

def   userInGroup(userName, groupName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if groupName == 'nil':
        return False
    else:
        users = groups[groupName]
        return users.__contains__(userName)

def   isOwner(fileName, userName):
     global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles          
     result = False
     if not(fileExists(fileName)):
        outputAndLog('File ' + fileName + ' does not exist')
     else:
         for eachFile in files: 
            if (eachFile.getFileName() == (fileName)):
                result = userName == (eachFile.getOwner())
                break
     return result

# getters
def   getGroupName(fileName):
     groupName = ''
     for eachFile in files:
        if (eachFile.getFileName() == fileName):
            groupName = eachFile.getGroup()
            break
     return groupName

def  getPermission(fileName, owner, group, others):
    theValues = [ False, False, False ]
    permissions = [ 'r', 'w', '-', '-', '-', '-', '-', '-', '-' ]
    for eachFile in files :
        if (eachFile.getFileName() == (fileName)):
            permissions = eachFile.getPermission()
            if (owner):
                theValues[0] = permissions[0] == ('r')
                theValues[1] = permissions[1] == ('w')
                theValues[2] = permissions[2] == ('x')
            elif (group):
                theValues[0] = permissions[3] == ('r')
                theValues[1] = permissions[4] == ('w')
                theValues[2] = permissions[5] == ('x')
            elif (others):
                theValues[0] = permissions[6] == ('r')
                theValues[1] = permissions[7] == ('w')
                theValues[2] = permissions[8] == ('x')
            break
    return theValues

def  ownerGroupmemberOthers(fileName, userName):
    permissions = [ False, False, False ]
    permissions[0] = isOwner(fileName, userName)
    permissions[1] = userInGroup(userName, getGroupName(fileName))
    permissions[2] = not(permissions[0] or permissions[1])
    return permissions

def  getRWX(fileName, userName):
    ow_gm_ot = ownerGroupmemberOthers(fileName, userName)
    rwx = getPermission(fileName, ow_gm_ot[0], ow_gm_ot[1], ow_gm_ot[2])
    return rwx

def   canRead(fileName, userName)  :   
    return getRWX(fileName, userName)[0]

def   canWrite(fileName, userName):
    return getRWX(fileName, userName)[1]

def   canExecute(fileName, userName): 
    return getRWX(fileName, userName)[2]

def   addToAccounts(userName, password):
    if not(checkUsername(userName)):
        accounts[userName] = password
        updateFile("accounts.txt", userName+" "+encode(password))
        outputAndLog('User ' + userName + ' created')
    else:
        outputAndLog('Error with useradd: User ' + userName + ' already exists')

def   addGroup(groupName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(checkGroup(groupName)):
        users = []
        groups[groupName] = users
        outputAndLog('Group ' + groupName + ' created')
    else:
        outputAndLog('Error with groupadd: Group ' + groupName + ' already exists')

def   addUserToGroup(groupName, userName): 
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if not(groups.__contains__(groupName)):
        outputAndLog('Error: Group ' + groupName + ' does not exist')
    else:
        usersInGroup = groups[groupName]
        if usersInGroup.__contains__(userName):
            outputAndLog('Error: User ' + userName + ' already exists')
        else:
            usersInGroup.append(userName)
            groups[groupName] = usersInGroup
            outputAndLog('User ' + userName + ' added to Group ' + groupName)

def   makeFile(fileName, userName):
    global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
    if (fileExists(fileName)):
        outputAndLog('Error! File ' + fileName + ' already exists')
    else:
        newFile = FileComponents(fileName, userName, 'nil')
        files.append(newFile)
        file = open(fileName, 'w+')
        outputAndLog('User ' + userName + ' created file ' + fileName + ' with default permissions')

def   updatePermissions(fileName, permissions):
     global newProgram, accounts, groups, files, userLoggedIn, isAUserLoggedIn, oldFiles
     count = 0
     for eachFile in files:
        if (eachFile.getFileName() == (fileName)):
            theFile = eachFile
            theFile.setPermission(permissions)
            files[count] = theFile
            break
        count = count + 1;
        
def   changePermissions(fileName, permissions):
    str = list(permissions)
    updatePermissions(fileName, str)
    outputAndLog('Permissions for File ' + fileName + ' set to ' + separatePerms(permissions) + ' User ' + userLoggedIn)


def   changeOwner(userName, fileName):
    if not(checkUsername(userName)):
        outputAndLog('Error: User ' + userName + '  does not exist')
    else:
        count = 0
        for eachFile in files:
            if (eachFile.getFileName() == (fileName)):
                theFile = eachFile
                theFile.setOwner(userName)
                files[count] = theFile
                outputAndLog('Owner of File ' + fileName + ' changed to ' + userName + ' by User ' + userLoggedIn)
                break
            count = count + 1;

def   changeGroup(userName, groupName, fileName):
    if (not(userName == ('root')) and not(userInGroup(userName, groupName))):
        outputAndLog('Error with chgrp: User ' + userName + '  does not belong to group ' + groupName)
    else:
        count = 0
        for eachFile in files:
            if eachFile.getFileName() == (fileName):
                theFile = eachFile
                theFile.setGroup(groupName)
                files[count] = theFile
                outputAndLog('Group for  ' + fileName + ' changed to ' + groupName + ' by ' + userName)
                break
            count = count + 1

def   getAccounts():
     text = ''
     for userName in accounts:
        text = text + 'Username: ' + userName + ' Passowrd: ' + accounts[userName] + '\n'
     return text

def   getGroups():
     text = ''
     for groupName in groups:
        users = ''
        for eachUser in groups[groupName]:
            users = users + eachUser + ' '
        text = text + groupName + ': ' + users.strip() + '\n'
     return text

def   getFiles():
     text = ''
     for eachFile in files:
         fileName = eachFile.getFileName()
         owner = eachFile.getOwner()
         group = eachFile.getGroup()
         permissions = eachFile.getPermission()
         strPerms = ''
         count = 1
         for eachLetter in permissions:
            strPerms = strPerms + permissions[count - 1]
            if (count % 3 == 0):
                strPerms += ' '
            count = count + 1
            text = fileName + ': ' + owner + ' ' + group + ' ' + strPerms.strip() + '\n'
     return text

def  getFileInfo(theFile):
     text = ''
     for eachFile in files:
         fileName = eachFile.getFileName()
         if (theFile == (fileName)):
             owner = eachFile.getOwner()
             group = eachFile.getGroup()
             permissions = eachFile.getPermission()
             strPerms = ''
             count = 1
             for eachLetter in permissions:
                strPerms = strPerms + permissions[count - 1] + ' '
                if (count % 3 == 0):
                    strPerms += ' '
                count = count + 1
             text = fileName + ': ' + owner + ' ' + group + ' ' + strPerms.strip()
     return text

def encode(input):
    passwdA = "\"" + input + "\""
    cmd1 = "echo " + passwdA + "| \\openssl enc -base64 -e -aes-128-ecb -nosalt -pass pass:\"lol\""
    encoded = subprocess.check_output(cmd1, shell=True).strip()
    return encoded
def decode(input):    
    encodedA = "\"" + input + "\""
    cmd2 = "echo " + encodedA + "| \\openssl enc -base64 -d -aes-128-ecb -nosalt -pass pass:\"lol\""
    decoded = subprocess.check_output(cmd2, shell=True).strip()
    return decoded

def   outputAndLog(text):
    print(text)
    updateFile('audit.txt', text.strip())

def   isAdminFile(fileName):
    return fileName == ('accounts.txt') or fileName == ('audit.txt')or fileName == ('groups.txt')or fileName == ('files.txt')

def   separatePerms(perms):
     output = ''
     count = 1
     for eachLetter in list(perms):
        if (count % 3 <> 0):
            output = output + eachLetter
        else:
            output = output + eachLetter + ' '
        count = count + 1
     return output.strip()
def main():
    fileName=raw_input("Enter the name of the file you are reading from: ")
    file = open(fileName)
    count = 0
    counter = 0
    for line in file:
        line = line.strip()
        params = re.split('\s+', line)
        eachCommand = params[0].strip()
        if counter == 0:
            if not(params[0] == 'useradd' and params[1] == 'root' and params[2] <> ''):
                outputAndLog('Error: Root user should be created first')
                return None
        if eachCommand == 'useradd':
            useradd(params[1], params[2])
        elif eachCommand == 'login':
            login(params[1], params[2])
        elif eachCommand == 'logout':
            logout()
        elif eachCommand == 'groupadd':
            groupadd(params[1])
        elif eachCommand == 'usergrp':
            usergrp(params[1], params[2])
        elif eachCommand == 'mkfile':
            mkfile(params[1])
        elif eachCommand == 'chmod':
            chmod(params[1], (params[2] + params[3] + params[4]))
        elif eachCommand == 'chown':
            chown(params[1], params[2])
        elif eachCommand == 'chgrp':
            chgrp(params[1], params[2])
        elif eachCommand == 'read':
            read(params[1])
        elif eachCommand == 'write':
            text = ''
            ii = 0;
            for eachWord in params:
                if ii > 1:
                    text = text + params[ii] + ' '
                ii = ii + 1
            write(params[1], text.strip())
        elif eachCommand == 'execute':
            execute(params[1])
        elif eachCommand == 'ls':
            ls(params[1])
        elif eachCommand == 'end':
            end()
        else:
            print 'Invalid Command'
        counter = counter + 1
main()
