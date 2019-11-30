import java.util.ArrayList;
import java.util.Scanner;
import java.sql.*;

public class ProgrammingPractice {

	public static final int MENU_MIN = 0;
	public static final int MENU_MAX = 4;

	private static final int STR_MAX = 255;

	static Scanner keyboard = new Scanner(System.in);

	public static final int[] DAYS_IN_MONTH = {0,31,28,31,30,31,30,31,31,30,31,30,31};

	private static final String whitelist = " ',-_.@0123456789ABCDEFGHIJKLMNOP"
			+ "QRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private static final String LOW_MSG="The value you entered is below the minimum of ";
	private static final String HIGH_MSG="The value you entered is above the maximum of ";
	private static final String CONFIRMATION="You entered: ";

	private static final String MENU_INPUT="Please select an option";

	private static String username;
	private static String password;

	private static final String connect_string = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
	private static Connection db_connection;
	private static Statement query;
	private static PreparedStatement prepared_query;
	private static ResultSet results;

	private static String getYorN(String msg) {
		String answer = getString(msg, 5);

		while (answer.compareToIgnoreCase("t")   != 0 
				&& answer.compareToIgnoreCase("true") != 0 
				&& answer.compareToIgnoreCase("f")   != 0 
				&& answer.compareToIgnoreCase("false")  != 0) {

			if (answer.replace(" ", "").equals("")) {
				System.err.println("Error: Missing input.");
			} else {
				if (answer.compareToIgnoreCase("t")   != 0 
						&& answer.compareToIgnoreCase("true") != 0 
						&& answer.compareToIgnoreCase("f")   != 0 
						&& answer.compareToIgnoreCase("false")  != 0) {
					System.err.println("Error: Unexpected input.");
				}
			}
			answer = getString(msg, 5);
		} 

		if  (answer.compareToIgnoreCase("t")   == 0  
				|| answer.compareToIgnoreCase("true") == 0) {
			return "y";
		} 
		else {
			return "n";
		}
	}

	static void pause() {
		System.out.println("Press 'Enter' to coninue.");
		keyboard.nextLine();
	}

	private static boolean checkString(String s) {
		for(char c:s.toCharArray()) {
			if (whitelist.indexOf(c) == -1) {
				System.err.format("Forbidden character [%c] found in [%s]", c, s);
				return false;
			}
		}
		return true;
	}

	private static String getString(String msg, int max_length) {
		String input;
		boolean do_over;

		do {
			System.out.println(msg);
			input = keyboard.nextLine();
			do_over = input.isEmpty() || input.length() > max_length;
			if (do_over) System.err.println("invalid input length");
		} while (do_over || !checkString(input));

		return input;
	}

	public static boolean tryInt(String test){
		try{
			Integer.parseInt(test);
			return true;
		} catch (NumberFormatException e) {
			System.err.println("Invalid integer. Try again.");
			return false;
		}
	}

	public static int getInteger(String msg) {
		String stringInput;
		do {
			System.out.println(msg);
			stringInput = keyboard.nextLine();
		} while (!tryInt(stringInput));
		int number = Integer.parseInt(stringInput);
		System.out.println(CONFIRMATION + number + "\n");
		return number;
	}

	public static int getIntegerBetweenLowAndHigh(String msg, int low, int high) {
		msg = msg +	String.format(": (%d - %d)", low, high);
		int number = getInteger(msg);
		while (number < low || number > high) {
			if (number < low) System.err.println(LOW_MSG + low);
			if (number > high) System.err.println(HIGH_MSG + high);
			number = getInteger(msg);
		}
		return number;
	}

	static private void printMenu() {
		System.out.println(
				"1) Enter member information\r\n" + 
						"2) List the member(s) who have paid more than $400\r\n" + 
						"3) Add a problem into a specified problem pool\r\n" + 
						"4) List the problems and problem pools that each member has\r\n\n" + 
						"0) Quit"
				);
	}

	public static String getDate(String msg) {
		boolean do_over;
		int month;
		int day;
		int max_day;
		int year;
		int input;

		do {
			input = getIntegerBetweenLowAndHigh(msg, 10100, 123199);
			month = input / 10000;
			max_day = DAYS_IN_MONTH[month];
			day = (input/100)%100;
			year = input%100;
			if (month == 2 && year % 4 == 0) max_day++;
			do_over = day > max_day || day == 0;
			if (do_over) System.err.printf("%d month has %d days (you "
					+ "entered %d)", month, max_day, day);
		} while (do_over);

		return String.format("%06d", input);
	}

	public static void closeEverything() {
		try { 
			if(keyboard != null) {
				keyboard.close(); 
			}
		} 
		catch (Exception e) {
			System.err.println("Error closing keyboard reader.");
			e.printStackTrace();
		}

		if (db_connection != null)
			try {db_connection.close();} catch (SQLException e) {}

		if (query != null)
			try {query.close();} catch (SQLException e) {}
	}

