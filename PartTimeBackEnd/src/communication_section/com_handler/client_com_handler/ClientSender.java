package communication_section.com_handler.client_com_handler;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import communication_section.com_handler.ClientHandler;
import communication_section.communicate_object.ComObject;

public class ClientSender extends Thread{
	
	private ClientHandler ch;
	
	private OutputStream os;
    private ObjectOutputStream oos;


    private ComObject outgoingObjFromQueue;

    
    private Queue<ComObject> outgoingObjQueueList = new LinkedList<>();

    public ClientSender(OutputStream os, ClientHandler ch){

        this.os = os;
        this.ch = ch;
        
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(os));
            oos.flush();
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
		    	
		    	//outgoingObjQueueList.poll() get a object in the first queue and remove it ... It return null if the queue is empty
		    	outgoingObjFromQueue = outgoingObjQueueList.poll();//get first object in queue
		    	System.out.println("Sending obj -> type: " + outgoingObjFromQueue.getType() +  " , request " +  outgoingObjFromQueue.getTitle());
		    	
		    	long start = System.currentTimeMillis();
		    	
		    	sentObject(outgoingObjFromQueue);
		    	
		    	long end = System.currentTimeMillis();
		    	
		    	System.out.println("That took " + (end-start) + "ms");
		    	
		    	this.outgoingObjFromQueue = null;// reset
		    			    	
    		}
    	}catch(InterruptedException ie) {
    		ch.terminateSocketConnection();
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
    		//this is a infinite loop that break when hasObjectOrNot == true
    		hasObjectOrNot = checkForObject();
    		
    		if(hasObjectOrNot == false) {
    			//if no object, make thread sleep
    			Thread.sleep(500);//sleep 0.5s
    		}
    		
    	}
    }
    
    public boolean checkForObject() {
    	boolean result = false;
    	
    	if(outgoingObjQueueList.isEmpty() == false) {
    		//mean got object need to be sent
    		System.out.println("Object found in outgoingObjQueueList");
    		result = true;
    	}else {
    		//mean no object found
    		//System.out.println("NO Object found");
    	}
    	
    	return result;
    }

    public void addObjectIntoSentQueue(ComObject cObj) {
    	//add object into queue
    	
    	
    	outgoingObjQueueList.add(cObj);//add into list
    }
    
    public void sentObject(ComObject myOutgoingObj){
    	try {
                      
        
        
        oos.writeObject(myOutgoingObj);
        oos.flush();
        
       
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }
	
}
