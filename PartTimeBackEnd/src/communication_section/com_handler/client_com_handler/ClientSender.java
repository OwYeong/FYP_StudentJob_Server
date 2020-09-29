package communication_section.com_handler.client_com_handler;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import communication_section.com_handler.ClientHandler;
import communication_section.communicate_object.ComObject;

public class ClientSender extends Thread{
	
	private ClientHandler ch;
	
	private OutputStream os;
    private ObjectOutputStream oos;

    public ComObject outgoingObj;

    public ClientSender(OutputStream os, ClientHandler ch){

        this.os = os;
        this.ch = ch;
        
        try {
            oos = new ObjectOutputStream(this.os);
            System.out.println("ObjectOutputStream initialize success!");
        } catch (IOException e) {
        	System.out.println("ObjectOutputStream initialize error!");
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
       	
    	
    	try {
    		
    		while(true) {
		    	waitForObject();// this is a infinite loop that check for object every 0.5s --> loop is break when there a object to sent 
		    	
		    	sentObject();
		    	
		    	this.outgoingObj = null;// reset
		    			    	
    		}
    	}catch(InterruptedException ie) {
    		System.out.println("Client sender Thread Interrupted, this thread will be shutdown.");
    		
    	}catch(Exception e) {
    		System.out.println("Client sender error, connection");
    		ch.terminateSocketConnection();
    		e.printStackTrace();
    	}
        
    }
    
    public void waitForObject() throws InterruptedException{
    	boolean hasObjectOrNot = false;
    	
    	while(hasObjectOrNot == false) {
    		
    		hasObjectOrNot = checkForObject();
    		
    		if(hasObjectOrNot == false) {
    			//if no object, make thread sleep
    			Thread.sleep(500);//sleep 0.5s
    		}
    		
    	}
    }
    
    public boolean checkForObject() {
    	boolean result = false;
    	
    	if(outgoingObj != null) {
    		//mean got object need to be sent
    		System.out.println("Object found");
    		result = true;
    	}else {
    		//mean no object found
    		//System.out.println("NO Object found");
    	}
    	
    	return result;
    }

    public void addObjectForSent(ComObject cObj) {
    	//add object to sent
    	
    	this.outgoingObj = cObj;
    }
    
    public void sentObject(){
    	try {
                
        System.out.println("Preparing to send outgoingObj: ");
        oos.writeObject(outgoingObj);
        oos.flush();
        
       
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }
	
}
