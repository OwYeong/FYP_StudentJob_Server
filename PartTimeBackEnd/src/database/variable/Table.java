package database.variable;

import java.util.ArrayList;

public class Table {
	
	
	private String tableName;
	private int totalTableColumnNum;
	private ArrayList<String> columnName;
	private String[][] dataInTable;
	
	public Table(String tableName, int totalTableColumnNum, ArrayList<String> columnName, String[][] dataInTable) {
		this.tableName = tableName;
		this.totalTableColumnNum = totalTableColumnNum;
		this.columnName = columnName;
		this.dataInTable = dataInTable;
		
	}
	
	public String[][] getDataInTable() {
		
		return dataInTable;
	}
	
	public void printTable() {
		
		/* note: (dataInTable.length - 1) because array 0 is empty
		 * 		 but .1ength include empty
		 *  */
		for(int r = 1; r <= (dataInTable.length - 1); r++ ) {
			
			System.out.println("Row" + r);
			
			for(int c = 1; c <= (dataInTable[0].length - 1); c++) {
				
				System.out.print(" : " + dataInTable[r][c] + " | ");
			}
			
			System.out.print("\n");
			
		}
		
	}
	
	public String[][] searchInTable ( String[] columnInSearch, String[]dataToBeCompared  ) {
		ArrayList<Integer> cis_InNum = new ArrayList<Integer>();//variable to store columnInSearch in number		
		Boolean cNameError = false;
		String[][] result = new String[0][0];// create a 2d array for saving the result
		
		ArrayList<Integer> matchedRow = new ArrayList<Integer>();// used to collect all matched row in the table
		
		
		//SECTION: converting table name into its column name code
		for(int loop = 0; loop < columnInSearch.length; loop++) {
			//start convert from the first one
			//loop for columnNameComparedWithData to convert it into number
			
			System.out.println("column SIZE : " + columnName.size());
			cis_InNum.add(loop, -1);//initialize the number to -1
			
			for( int i = 1; i <= columnName.size() - 1; i++) {
				//loop all the column Name in the table
				
				if (columnName.get(i).equals(columnInSearch[loop]) ) {
					//if the columnName in table MATCHED columnInSearch
					
					cis_InNum.set(loop, i);//replace the -1 with the ColumnInSearch in number
				}				
			}
			//after looping all the columnName in table
			
			
			if(cis_InNum.get(loop) == -1) {
				// if the cis_InNum is not being Replace
				// Means columnName Entered for searching, is not available in the table
				
				cNameError = true;// Indicate Error Occur
				
				System.out.println("Converting into Number Error in Column Entered: " + columnInSearch[loop] );
							
			}			
					
			
		}
		
		
		// if no error
		if(!cNameError) {
			System.out.println("size is: " + cis_InNum.size());
			
			for(int i = 0; i < cis_InNum.size(); i++) {
				System.out.println( i + "column in code is " + cis_InNum.get(i));
				
			}
			
			//SECTION: Begin Searching In Table
			for(int r = 1; r <= (dataInTable.length - 1); r++) {
				//start Search from table data ROW 1
				ArrayList<Boolean> matchedOrNot = new ArrayList<Boolean>();
							
				for(int i = 0; i < cis_InNum.size(); i++) {
					//start matching with the first Column we want to search
				
					for(int c = 1; c <= (dataInTable[0].length -1); c++) {
					//loop all data in all table columnName --> match with Column we want use to search
										
						if(cis_InNum.get(i) == c) {
							//if matched (column name in table) with (Column name we want use to search)
							
							//SECTION: Match the (data in the TABLE) With (data we want to search)
							if(dataInTable[r][c].equals(dataToBeCompared[i])) {		
								
								matchedOrNot.add(true);// take note for the search result true
							}else {
								
								matchedOrNot.add(false);// take note for the search result false
							}
						}
					}
																			
				}
				
				
				
				if(isAllResultMatched(matchedOrNot)) {
					//if all result is matched
					
					//save the data row for RETURN later on
					matchedRow.add(r);
				}				
				
				//Check the next ROW				
			}
			
			
			
			if(!matchedRow.isEmpty()) {
				System.out.println(matchedRow.size() + " ROW MATCHED");
				System.out.print("Matched row is : ");
				for(int i = 0; i < matchedRow.size(); i++) {
					
					System.out.print(matchedRow.get(i) + ", ");
					
				}
				System.out.print("\n");
				
				result = new String[matchedRow.size() + 1][columnName.size()];
				
				//SECTION : STORING MATCHED ROW INTO A NEW RESULT[ARRAY]
				
				//store column name INTO RESULT row number 0
				for(int loop=1;loop < columnName.size();loop++) {
					result[0][loop] = columnName.get(loop);//store column name
				}
				
				
				int dataRowToStore =1;
								
				//store Matched row into the new result ARRAY
				for(int row=1; row <= (dataInTable.length-1);row++ ) {
					
					boolean isRowNeeded = false;
					//for checking if the row we need
					for(int loop=0;loop < matchedRow.size();loop++) {
						if(row == matchedRow.get(loop) ) {
							isRowNeeded = true;
						}
					}
					
					//if needed add to our result
					if(isRowNeeded == true) {
						for(int col = 1; col <= (dataInTable[0].length - 1); col++) {
							result[dataRowToStore][col] = dataInTable[row][col];
						}
						dataRowToStore++;
					}
					
				}
				
				//SECTION : PRINT OUT FOR CHECKING
				System.out.println("The result of this search are: ");
				for(int r = 0; r < result.length ; r++) {
					
					for(int c = 1; c < result[0].length;c++) {
						System.out.print(result[r][c] + " | " );
					} 
					System.out.print("\n"); 
				}
				
				
				
			}else {
				System.out.println("No Result Matched");
			}
			
			
			
			
		}else {
			System.out.println("PLEASE CHECK the Column Name provided.");
		}
				
		//SECTION : RETURN THE RESULT
		return result;
	}
	
	private boolean isAllResultMatched(ArrayList<Boolean> b) {
		for(int i = 0; i < b.size(); i++) {
			
			System.out.print(b.get(i) + ", ");
			
			if (i == (b.size()-1)) {
				System.out.print("\n");
			}
		}
		
		for(int i = 0; i < b.size(); i++) {
			
			if(b.get(i) == false) {
				//if any boolean in arrayList is false then return false
				return false;
			}
			
		}
		
		return true;
	}
	
	public String getTableName() {
		
		return tableName;
	}

	public int getTotalTableColumnNum() {
		return totalTableColumnNum;
	}

	public ArrayList<String> getColumnName() {
		return columnName;
	}
	

	
	
	
	
	
	
}
