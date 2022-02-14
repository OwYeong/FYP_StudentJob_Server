package communication_section.communicate_object;

import java.io.Serializable;

import database.variable.Query;
import database.variable.Table;

public class ComObject implements Serializable{
	/* this class is created for inserting all relevant info into this object --> 
	 * This object can be a Server Reply or Client Request
	 * 
	 * Used for communication between Server and the client --> sending, receiving through a socket
	 */
	
	
	/**
 	Note: sending Serializable object through socket
 	
 	both class must have the same 
 	1) same serialVersionUID
 	2) same package name
 	3) same class name
 	
 	Otherwise, It causes error when deserializing the object
	 */
	
	private static final long serialVersionUID = 1L;
	
	private String type;//4 main type: Server Reply, Server Request, Client Request, Client Reply
	
	private String title;//Mainly used to store: Request Function Name, Reply Action Name
	private String[] relatedStringContent;//Mainly used to store relevant String content for performing the request/reply --> Login Function Ex: username,password
	private Table[] relatedTableContent;//Mainly used to store relevant Table content
	private Query[] relatedQueryContent;//Mainly used to store relevant Query content
	
	/**SECTION : Object Constructor*/
	/** All Array/ Variable will be initialize. 
	 * If the relatedContentArray is not used (Will be assign a Value -> null)
	 * Give a default value null. so can be easily Track later on*/
	public ComObject(String type, String title, String[] relatedStringContent, Table[] relatedTableContent, Query[] relatedQueryContent) {
		//Used to construct Communication Object Which have all type of Related content
		this.type = type;
		this.title = title;
		this.relatedStringContent = relatedStringContent;
		this.relatedTableContent = relatedTableContent;
		this.relatedQueryContent = relatedQueryContent;
	}
	
	public ComObject(String type, String title, String[] relatedStringContent){
		//Used to construct Communication Object Which have only type of String Related content
		this.type = type;
		this.title = title;
		this.relatedStringContent = relatedStringContent;
		this.relatedTableContent = null;
		this.relatedQueryContent = null;
	}
	
	public ComObject(String type, String title, Table[] relatedTableContent){
		//Used to construct Communication Object Which have only type of Table Related content
		this.type = type;
		this.title = title;
		this.relatedStringContent = null;
		this.relatedTableContent = relatedTableContent;
		this.relatedQueryContent = null;
		
	}
	
	public ComObject(String type, String title, Query[] relatedQueryContent){
		//Used to construct Communication Object Which have only type of Query Related content
		this.type = type;
		this.title = title;
		this.relatedStringContent = null;
		this.relatedTableContent = null;
		this.relatedQueryContent = relatedQueryContent;
		
		
	}
	
	public ComObject(String type, String title, String[] relatedStringContent, Table[] relatedTableContent){
		//Used to construct Communication Object Which have two type of Related content -> string and Table
		this.type = type;
		this.title = title;
		this.relatedStringContent = relatedStringContent;
		this.relatedTableContent = relatedTableContent;
		this.relatedQueryContent = null;
		
	}
	
	public ComObject(String type, String title, String[] relatedStringContent, Query[] relatedQueryContent){
		//Used to construct Communication Object Which have two type of Related content -> string and Query
		this.type = type;
		this.title = title;
		this.relatedStringContent = relatedStringContent;
		this.relatedTableContent = null;
		this.relatedQueryContent = relatedQueryContent;
		
	}
	
	public ComObject(String type, String title){
        //Used to construct Communication ComObject Which have no Related Content
        this.type = type;
        this.title = title;
        this.relatedStringContent = null;
		this.relatedTableContent = null;
		this.relatedQueryContent = null;

    }
	
	/**SECTION END : Object Constructor*/
	
	/**SECTION : Object Getter*/
	public String getType() {
		return type;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String[] getRelatedStringContent() {
		return relatedStringContent;
	}
	
	public Table[] getRelatedTableContent() {
		return relatedTableContent;
	}
	
	public Query[] getRelatedQueryContent() {
		return relatedQueryContent;
	}
	
	/**SECTION END: Object Getter*/
	
	public boolean checkEmptyOrNot(String relatedContentName) {
		//used to check which relatedContent is null
		boolean result = false;//used to store result
		
		switch(relatedContentName) {
			case "relatedStringContent":
				if(relatedStringContent == null) {
					//mean its empty
					result = true;
				}else {
					result = false;			
				}
				break;
								
			case "relatedTableContent":
				if(relatedTableContent == null) {
					//mean its empty
					result = true;
				}else {
					result = false;
				}
				break;
				
			case "relatedQueryContent":
				if(relatedQueryContent == null) {
					//mean its empty
					result = true;
				}else {
					result = false;
				}
				break;
			default:
				System.out.println("Error! relatedContentName not Found! ");
								
		}
		
		return result;
		
	}
	
	
}
