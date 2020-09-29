package communication_section.com_handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import communication_section.com_handler.client_com_handler.ClientReceiver;
import communication_section.com_handler.client_com_handler.ClientSender;
import communication_section.communicate_object.ComObject;
import database.DatabaseController;
import database.variable.Query;
import database.variable.Table;

public class ClientHandler implements Runnable {
	
	private Socket s;
	private InputStream is;
	private OutputStream os;
		
	private Thread clientReceiverThread;
	private Thread clientSenderThread;
	
	private ClientSender cs;
	private ClientReceiver cr;
	
	private ComObject outgoingObj;
	private ComObject incomingObj;
	
    private String receivedTitle;
    private String[] receivedStringContent;
    private Table[] receivedTableContent;
    private Query[] receivedQueryContent;
	
	private String[] relatedStringContent;
	private Table[] relatedTableContent;
	private Query[] relatedQueryContent;
	
	public ClientHandler(Socket s) {
		
		this.s = s;// get the socket
		
			
	}

	@Override
	public void run() {
		
		try {
			//socketStreamInitialize();
			//Data in/out Stream initialize		
			
			is = s.getInputStream();
			os = s.getOutputStream();
			
					
			
			System.out.println("ClientHandler IO Stream initialize done for " + s );
			
			System.out.println("Assigning a Sender and receiver thread for " + s );
			
			
			
			
			cs = new ClientSender(os, this);
			clientSenderThread = new Thread(cs);
			
			clientSenderThread.start();
			
			cr = new ClientReceiver(is, this);//pass inputStream -> for listening packet and ClientHandler -> for Using handleClientRequest() method to process listened request
			clientReceiverThread = new Thread(cr);
			
			clientReceiverThread.start();
						
			
			//Object in/out Stream initialize
			//Thread.sleep(2000);
			System.out.println("send to client now ");
			
			//notify client that socket is connected
			relatedStringContent = new String[]{"Hi, Connection To Server Accepted"};
			outgoingObj = new ComObject("ServerRequest", "Greet", relatedStringContent);
			
			cs.addObjectForSent(outgoingObj);//sent the notify out
			outgoingObj = null;
			
			
			
			
			
			
			
			//Client Handler Thread Finish work
			//Client Handler Thread close	
			
			
		}catch(Exception e) {
			System.out.println("ClientHandler for " + s + "Stream init Error" );
			e.printStackTrace();
		}
								
	}
	
	public void terminateSocketConnection() {
		try {
			System.out.println("Connection disconnected");
			System.out.println("Terminate the connection for " + s);
			
			//interrupt thread to shut it down
			if (clientSenderThread.isAlive()) {
				cs.interrupt();
			}
			if(clientReceiverThread.isAlive()) {
				cr.interrupt();
			}
			
								
			close();//close the socket
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	private void close() throws IOException{
		
		
		is.close();
		os.close();
		s.close();
	}
	
	//all relate request function
	public void handleClientRequest(ComObject cObj ) {
		System.out.println("handling request");
		
		extractComObj(cObj);
		
		switch(receivedTitle){
			case "login":
				//Client Request to LOGIN
				outgoingObj = loginRequest();
				break;
				
		}
		
		
		//make reply
        if (outgoingObj != null) {
        	
        	System.out.println("Preparing to sent obj -> type: " + outgoingObj.getType() +  " , request " +  outgoingObj.getTitle() + " string " + outgoingObj.getRelatedStringContent()[0]);
            cs.addObjectForSent(outgoingObj);//add object in the ConnectionSender Queue
        }else {
        	System.out.println("Please Check outgoingObject ! declare before sent. ");
        }
		
        resetIncomingObj();
		
		
	}
	
	public void handleClientReply(ComObject cObj ) {
		System.out.println("handling Client Reply");
		
		extractComObj(cObj);
		
		switch (receivedTitle) {
        	case "Greet":
        		System.out.println("Client Reply for " + receivedTitle + " is : " + receivedStringContent[0]);



		}
		
		
        resetIncomingObj();
        resetOutgoingObj();
		
		
	}
	
	private void extractComObj(ComObject cObj) {
		
		incomingObj = cObj;
		receivedTitle = cObj.getTitle();
		
		//get the received content if they are not null
        if(!(incomingObj.checkEmptyOrNot("relatedStringContent")))
            receivedStringContent = incomingObj.getRelatedStringContent();
        if(!(incomingObj.checkEmptyOrNot("relatedTableContent")))
            receivedTableContent = incomingObj.getRelatedTableContent();
        if(!(incomingObj.checkEmptyOrNot("relatedQueryContent")))
            receivedQueryContent = incomingObj.getRelatedQueryContent();
        

	}
	
	private void resetIncomingObj() {
		
		//reset all receiving variable

        incomingObj = null;
        receivedTitle = null;
        receivedStringContent = null;
        receivedTableContent = null;
        receivedQueryContent = null;
        

	}
	
	private void resetOutgoingObj() {
		
		//reset all sending variable

		outgoingObj = null;
		relatedStringContent = null;
		relatedTableContent = null;
		relatedQueryContent = null;
        

	}
	
	private ComObject loginRequest() {
						
		System.out.println("login function performing");
		
		Table u = DatabaseController.getTable("user");
		String[] searchColumn = {"name", "password"};
		String[] dataToBeCompared = {receivedStringContent[0], receivedStringContent[1]};
		String[][] resultRow = u.searchInTable(searchColumn, dataToBeCompared);
		
		if(resultRow.length >= 1) {
			
			relatedStringContent = new String[] {"Account Verifyed! DONE"};
			
			
		}else {
			relatedStringContent = new String[] {"Wrong username or password"};
		}
		
		
		ComObject replyObj = new ComObject("ServerReply", "login", relatedStringContent);
		
		return replyObj;
		
		
		
	}
	
	
	
}
