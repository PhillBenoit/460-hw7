import java.io.*;
import java.sql.*;

class JDBCDemo{
	private static final String connect_string = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
    private static Connection m_conn;

    public static void main(String[] args)
    {
    	if(args.length < 2){
    		System.err.println("Usage: java JDBCDemo user_name password");
    		System.exit(2);
    	}

    	String user_name = args[0];
    	String password = args[1];
		Statement s = null;
		PreparedStatement pstmt = null;
    	System.out.println("Begin JDBCDemo\n");

        try {
          	// Registers drivers
			Class.forName("oracle.jdbc.OracleDriver");  
			//get a connection
			m_conn = DriverManager.getConnection(connect_string,user_name,password);  
			if (m_conn == null) 
				throw new Exception("getConnection failed");
			//create a statement, resultset
			s = m_conn.createStatement(); 
			ResultSet rs = null;
			
			try {
			    if (s == null) throw new Exception("createStatement failed");
			    System.out.println("Creating table TEST19 and inserting data . . .\n");
			    s.executeUpdate("CREATE TABLE TEST19(Name VARCHAR(10), id NUMBER(5))");
			    s.executeUpdate("INSERT INTO TEST19 VALUES('bob',5)");
			    s.executeUpdate("INSERT INTO TEST19 VALUES('mary',6)");
			   
			    // using prepared statements
				pstmt = m_conn.prepareStatement("INSERT INTO TEST19 VALUES(?,?)");
				pstmt.setString(1, "Alex");
				pstmt.setInt(2, 7);
				pstmt.executeUpdate();
				m_conn.commit();

			    System.out.println("Query the table and print results . . .\n");
			    rs = s.executeQuery("Select * from TEST19");

			    // using column indexes
			    while(rs.next())
					System.out.println(rs.getString(1) + "\t" + rs.getString(2));

				// finding metadata
				System.out.println("\nPrinting column names . . .\n");
				ResultSetMetaData rsmeta = rs.getMetaData();
				int columns = rsmeta.getColumnCount();
				for (int i = 1; i<=columns; i++){
					System.out.println(rsmeta.getColumnName(i)+"\t");
				}

			    System.out.println("\nDropping table TEST19 . . .\n");
			    s.executeUpdate("DROP TABLE TEST19");
			    m_conn.commit();
			}
			catch(Exception e) {} 
			finally {
			    try { if (rs != null) rs.close(); } catch (Exception e) {};
			    try { if (s != null) s.close(); } catch (Exception e) {};
			    try { if (pstmt != null) pstmt.close(); } catch (Exception e) {};
			    try { if (m_conn != null) m_conn.close(); } catch (Exception e) {};
			}
	    } 
	    catch (Exception e) {
	    	e.printStackTrace();
			System.exit(1);
		}

    	System.out.println("JDBCDemo Successfully Completed");
    }
}
