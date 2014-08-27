import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;

/**
 * The Class Access.
 */
public class access {

	/**
	 * The Class FileComponents.
	 */
	private static class FileComponents {
		
		/** The filename. */
		private String filename = "";
		
		/** The owner. */
		private String owner = "";
		
		/** The group. */
		private String group = "";
		
		/** The permissions. */
		private String[] permissions = { "r", "w", "-", "-", "-", "-", "-",
				"-", "-" };

		/**
		 * Instantiates a new file components.
		 *
		 * @param nfilename the nfilename
		 * @param nowner the nowner
		 * @param ngroup the ngroup
		 */
		private FileComponents(String nfilename, String nowner, String ngroup) {
			this.filename = nfilename;
			this.owner = nowner;
			this.group = ngroup;

		}

		/**
		 * Gets the owner.
		 *
		 * @return the owner
		 */
		public String getOwner() {
			return this.owner;
		}

		/**
		 * Gets the file name.
		 *
		 * @return the file name
		 */
		public String getFileName() {
			return this.filename;
		}

		/**
		 * Gets the group.
		 *
		 * @return the group
		 */
		public String getGroup() {
			return this.group;
		}

		/**
		 * Gets the permission.
		 *
		 * @return the permission
		 */
		public String[] getPermission() {
			return permissions;
		}

		/**
		 * Sets the group.
		 *
		 * @param groupName the new group
		 */
		public void setGroup(String groupName) {
			this.group = groupName;
		}

		/**
		 * Sets the owner.
		 *
		 * @param owner the new owner
		 */
		public void setOwner(String owner) {
			this.owner = owner;
		}

		/**
		 * Sets the permission.
		 *
		 * @param newPerm the new permission
		 */
		public void setPermission(String[] newPerm) {
			this.permissions = newPerm;
		}
	}

	/** The new program. */
	public static boolean newProgram = true;
	
	/** The accounts. */
	public static HashMap<String, String> accounts = new HashMap<String, String>();
	
	/** The groups. */
	public static HashMap<String, ArrayList<String>> groups = new HashMap<String, ArrayList<String>>();
	
	/** The files. */
	public static ArrayList<FileComponents> files = new ArrayList<FileComponents>();
	
	/** The user logged in. */
	public static String userLoggedIn = "";
	
	/** The is a user logged in. */
	public static boolean isAUserLoggedIn = false;
	
