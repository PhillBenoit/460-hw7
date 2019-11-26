import java.util.Scanner;

public class ProgrammingPractice {

	public static final int MENU_MIN = 0;
	public static final int MENU_MAX = 4;
	
	static Scanner keyboard = new Scanner(System.in);
	
	private static final String LOW_MSG="The value you entered is below the minimum of ";
    private static final String HIGH_MSG="The value you entered is above the maximum of ";
    private static final String CONFIRMATION="You entered: ";
    
    private static final String MENU_INPUT="Please select an option";

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
	
    public static void closeScanner() {
        try { 
            if(keyboard != null) {
                keyboard.close(); 
            }
        } 
        catch (Exception e) {
            System.err.println("Error closing keyboard reader.");
            e.printStackTrace();
        }
    }
	
    public static void main(String[] args) {
    	int input;
    	
        do {
        	printMenu();
        	input = getIntegerBetweenLowAndHigh(MENU_INPUT, MENU_MIN, MENU_MAX);
        	
        	
        } while (input != 0);
        closeScanner();
    }

}
