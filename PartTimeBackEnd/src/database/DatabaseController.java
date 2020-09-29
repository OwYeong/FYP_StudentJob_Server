package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import database.variable.Query;
import database.variable.Table;
import sql_connection_core.MySqlConnector;

public class DatabaseController {
	
	/*Note: This Class is Create to provide a full control on the database
	 * 		EX: (edit, delete , add) Data in Table
	 * */
	
	
	//Global Variable for all function
	private static Connection connection;
	private static Statement statement;
	private static ResultSet resultSet;
	
	
	public static void initialize() {
		
		//For DatabaseController to Work, These variable should be declared.
		//In this case, I would get it from MySqlConnector as it create connection to SQL(using JDBC Connector)
		
		try {
		
		connection = MySqlConnector.connection;//get the connection to DATABASE
		
		
		System.out.println("DatabaseController Successfully Initialize");
		
		}catch(Exception e) {
			
			System.out.println("DatabaseController Initialize Error");
			e.printStackTrace();
		}
		
		
		
	}
	
	public static Table getTable(String requestedTableName){
		  Table t = null;
		  try {
			  
			  /* Note: throws SQLException means if this particular function is call somewhere
			   *  and it contain ERROR, this functions will throws out the error
			   *  
			   *  So that place where the function is called could catch the exception and print it out.
			   *  
			   *  * if function dint not throws out the exception, ended up it will cause exception unhandled.
			   * */
			  
			  // Statements allow to issue SQL queries to the database
		      statement = connection.createStatement();
		      
		      // Result set get the result of the SQL query
		      resultSet = statement
		          .executeQuery("select * from " + requestedTableName); //execute the statement
			  
				
			    
			  int totalTableColumnNum = resultSet.getMetaData().getColumnCount();// get total column, the table have
			  String tableName = resultSet.getMetaData().getTableName(1);//get table name
			  ArrayList<String> result = new ArrayList<String>();
			  ArrayList<String> columnName = new ArrayList<String>();
			  
			  
			 
			
			  //arrayList start from 0
			  columnName.add(null);//replace array[0]
			  result.add(null);//replace array[0]
			
			  System.out.println(tableName + " has " + totalTableColumnNum + " column");
			  
			
			  //SECTION: Get Column name for the table 
			  for  (int i = 1; i<= totalTableColumnNum; i++)
			  {
				  columnName.add(resultSet.getMetaData().getColumnName(i));
				  
				  System.out.println("Column " + i  + " "+ columnName.get(i));
				  
				  
			  }
			
			  
			  //SECTION: Get Data inside TAble 
							  
			  int totalResultRowNum;
			  
			  resultSet.last();
			  totalResultRowNum = resultSet.getRow();
			  
			  
			  String[][] dataInTable = new String[totalResultRowNum + 1][totalTableColumnNum + 1];
			  
			  /* Note: array declaration + 1 --> because I want array start from 1
			   * 	which means array[0][0] will be ignored
			   * 	
			   * 	
			   *   */
			  
			  resultSet.beforeFirst();
			  
			  int resultInRow = 1;//counter for query result --> identify the row that is currently going on
			
			  while (resultSet.next()) {
				  
				  //query result start from row 1
				  //while query result still contain data
				  
		    	
				  
		    	
		    	
				  for(int resultInColumn = 1; resultInColumn <= totalTableColumnNum; resultInColumn++ ) {
		    		
		    		
		    			    			
					  result.add( resultInColumn , resultSet.getString(resultInColumn));// get result from inside all column and store in a array	
					  
					  dataInTable[resultInRow][resultInColumn] = result.get(resultInColumn);
		    		
					  System.out.print(columnName.get(resultInColumn) + ": " + result.get(resultInColumn) + ", ");
				  }
		    	
				  System.out.print("\n");//enter down
				  
		    	
				  resultInRow++;//update row
		     
		    }
			
			t = new Table(tableName, (int)totalTableColumnNum, columnName, dataInTable);
			
			
			  
			  
		}catch(Exception e) {
			System.out.println("Table not found in databases");
			e.printStackTrace();
		}
		  
		System.out.println("Succesfully Return Table");
		  
		return t;
		  
		  
		  
	}
	