	/** The old files. */
	public static ArrayList<String> oldFiles = new ArrayList<String>();

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("What file are you reading from?");
		Scanner in = new Scanner(System.in);
		String fileName = in.next();
		FileReader foo = new FileReader(fileName);
		Scanner inFile = new Scanner(foo);
		int counter = 0;
		while (inFile.hasNext()) {
			String input = inFile.nextLine();
			String[] params = input.trim().split(" ");
			String eachCommand = params[0];
			if (counter == 0) {
				if (!(params[0].equals("useradd") && params[1].equals("root") && !params[2]
						.isEmpty())) {
					outputAndLog("Error: Root user should be created first");
					return;
				}
			}
			switch (eachCommand) {
			case "useradd":
				useradd(params[1], params[2]);
				break;
			case "login":
				login(params[1], params[2]);
				break;
			case "logout":
				logout();
				break;
			case "groupadd":
				groupadd(params[1]);
				break;
			case "usergrp":
				usergrp(params[1], params[2]);
				break;
			case "mkfile":
				mkfile(params[1]);
				break;
			case "chmod":
				chmod(params[1], (params[2] + params[3] + params[4]));
				break;
			case "chown":
				chown(params[1], params[2]);
				break;
			case "chgrp":
				chgrp(params[1], params[2]);
				break;
			case "read":
				read(params[1]);
				break;
			case "write":
				String text = "";
				for (int i = 2; i < params.length; i++) {
					text = text + params[i] + " ";
				}
				write(params[1], text.trim());
				break;
			case "execute":
				execute(params[1]);
				break;
			case "ls":
				ls(params[1]);
				break;
			case "end":
				end();
				break;
			default:
				System.out.println("Invalid Command");
			}
			counter++;
		}
	}
	
	/**
	 * Useradd.
	 *
	 * @param userName the user name
	 * @param password the password
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void useradd(String userName, String password)
			throws IOException, InterruptedException {
		if (newProgram && userName.equals("root"))
			addToAccounts(userName, password);
		else if (!userLoggedIn.equals("root"))
			outputAndLog("Error: only root may issue useradd command");
		else
			addToAccounts(userName, password);
	}

	/**
	 * Login.
	 *
	 * @param userName the user name
	 * @param password the password
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void login(String userName, String password)
			throws IOException, InterruptedException {
		if (isAUserLoggedIn)
			outputAndLog("Login Failed: User " + userLoggedIn + " logged In. No two users can login at the same time.");
		else if (checkUsername(userName) && checkPassowrd(password, userName)) {
			outputAndLog("User " + userName + " logged in");
			userLoggedIn = userName;
			isAUserLoggedIn = true;
		} else
			outputAndLog("Login failed: invalid username or password");
	}

	/**
	 * Logout.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void logout() throws IOException {
		if (!isAUserLoggedIn)
			outputAndLog("Error: There is no user logged in");
		else {
			isAUserLoggedIn = false;
			outputAndLog("User " + userLoggedIn + " is logged out");
			userLoggedIn = "";
		}

	}

	/**
	 * Groupadd.
	 *
	 * @param groupName the group name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void groupadd(String groupName) throws IOException {
		if (!isAUserLoggedIn)
			outputAndLog("Error: There is no user logged in");
		else if (!userLoggedIn.equals("root"))
			outputAndLog("Error: only root may issue groupadd command");
		else if (groupName.equals("nil"))
			outputAndLog("Error: nil is not a valid group name");
		else
			addGroup(groupName);
	}

	/**
	 * Usergrp.
	 *
	 * @param userName the user name
	 * @param groupName the group name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void usergrp(String userName, String groupName)
			throws IOException {
		if (!isAUserLoggedIn)
			outputAndLog("Error: There is no user logged in");
		else if (!userLoggedIn.equals("root"))
			outputAndLog("Error: The roor user is not logged in");
		else if (groupName.equals("nil"))
			outputAndLog("Error: nil is not a valid group name");
		else
			addUserToGroup(groupName, userName);
	}

	/**
	 * Mkfile.
	 *
	 * @param fileName the file name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void mkfile(String fileName) throws IOException {
		if (!isAUserLoggedIn)
			outputAndLog("Error: There is no user logged In");
		else if (isAdminFile(fileName))
			outputAndLog("Error: Users cannot modify admin files");
		else
			makeFile(fileName, userLoggedIn);

	}

	/**
	 * Chmod.
	 *
	 * @param fileName the file name
	 * @param permissions the permissions
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void chmod(String fileName, String permissions)
			throws IOException {
		if (!isAUserLoggedIn)
			outputAndLog("Error with chmod: There is no user logged In");
		else if (!fileExists(fileName))
			outputAndLog("Error with chmod: " + fileName + " does not exist");
		else if (!isOwner(fileName, userLoggedIn)
				&& !userLoggedIn.equals("root")) {
			outputAndLog("Error with chmod: Current user is not the owner and not root");
		} else if (isAdminFile(fileName))
			outputAndLog("Error with chmod: Users cannot modify admin files");
		else {
			changePermissions(fileName, permissions);
		}
	}

	/**
	 * Chown.
	 *
	 * @param fileName the file name
	 * @param userName the user name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void chown(String fileName, String userName)
			throws IOException {
		if (!isAUserLoggedIn)
			outputAndLog("Error with chown: There is no user logged In");
		else if (!fileExists(fileName))
			outputAndLog("Error with chown: " + fileName + " does not exist");
		else if (!isOwner(fileName, userLoggedIn)
				&& !userLoggedIn.equals("root"))
			outputAndLog("Error with chown: Current user is not the owner and not root");
		else if (isAdminFile(fileName))
			outputAndLog("Error with chown: Users cannot modify admin files");
		else if (!fileExists(fileName))
			outputAndLog("Error with read: " + fileName + " does not exist");
		else
			changeOwner(userName, fileName);
	}

	/**
	 * Chgrp.
	 *
	 * @param fileName the file name
	 * @param groupName the group name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void chgrp(String fileName, String groupName)
			throws IOException {
		if (!isAUserLoggedIn)
			outputAndLog("Error with chgrp: There is no user logged In");
		else if (!fileExists(fileName))
			outputAndLog("Error with chgrp: " + fileName + " does not exist");
		else if (!isOwner(fileName, userLoggedIn)
				&& !userLoggedIn.equals("root"))
			outputAndLog("Error with chgrp: Current user is not the owner and not root");
		else if (isAdminFile(fileName))
			outputAndLog("Error with chgrp: Users cannot modify admin files");
		else if (!checkGroup(groupName))
			outputAndLog("Error with chgrp: Group " + groupName
					+ " does not exist");
		else {
			changeGroup(userLoggedIn, groupName, fileName);
		}
	}

	/**
	 * Read.
	 *
	 * @param fileName the file name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void read(String fileName) throws IOException {
		if (!isAUserLoggedIn)
			outputAndLog("Error with read: There is no user logged In");
		else if (isAdminFile(fileName))
			outputAndLog("Error with read: Users cannot modify admin files");
		else if (!fileExists(fileName))
			outputAndLog("Error with read: " + fileName + " does not exist");
		else if (userLoggedIn.equals("root"))
			readFile(fileName);
		else if (canRead(fileName, userLoggedIn))
			readFile(fileName);
		else
			outputAndLog("User " + userLoggedIn + " denied access to read "
					+ fileName);

	}

	/**
	 * Write.
	 *
	 * @param fileName the file name
	 * @param text the text
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void write(String fileName, String text) throws IOException {
		if (!isAUserLoggedIn)
			outputAndLog("Error with write: There is no user logged In");
		else if (isAdminFile(fileName))
			outputAndLog("Error with write: Users cannot modify admin files");
		else if (!fileExists(fileName))
			outputAndLog("Error with write: " + fileName + " does not exist");
		else if (userLoggedIn.equals("root")) {
			updateFile(fileName, text);
			outputAndLog("User " + userLoggedIn + " wrote to " + fileName
					+ ": " + text);
		} else if (canWrite(fileName, userLoggedIn)) {
			updateFile(fileName, text);
			outputAndLog("User " + userLoggedIn + " wrote to " + fileName
					+ ": " + text);
		} else
			outputAndLog("User " + userLoggedIn + " denied access to write to "
					+ fileName);
	}

	/**
	 * Execute.
	 *
	 * @param fileName the file name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void execute(String fileName) throws IOException {
		if (!isAUserLoggedIn)
			outputAndLog("Error with execute: There is no user logged In");
		else if (isAdminFile(fileName))
			outputAndLog("Error with execute: Users cannot modify admin files");
		else if (!fileExists(fileName))
			outputAndLog("Error with execute: " + fileName + " does not exist");
		else if (userLoggedIn.equals("root"))
			outputAndLog("User " + userLoggedIn + " executed " + fileName);
		else if (canExecute(fileName, userLoggedIn))
			outputAndLog("User " + userLoggedIn + " executed " + fileName);
		else
			outputAndLog("User " + userLoggedIn + " denied access to execute "
					+ fileName);
	}

	/**
	 * Ls.
	 *
	 * @param fileName the file name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void ls(String fileName) throws IOException {
		if (!isAUserLoggedIn)
			outputAndLog("Error with ls: There is no user logged In");
		else if (isAdminFile(fileName))
			outputAndLog("Error with ls: Users cannot modify admin files");
		else if (!fileExists(fileName))
			outputAndLog("Error with ls: " + fileName + " does not exist");
		else
			outputAndLog(getFileInfo(fileName));
	}

	/**
	 * End.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void end() throws IOException {
		// updateFile("accounts.txt", getAccounts());
		updateFile("files.txt", getFiles());
		updateFile("groups.txt", getGroups());
		System.exit(0);
	}

	// Accessing files
	/**
	 * Update file.
	 *
	 * @param fileName the file name
	 * @param text the text
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void updateFile(String fileName, String text)
			throws IOException {
		if (oldFiles.contains(fileName)) {
			FileWriter fw = new FileWriter(fileName, true);
			fw.write(text + "\n");
			fw.close();
		} else {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			writer.println(text);
			writer.close();
			newProgram = false;
			oldFiles.add(fileName);
		}
	}

	/**
	 * Read file.
	 *
	 * @param fileName the file name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void readFile(String fileName) throws IOException {
		FileReader reader = new FileReader(fileName);
		Scanner inFile = new Scanner(reader);
		String text = "User " + userLoggedIn + " reads " + fileName
				+ " as follows: \n";
		while (inFile.hasNext()) {
			text = text + inFile.nextLine() + "\n";
		}
		outputAndLog(text.trim());
	}

	// validating access
	/**
	 * Check username.
	 *
	 * @param userName the user name
	 * @return true, if successful
	 */
	public static boolean checkUsername(String userName) {
		return accounts.containsKey(userName);

	}

	/**
	 * Check group.
	 *
	 * @param groupName the group name
	 * @return true, if successful
	 */
	public static boolean checkGroup(String groupName) {
		return groups.containsKey(groupName);
	}

	/**
	 * Check passowrd.
	 *
	 * @param password the password
	 * @param userName the user name
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static boolean checkPassowrd(String password, String userName) throws IOException, InterruptedException {
		boolean result = false;
		String decodedPassword=getPassword(userName);
		result=password.equals(decodedPassword);
		return result;
	}
	
	/**
	 * Gets the password.
	 *
	 * @param userName the user name
	 * @return the password
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static String getPassword(String userName) throws IOException, InterruptedException{
		String password="";
		String user_pass="";
		FileReader reader = new FileReader("accounts.txt");
		Scanner inFile = new Scanner(reader);
		boolean foundUser=false;
		while (inFile.hasNext() && !foundUser) {
			user_pass = inFile.nextLine();
			String [] both=user_pass.split(" ");
			foundUser=both[0].equals(userName);
			if(foundUser){
				password=decode(both[1]);
			}
		}
		return password;
	}

	/**
	 * File exists.
	 *
	 * @param fileName the file name
	 * @return true, if successful
	 */
	public static boolean fileExists(String fileName) {
		boolean result = false;
		for (FileComponents eachFile : files) {
			if (eachFile.getFileName().equals(fileName)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * User in group.
	 *
	 * @param userName the user name
	 * @param groupName the group name
	 * @return true, if successful
	 */
	public static boolean userInGroup(String userName, String groupName) {
		boolean result = false;
		for (Entry<String, ArrayList<String>> entry : groups.entrySet()) {
			String key = entry.getKey();
			if (key.equals(groupName)) {
				ArrayList<String> value = entry.getValue();
				result = value.contains(userName);
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if is owner.
	 *
	 * @param fileName the file name
	 * @param userName the user name
	 * @return true, if is owner
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static boolean isOwner(String fileName, String userName)
			throws IOException {
		boolean result = false;
		if (!fileExists(fileName)) {
			outputAndLog("File " + fileName + " does not exist");
		} else {
			for (FileComponents eachFile : files) {
				if (eachFile.getFileName().equals(fileName)) {
					result = userName.equalsIgnoreCase(eachFile.getOwner());
					break;
				}
			}
		}
		return result;
	}

	// getters
	/**
	 * Gets the group name.
	 *
	 * @param fileName the file name
	 * @return the group name
	 */
	public static String getGroupName(String fileName) {
		String groupName = "";
		for (FileComponents eachFile : files) {
			if (eachFile.getFileName().equals(fileName)) {
				groupName = eachFile.getGroup();
				break;
			}
		}
		return groupName;
	}

	/**
	 * Gets the permission.
	 *
	 * @param fileName the file name
	 * @param owner the owner
	 * @param group the group
	 * @param others the others
	 * @return the permission
	 */
	public static boolean[] getPermission(String fileName, boolean owner,
			boolean group, boolean others) {
		boolean[] theValues = { false, false, false };
		String[] permissions = { "r", "w", "-", "-", "-", "-", "-", "-", "-" };
		for (FileComponents eachFile : files) {
			if (eachFile.getFileName().equals(fileName)) {
				permissions = eachFile.getPermission();
				if (owner) {
					theValues[0] = permissions[0].equals("r");
					theValues[1] = permissions[1].equals("w");
					theValues[2] = permissions[2].equals("x");
				} else if (group) {
					theValues[0] = permissions[3].equals("r");
					theValues[1] = permissions[4].equals("w");
					theValues[2] = permissions[5].equals("x");
				} else if (others) {
					theValues[0] = permissions[6].equals("r");
					theValues[1] = permissions[7].equals("w");
					theValues[2] = permissions[8].equals("x");
				}
				break;
			}
		}
		return theValues;
	}

	/**
	 * Owner groupmember others.
	 *
	 * @param fileName the file name
	 * @param userName the user name
	 * @return the boolean[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static boolean[] ownerGroupmemberOthers(String fileName,
			String userName) throws IOException {
		boolean[] permissions = { false, false, false };
		permissions[0] = isOwner(fileName, userName);
		permissions[1] = userInGroup(userName, getGroupName(fileName));
		permissions[2] = !(permissions[0] || permissions[1]);
		return permissions;
	}

	/**
	 * Gets the rwx.
	 *
	 * @param fileName the file name
	 * @param userName the user name
	 * @return the rwx
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static boolean[] getRWX(String fileName, String userName)
			throws IOException {
		boolean[] ow_gm_ot = ownerGroupmemberOthers(fileName, userName);
		boolean[] rwx = getPermission(fileName, ow_gm_ot[0], ow_gm_ot[1],
				ow_gm_ot[2]);
		return rwx;
	}

	// ReadWriteExecute
	/**
	 * Can read.
	 *
	 * @param fileName the file name
	 * @param userName the user name
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static boolean canRead(String fileName, String userName)
			throws IOException {
		return getRWX(fileName, userName)[0];
	}

	/**
	 * Can write.
	 *
	 * @param fileName the file name
	 * @param userName the user name
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static boolean canWrite(String fileName, String userName)
			throws IOException {
		return getRWX(fileName, userName)[1];
	}

	/**
	 * Can execute.
	 *
	 * @param fileName the file name
	 * @param userName the user name
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static boolean canExecute(String fileName, String userName)
			throws IOException {
		return getRWX(fileName, userName)[2];
	}

	// adding to accounts/groups/files
	/**
	 * Adds the to accounts.
	 *
	 * @param userName the user name
	 * @param password the password
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void addToAccounts(String userName, String password)
			throws IOException, InterruptedException {
		if (!checkUsername(userName)) {
			accounts.put(userName, password);
			updateFile("accounts.txt", userName+" "+encode(password));
			outputAndLog("User " + userName + " created");
		} else {
			outputAndLog("Error with useradd: User " + userName
					+ " already exists");
		}
	}

	/**
	 * Adds the group.
	 *
	 * @param groupName the group name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void addGroup(String groupName) throws IOException {
		if (!checkGroup(groupName)) {
			ArrayList<String> users = new ArrayList<String>();
			groups.put(groupName, users);
			outputAndLog("Group " + groupName + " created");
		} else {
			outputAndLog("Error with groupadd: Group " + groupName
					+ " already exists");
		}
	}

	/**
	 * Adds the user to group.
	 *
	 * @param groupName the group name
	 * @param userName the user name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void addUserToGroup(String groupName, String userName)
			throws IOException {
		if (!groups.containsKey(groupName)) {
			outputAndLog("Error: Group " + groupName + " does not exist");
		} else {
			for (Entry<String, ArrayList<String>> entry : groups.entrySet()) {
				String eachGroupName = entry.getKey();
				if (eachGroupName.equals(groupName)) {
					ArrayList<String> users = entry.getValue();
					if (users.contains(userName)) {
						outputAndLog("Error: User " + userName
								+ " already exists");
					} else {
						users.add(userName);
						entry.setValue(users);
						outputAndLog("User " + userName + " added to Group "
								+ groupName);
					}
					break;
				}
			}
		}
	}

	// files and permissions
	/**
	 * Make file.
	 *
	 * @param fileName the file name
	 * @param userName the user name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void makeFile(String fileName, String userName)
			throws IOException {
		if (fileExists(fileName)) {
			outputAndLog("Error: File: " + fileName + " already exists");
		} else {
			FileComponents newFile = new FileComponents(fileName, userName,
					"nil");
			files.add(newFile);
			outputAndLog("User " + userName + " created file " + fileName+" with default group and default permissions.");
		}
	}

	/**
	 * Update permissions.
	 *
	 * @param fileName the file name
	 * @param permissions the permissions
	 */
	public static void updatePermissions(String fileName, String[] permissions) {
		int count = 0;
		for (FileComponents eachFile : files) {

			if (eachFile.getFileName().equals(fileName)) {
				FileComponents theFile = eachFile;
				theFile.setPermission(permissions);
				files.set(count, theFile);
				break;
			}
			count++;
		}
	}

	/**
	 * Change permissions.
	 *
	 * @param fileName the file name
	 * @param permissions the permissions
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void changePermissions(String fileName, String permissions)
			throws IOException {
		String[] str = permissions.split("");
		String[] nstr = new String[9];
		for (int count = 1; count < str.length; count++) {
			nstr[count - 1] = str[count];
		}
		updatePermissions(fileName, nstr);
		outputAndLog("Permissions for File " + fileName + " set to "
				+ separatePerms(permissions) + " by User " + userLoggedIn);

	}

	/**
	 * Change owner.
	 *
	 * @param userName the user name
	 * @param fileName the file name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void changeOwner(String userName, String fileName)
			throws IOException {

		if (!checkUsername(userName)) {
			outputAndLog("Error: User " + userName + "  does not exist");
		} else {
			int count = 0;
			for (FileComponents eachFile : files) {
				if (eachFile.getFileName().equals(fileName)) {
					FileComponents theFile = eachFile;
					theFile.setOwner(userName);
					files.set(count, theFile);
					outputAndLog("Owner of File " + fileName + " changed to "
							+ userName + " by User " + userLoggedIn);
					break;
				}
				count++;
			}
		}
	}

	/**
	 * Change group.
	 *
	 * @param userName the user name
	 * @param groupName the group name
	 * @param fileName the file name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void changeGroup(String userName, String groupName,
			String fileName) throws IOException {
		if (!userName.equals("root") && !userInGroup(userName, groupName)) {
			outputAndLog("Error with chgrp: User " + userName
					+ "  does not belong to group " + groupName);
		} else {
			int count = 0;
			for (FileComponents eachFile : files) {
				if (eachFile.getFileName().equals(fileName)) {
					FileComponents theFile = eachFile;
					theFile.setGroup(groupName);
					files.set(count, theFile);
					outputAndLog("Group for  " + fileName + " changed to "
							+ groupName + " by " + userName);
					break;
				}
				count++;
			}
		}
	}

	/**
	 * Gets the groups.
	 *
	 * @return the groups
	 */
	public static String getGroups() {
		String text = "";
		for (Entry<String, ArrayList<String>> entry : groups.entrySet()) {
			String groupName = entry.getKey();
			String users = "";
			for (String eachUser : entry.getValue()) {
				users = users + eachUser + " ";
			}
			text = text + groupName + ": " + users.trim() + "\n";
		}
		return text;
	}

	/**
	 * Gets the files.
	 *
	 * @return the files
	 */
	public static String getFiles() {
		String text = "";
		for (FileComponents eachFile : files) {
			String fileName = eachFile.getFileName();
			String owner = eachFile.getOwner();
			String group = eachFile.getGroup();
			String[] permissions = eachFile.getPermission();
			String strPerms = "";
			int count = 1;
			for (int i = 0; i < permissions.length; i++) {
				strPerms = strPerms + permissions[i];
				if (count % 3 == 0) {
					strPerms += " ";
				}
				count++;
			}
			text = fileName + ": " + owner + " " + group + " "
					+ strPerms.trim() + "\n";
		}
		return text;
	}

	/**
	 * Gets the file info.
	 *
	 * @param theFile the the file
	 * @return the file info
	 */
	public static String getFileInfo(String theFile) {
		String text = "";
		for (FileComponents eachFile : files) {
			String fileName = eachFile.getFileName();
			if (theFile.equals(fileName)) {
				String owner = eachFile.getOwner();
				String group = eachFile.getGroup();
				String[] permissions = eachFile.getPermission();
				String strPerms = "";
				int count = 1;
				for (int i = 0; i < permissions.length; i++) {
					strPerms = strPerms + permissions[i] + " ";
					if (count % 3 == 0) {
						strPerms += " ";
					}
					count++;
				}
				text = fileName + ": " + owner + " " + group + " "
						+ strPerms.trim();
			}
		}
		return text;
	}

	/**
	 * Output and log.
	 *
	 * @param text the text
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void outputAndLog(String text) throws IOException {
		System.out.println(text);
		updateFile("audit.txt", text);

	}

	/**
	 * Checks if is admin file.
	 *
	 * @param fileName the file name
	 * @return true, if is admin file
	 */
	public static boolean isAdminFile(String fileName) {
		return fileName.equals("accounts.txt") || fileName.equals("audit.txt")
				|| fileName.equals("groups.txt")
				|| fileName.equals("files.txt");
	}

	/**
	 * Separate perms.
	 *
	 * @param perms the perms
	 * @return the string
	 */
	public static String separatePerms(String perms) {
		String output = "";
		int count = 1;
		for (char eachLetter : perms.toCharArray()) {
			if (count % 3 != 0)
				output = output + eachLetter;
			else
				output = output + eachLetter + " ";
			count++;
		}
		return output.trim();
	}
	
	/**
	 * Encode.
	 *
	 * @param input the input
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static String encode(String input) throws IOException, InterruptedException{
    	String result="";
    	java.lang.Runtime rt = java.lang.Runtime.getRuntime();
    	String newIn="\""+input+"\"";
        String[] cmd = { "sh", "-c","echo "+newIn+" | \\openssl enc -base64 -e -aes-128-ecb -nosalt -pass pass:\"lol\"" };
        java.lang.Process p = rt.exec(cmd);
        p.waitFor();
        java.io.InputStream is = p.getInputStream();
        java.io.BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(is));
        String s = null;
        while ((s = reader.readLine()) != null) {
        	result=s;
        }
        is.close();
    	return result;
    }
    
    /**
     * Decode.
     *
     * @param input the input
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException the interrupted exception
     */
    public static String decode(String input) throws IOException, InterruptedException{
    	String result="";
    	java.lang.Runtime rt = java.lang.Runtime.getRuntime();
    	String newIn="\""+input+"\"";
        String[] cmd = { "sh", "-c","echo "+newIn+" | \\openssl enc -base64 -d -aes-128-ecb -nosalt -pass pass:\"lol\"" };
        java.lang.Process p = rt.exec(cmd);
        p.waitFor();
        java.io.InputStream is = p.getInputStream();
        java.io.BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(is));
        String s = null;
        while ((s = reader.readLine()) != null) {
            result=s;
        }
        is.close();
    	return result;
    }
}