	private static void checkResultErrors() {
		SQLWarning w = null;
		if (results != null) {
			try {
				w = results.getWarnings();
				while (w != null) {
					System.err.println(w.getMessage());
					w = results.getWarnings();
				}
			}
			catch (SQLException e1) {}
		}
	}

	private static void checkQueryErrors() {
		try {
			SQLWarning w = query.getWarnings();
			while (w != null) {
				System.err.println(w.getMessage());
				w = query.getWarnings();
			}
		} catch (SQLException e1) {}

	}

	private static void checkPQueryErrors() {
		if (prepared_query != null) {
			try {
				SQLWarning w = prepared_query.getWarnings();
				while (w != null) {
					System.err.println(w.getMessage());
					w = prepared_query.getWarnings();
				}
			} catch (SQLException e1) {}
		}
	}

	public static void listPools() {
		try {
			results = query.executeQuery("SELECT Member.firstName, "
					+ "Member.lastName, ComposedOf.poolName, ComposedOf.title,"
					+ " Problem.difficulty from Member, Problem, ComposedOf "
					+ "WHERE Member.emailAddress = Problem.ContributorEmail "
					+ "AND Problem.title = ComposedOf.title AND "
					+ "Problem.ContributorEmail = ComposedOf.emailAddress "
					+ "ORDER BY Member.lastName, ComposedOf.title, "
					+ "ComposedOf.poolName");

			if (!results.next()) {
				System.out.println("There are no problems!");
				return;
			}
			
			String format_string = "%%%ds  %%%ds  %%%ds  %%%ds\n";
			String[] header = {"Member","Problem Pool","Problem","Difficulty"};
			int[] col_length = new int[header.length];
			
			for (int i = 0; i < header.length; i++)
				col_length[i] = header[i].length();

			String name = "";
			do {
				name = results.getString(1) + " " + results.getString(2);
				col_length[0] = Math.max(col_length[0], name.length());
				for (int i = 1; i < header.length; i++)
					col_length[i] = Math.max(col_length[i],
							results.getString(i+2).length());
			} while (results.next());

			format_string = String.format(format_string, col_length[0],
					col_length[1], col_length[2], col_length[3]);
			
			results.first();
			String previous_name = "";
			System.out.printf(format_string, header[0], header[1], header[2],
					header[3]);
			do {
				name = results.getString(1) + " " + results.getString(2);
				if (name.equals(previous_name)) name = "";
				else {
					System.out.println();
					previous_name = String.valueOf(name);
				}
				System.out.printf(format_string, name, results.getString(3),
						results.getString(4), results.getString(5));
			} while (results.next());
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			checkResultErrors();
			checkQueryErrors();
		} finally {
			if (results != null) {
				try {results.close();} catch (SQLException e) {}
				results = null;
			}
			pause();
		}
	}

	public static void addProblemToPool() {
		try {
			results = query.executeQuery("SELECT COUNT(emailAddress) FROM ProblemsPool");
			results.next();
			if (results.getInt(1) == 0) {
				System.out.println("No problem pools exist!");
				return;
			}
			results.close();

			String member = getString("Enter email address for the member "
					+ "having the relevant problem pool:", STR_MAX);

			prepared_query = db_connection.prepareStatement("SELECT "
					+ "COUNT(emailAddress) FROM Member WHERE "
					+ "emailAddress = ?");
			prepared_query.setString(1, member);
			results = prepared_query.executeQuery();
			if (results.getInt(1) == 0) {
				System.out.println("No member exists with this email!");
				return;
			}
			results.close();
			prepared_query.close();

			prepared_query = db_connection.prepareStatement("SELECT poolName "
					+ "FROM ProblemsPool WHERE emailAddress = ?");
			prepared_query.setString(1, member);
			ArrayList<String> pools = new ArrayList<String>();
			results = prepared_query.executeQuery();
			while (results.next()) pools.add(results.getString(1));
			if (pools.isEmpty()) {
				System.out.println("This member has no problem pool!");
				return;
			}
			prepared_query.close();
			results.close();

			System.out.println("Problem pools available:\n");
			int count = 0;
			for (String p:pools) System.out.println(++count + " " + p);
			System.out.println();
			int pool = getIntegerBetweenLowAndHigh("Enter the number of "
					+ "problem pool desired:", 1, count);
			pool--;

			prepared_query = db_connection.prepareStatement("SELECT title FROM "
					+ "Problem WHERE ContributorEmail != ? AND title NOT IN "
					+ "(SELECT title FROM ComposedOf WHERE emailAddress = ? AND"
					+ " poolName = ?)");
			prepared_query.setString(1, member);
			prepared_query.setString(2, member);
			prepared_query.setString(3, pools.get(pool));
			results = prepared_query.executeQuery();
			ArrayList<String> problems = new ArrayList<String>();
			while (results.next()) problems.add(results.getString(1));
			if (problems.isEmpty()) {
				System.out.println("member has no problems that qualify for "
						+ "this pool.");
				return;
			}
			prepared_query.close();

			System.out.println("Problems available:\n");
			count = 0;
			for (String p:problems) System.out.println(++count + " " + p);
			System.out.println();
			int problem = getIntegerBetweenLowAndHigh("Enter the number of "
					+ "problem desired:", 1, count);
			problem--;

			prepared_query = db_connection.prepareStatement("INSERT INTO ComposedOf VALUES(?, ?, ?)");
			prepared_query.setString(1, problems.get(problem));
			prepared_query.setString(2, member);
			prepared_query.setString(3, pools.get(pool));
			prepared_query.executeUpdate();
			db_connection.commit();

			System.out.printf("Problem %s successfully added to problem pool \""
					+ "%s\" of member having email address %s\n",
					problems.get(problem), pools.get(pool), member);

		} catch (SQLException e) {
			System.err.println(e.getMessage());
			checkResultErrors();
			checkQueryErrors();
			checkPQueryErrors();
		} finally {
			if (prepared_query != null) {
				try {prepared_query.close();} catch (SQLException e) {}
				prepared_query = null;
			}
			if (results != null){
				try {results.close();} catch (SQLException e) {}
				results = null;
			}
			pause();
		}
	}

