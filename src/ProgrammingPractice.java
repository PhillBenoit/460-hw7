import java.util.Scanner;
import java.sql.*;

public class ProgrammingPractice {

	public static final int MENU_MIN = 0;
	public static final int MENU_MAX = 4;
	
	private static final int STR_MAX = 255;
	private static final int TYPE_MAX = 20;
	private static final int DIFFICULTY_MAX = 10;
	
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
			month = input % 10000;
			max_day = DAYS_IN_MONTH[month];
			day = (input/100)%100;
			year = input%100;
			if (month == 2 && year % 4 == 0) max_day++;
			do_over = day <= max_day;
			if (do_over) System.err.printf("%d month only has %d days (you "
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
        
        try {
			db_connection.close();
		} catch (SQLException e) {}

        try {
			results.close();
		} catch (SQLException e) {}

        try {
			query.close();
		} catch (SQLException e) {}

        try {
			prepared_query.close();
		} catch (SQLException e) {}

    }
	
    public static void listPools() {
    	
    }
    
    public static void addProblem() {
    	
    }
    
    public static void listLegacyMembers() {
    	
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
			addProblem();
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
    	
    	try {
    		Class.forName("oracle.jdbc.OracleDriver");
    		db_connection = DriverManager.getConnection(connect_string,username,password);
    		if (db_connection == null)
    			throw new Exception("getConnection failed");
    		query = db_connection.createStatement();
    	} catch (Exception e) {
			e.printStackTrace();
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
