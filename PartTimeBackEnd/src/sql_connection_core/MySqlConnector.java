package sql_connection_core;

import java.sql.Connection;
import java.sql.DriverManager;

import database.DatabaseController;


public class MySqlConnector {
	/* NOTE: This Class is used to Establish Connection with LOCAL MySQL Database */	
	
	public static Connection connection = null; 
	  
	final static private String host = "localhost:3306";// XAMPP host SQL location .... local host also known as 127.0.0.1
	final static private String user = "root";// Sql database Default username
	final static private String passwd = "";// Sql database Default password is null
	  
	  
	public static void connectDataBase(String databaseName) throws Exception{
		  
		
		// This will load the MySQL driver, each DB has its own driver
		Class.forName("com.mysql.jdbc.Driver");
      
		// Setup the connection with the DB
		connection = DriverManager
				.getConnection("jdbc:mysql://" + host + "/" + databaseName+ "?"
						+ "user=" + user + "&password=" + passwd );
      
		//no error occur
      
		System.out.println("Connection to Database Succesful");	
		
		//when connection to database successfully, initialize DataBaseContoller so it can help to control database
		DatabaseController.initialize();
	      
		
		  
	}
	  		  
		  

	// Use to close connection when program exit
	private static void close() {
		try {
	      
	    	if (connection != null) {
	    		connection.close();
	    	}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	    

}