	public static void listLegacyMembers() {
		try {
			results = query.executeQuery("SELECT emailAddress FROM Member WHERE "
					+ "MONTHS_BETWEEN(SYSDATE, subscriptionStartDate)*8 > 400");
			if (results.next()) {
				System.out.println("Here are the email addresses of members who have paid more than $400.");
				do System.out.println(results.getString(1));
				while (results.next());
			}
			else System.out.println("No qualified paying member exists!");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			checkResultErrors();
			checkQueryErrors();
		} finally {
			if (results != null) {
				try {results.close();} catch (SQLException e) {}
				results = null;
			}
			pause();
		}
	}

	public static void addMember() {
		String firstName, lastName, email;
		String paying, staff, contributor;
		String date;

		firstName = getString("Enter first name:", STR_MAX);
		lastName = getString("Enter last name:", STR_MAX);
		email = getString("Enter email address:", STR_MAX);

		paying = getYorN("Is paying member (T or F):");
		staff = getYorN("Is staff member (T or F):");
		contributor = getYorN("Is problem contributor (T or F):");

		date = getDate(
				"Enter subscription start date (as MMDDYY, e.g., 120219):");

		try {
			prepared_query = db_connection.prepareStatement(
					"INSERT INTO Member VALUES(?,?,?,?,?,?,TO_DATE(?,'MMDDYY'))");
			prepared_query.setString(1, firstName);
			prepared_query.setString(2, lastName);
			prepared_query.setString(3, email);
			prepared_query.setString(4, paying);
			prepared_query.setString(5, staff);
			prepared_query.setString(6, contributor);
			prepared_query.setString(7, date);
			prepared_query.executeUpdate();
			db_connection.commit();
			System.out.println("New member added!");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			checkPQueryErrors();
		} finally {
			if (prepared_query != null){
				try {prepared_query.close();} catch (SQLException e) {}
				prepared_query = null;
			}
			pause();
		}
	}

	public static void parseMenuInput(int input) {
		switch (input) {
		case 0:
			break;
		case 1:
			addMember();
			break;
		case 2:
			listLegacyMembers();
			break;
		case 3:
			addProblemToPool();
			break;
		case 4:
			listPools();
			break;

		default:
			System.err.println("menu input not recognized");
			break;
		}
	}

	public static void main(String[] args) {
		int input;

		if(args.length < 2){
			System.err.println("Usage: java ProgrammingPractice user_name password");
			System.exit(2);
		}
		username = args[0];
		password = args[1];

		db_connection = null;
		query = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			db_connection = DriverManager.getConnection(connect_string,username,password);
			if (db_connection == null)
				throw new Exception("getConnection failed");
			query = db_connection.createStatement();
			if (query == null)
				throw new Exception("createStatement failed");

		} catch (Exception e) {
			e.printStackTrace();
			closeEverything();
			System.exit(1);
		}

		do {
			printMenu();
			input = getIntegerBetweenLowAndHigh(MENU_INPUT, MENU_MIN, MENU_MAX);
			parseMenuInput(input);
		} while (input != 0);
		closeEverything();
	}

}