	public static Query getQuery(String[] select, String[] table, String[] condition) {
		
		  Query q = null;
		
		  try {
			  
			  /* Note: throws SQLException means if this particular function is call somewhere
			   *  and it contain ERROR, this functions will throws out the error
			   *  
			   *  So that place where the function is called could catch the exception and print it out.
			   *  
			   *  * if function dint not throws out the exception, ended up it will cause exception unhandled.
			   * */
			  
			  // Statements allow to issue SQL queries to the database
		      statement = connection.createStatement();
		      
		      //SECTION: generating SELECT -> Attribute <- into the statement
		      String generateStatement = "SELECT ";//intial statement --> will be added more later on
		      
		      for(int loop = 0; loop < select.length; loop++ ) {
		    	  
		    	  if(loop != 0) {
		    		  //if not first Select attribute
		    		  
		    		  generateStatement += ", ";// add , after each SELECT entered
		    	  }
		    	  
		    	  generateStatement += select[loop]; //add each SQL select -> ATTRIBUTE <- into the statement 	   	  
		    	  
		      }
		      
		      //SECTION: generating FROM -> TABLE <- into the statement
		      
		      generateStatement += " FROM ";
		      
		      for(int loop = 0; loop < table.length; loop++ ) {
		    	  
		    	  if(loop != 0) {
		    		  //if not first Select attribute
		    		  
		    		  generateStatement += ", ";// add , after each SELECT entered
		    	  }
		    	  
		    	  generateStatement += table[loop]; //add each SQL from -> TABLE <- into the statement 	   	  
		    	  
		      }
		      
		      //SECTION: generating WHERE -> CONDITION <- into the statement
		      
		      generateStatement += " WHERE ";
		      
		      for(int loop = 0; loop < condition.length; loop++ ) {
		    	  
		    	  if(loop != 0) {
		    		  //Ignore 1st condition
		    		  
		    		  generateStatement += " AND ";// add AND(SQL keyword) after Each condition
		    	  }
		    	  
		    	  generateStatement += condition[loop]; //add each SQL WHERE -> CONDITION <- into the statement 	   	  
		    	  
		      }
		      
		      System.out.println(generateStatement);// check whole QUery
		      
		      	      
		      
		      // Result set get the result of the SQL query		 
		      resultSet = statement
		          .executeQuery(generateStatement); //execute the statement
			  
				
			  //SECTION: Getting Result of the Query USING (ResultSet)
		      
			  int totalQueryColumnNum = resultSet.getMetaData().getColumnCount();// get total column, the QUERY have
			  String[] tableInQuery = table;
			  ArrayList<String> result = new ArrayList<String>();
			  ArrayList<String> columnName = new ArrayList<String>();
			  
			  
			 
			
			  //arrayList start from 0
			  columnName.add(null);//replace array[0]
			  result.add(null);//replace array[0]
			
			  //System.out.println("This Query has " + totalQueryColumnNum + " column");
			  
			
			  //SECTION: Get Column name for the QUERY
			  for  (int i = 1; i<= totalQueryColumnNum; i++)
			  {
				  columnName.add(resultSet.getMetaData().getColumnName(i));
				  
				  //System.out.println("Column " + i  + " "+ columnName.get(i));
				  
				  
			  }
			
			  
			  //SECTION: Get Data inside QUERY
							  
			  int totalResultRowNum;
			  
			  resultSet.last();//set the result to last --> so can use to find the last row number
			  totalResultRowNum = resultSet.getRow();// get how many row of result this QUERY contain
			  
			  /* Getting (how many row of result) and (how many column of result)
			   * help us to indicate the size of the DATA ARRAY should contain
			   * 							|
			   * 							v
			   */
			  
			  String[][] dataInQuery = new String[totalResultRowNum + 1][totalQueryColumnNum + 1];
			  
			  
			  /* Note: array declaration + 1 --> because I want array start from 1
			   * 	which means array[0][0] will be ignored
			   * 	
			   * 	
			   *   */
			  
			  resultSet.beforeFirst();//set it back to 1st, After Getting the number of lastrow
			  
			  int resultInRow = 1;//counter for query result --> identify the row that is currently going on
			
			  while (resultSet.next()) { //while query result still contain data
				  
				  //query result start from row 1
				 
				  
		    	
				  
		    	
		    	
				  for(int resultInColumn = 1; resultInColumn <= totalQueryColumnNum; resultInColumn++ ) {
		    		
		    		
		    			    			
					  result.add( resultInColumn , resultSet.getString(resultInColumn));// get result from inside all column and store in a array	
					  
					  dataInQuery[resultInRow][resultInColumn] = result.get(resultInColumn);
		    		
					  //System.out.print(columnName.get(resultInColumn) + ": " + result.get(resultInColumn) + ", ");
				  }
		    	
				  //System.out.print("\n");//enter down
				  
		    	
				  resultInRow++;//update row
		     
		    }
			
			q = new Query(tableInQuery, (int)totalQueryColumnNum, columnName, dataInQuery);
			
			
			
			
			  
			  
		}catch(Exception e) {
			System.out.println("Query not found in databases");
			e.printStackTrace();
		}
		  
		System.out.println("Succesfully Return Query");
		  
		return q;
		
	}
	
