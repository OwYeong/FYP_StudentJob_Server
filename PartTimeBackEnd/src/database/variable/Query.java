package database.variable;

import java.io.Serializable;
import java.util.ArrayList;

public class Query implements Serializable {
	
	private static final long serialVersionUID = 2L;
	
	private String[] tableInQuery;
	private int totalQueryColumnNum;
	private ArrayList<String> columnName;
	private String[][] dataInQuery;
	
	public Query(String[] tableInQuery, int totalQueryColumnNum, ArrayList<String> columnName, String[][] dataInQuery) {
		this.tableInQuery = tableInQuery;
		this.totalQueryColumnNum = totalQueryColumnNum;
		this.columnName = columnName;
		this.dataInQuery = dataInQuery;
		
	}
	
	public void printQuery() {
		
		/* note: (dataInQuery.length - 1) because array 0 is empty
		 * 		 but .1ength include empty
		 *  */
		for(int i = 1; i <= (columnName.size() - 1); i++) {
			System.out.print(columnName.get(i) + " | ");
		}
		System.out.println("\nquery length " + dataInQuery.length + " nxt : " + dataInQuery[0].length );
		
		System.out.print("\n");
		
		
		
		for(int r = 1; r <= (dataInQuery.length - 1); r++ ) {
			
			System.out.println("Row" + r);
			
			for(int c = 1; c <= (dataInQuery[0].length - 1); c++) {
				
				System.out.print(" : " + dataInQuery[r][c] + " | ");
			}
			
			System.out.print("\n");
			
		}
		
	}

	public String[] getTableInQuery() {
		return tableInQuery;
	}

	public int getTotalQueryColumnNum() {
		return totalQueryColumnNum;
	}

	public ArrayList<String> getColumnName() {
		return columnName;
	}

	public String[][] getDataInQuery() {
		return dataInQuery;
	}
	
	public Boolean EmptyOrNot() {
		/* Note: this function is use to check whether the query result is empty */
		
		//-1 because I replace dataInQuery[0] with null
		//so have to deduct it out
		if ((dataInQuery.length - 1) == 0) {
			// if result table row is equal to 0 then its empty
			
			return true;
			
		}else {
			return false;
		}
		
	}

	


	

	
	

}
