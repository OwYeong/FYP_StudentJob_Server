package communication_section.com_handler.client_com_handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.SocketException;

import communication_section.com_handler.ClientHandler;
import communication_section.communicate_object.ComObject;


public class ClientReceiver extends Thread{
	
		
	public ClientHandler ch;
	
	private InputStream is;
	private ObjectInputStream ois;
	
	// variable prepared to receive
    public ComObject incomingObj;

    private String receivedType;
    private String receivedTitle;


    	
	public ClientReceiver(InputStream is, ClientHandler ch) {
		
		this.is = is;
		this.ch = ch;
		
		
		try {
	        ois = new ObjectInputStream(new BufferedInputStream(is));
	        System.out.println("ObjectInputStream initialize success! for : " + is);

	    } catch (IOException e) {
	    	System.out.println("ObjectInputStream initialize error!");
	        e.printStackTrace();
	    }
	}
	
	@Override
	public void run() {
		
		try {
			System.out.println("Client receiver Starts");
            while (true) {

                incomingObj = (ComObject) ois.readObject();

                receivedType = incomingObj.getType();
                receivedTitle = incomingObj.getTitle();

 
                System.out.println("Receive Type :" + incomingObj.getType() + " Receive Title Name: " + receivedTitle);

                                     
                if (receivedType.equals("ClientRequest")){
                    //if the Incoming Object is a Client Request
                	//Create a new Thread --> Then Execute the ClientHandler HandleClientRequest() Function
                	/** Note : Thread will be close automatically after executing the HandleClientRequest() Function */
                	
                   	new Thread(new Runnable() {          
                   		
                   		//IncomingObj will be reset instantly at the end of the loop, Therefore save the Value first
                   		ComObject currentIncomingObj = incomingObj;//save the value of current ComObj 
                   		
                   		@Override
                   		public void run() {
                   			ch.handleClientRequest(currentIncomingObj);
                   		}
                   	}).start();
                   	
                   	System.out.println("incoming object type : " + incomingObj.getType());
                       

                }else if ( receivedType.equals("ClientReply")){
                	//if the Incoming Object is a Client Reply
                	//Create a new Thread --> Then Execute the ClientHandler HandleClientReply() Function
                	/** Note : Thread will be close automatically after executing the HandleClientReply() Function */
                	
                	new Thread(new Runnable() {   
                		
                		//IncomingObj will be reset instantly at the end of the loop, Therefore save the Value first
                		ComObject currentIncomingObj = incomingObj;//save the value of current ComObj
                		
                   		@Override
                   		public void run() {
                   			ch.handleClientReply(currentIncomingObj);
                   		}
                   	}).start();
                    
                	System.out.println("incoming object type : " + incomingObj.getType());
                    
                }else{
                    System.out.println("Please Check ReceivedType --> Not match : " + receivedType);
                }

                
                //reset at the end of the loop
                resetIncomingObj();
                

            }
        }catch (EOFException e){        	
        	// if EOF Exception Error Occur 
        	// Means the socket between Server and client is disconnected
        	// Leading the ois.readObject() function throw EOF error;
        	
        	ch.terminateSocketConnection();
        	
            
        }catch(SocketException se) {
        	
        	ch.terminateSocketConnection();
        }catch(Exception e) {
        	ch.terminateSocketConnection();
        	e.printStackTrace();
        }
		
	}
	
	private void resetIncomingObj() {
		incomingObj = null;
		receivedType = null;
		receivedTitle = null;
	}
	
	

}