	public static void insertDataIntoTable(String tableName, String[] values ) {
		try {
			
			statement = connection
					.createStatement();
			
			String generateStatement = "INSERT INTO " + tableName + " VALUES ";
			
			//generating VALUES into Statement
			//Since VALUES is stored in an ARRAY
			for(int loop = 0; loop < values.length; loop++ ) {
				
				if(loop != 0 ) {
					generateStatement += ", ";// add , after each VALUES entered
				}
				
				generateStatement += values[loop];// add the data after VALUES --> (data, data, data, data)
				
			}
			
			System.out.println(generateStatement);// check whether is correct
			
			
			statement.executeUpdate(generateStatement);// execute statement
			
			System.out.println("DatabaseController Successfully Insert DATA Into " + tableName );
		
		}catch(SQLException e) {
			
			System.out.println("DatabaseController INSERT error" );
			
			e.printStackTrace();
		}
		
	}
	
	
	public static void deleteDataFromTable(String tableName, String condition ) {
		
		try {
		statement = connection
				.createStatement();
		
		//Statement check Before Execute
		System.out.println("DELETE FROM " + tableName + " WHERE " + condition);
		
		statement.executeUpdate("DELETE FROM " + tableName + " WHERE " + condition);
		
		
		System.out.println("DatabaseController Successfully DELETED DATA FROM " + tableName );
		
		
		}catch(SQLException e) {
			
			System.out.println("DatabaseController DELETE error");
			e.printStackTrace();
		}
		
		
		
	}
	
	public static void updateDataFromTable(String tableName, String[] set, String condition) {
		
		try {
			statement = connection
					.createStatement();
			
			//Statement check Before Execute				
			String generateStatement = "UPDATE " + tableName + " SET ";
			
			//generating VALUES into Statement
			//Since VALUES is stored in an ARRAY
			for(int loop = 0; loop < set.length; loop++ ) {
				
				if(loop != 0 ) {
					//if it is not first loop
					
					generateStatement += ", ";// add , after each VALUES entered
				}
				
				generateStatement += set[loop];// add the data after VALUES --> (data, data, data, data)
				
			}
			
			generateStatement += " WHERE " + condition;
			
			System.out.println(generateStatement);//check
			
			statement.executeUpdate(generateStatement);
			
			
			System.out.println("DatabaseController Successfully UPDATE DATA FROM " + tableName );
			
			
			}catch(SQLException e) {
				
				System.out.println("DatabaseController UPDATE error");
				e.printStackTrace();
			}
		
	}
	
	//use to close databasecontroller when program exit.
	public static void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	 

}
