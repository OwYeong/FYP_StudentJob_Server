package communication_section;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import communication_section.com_handler.ClientHandler;

public class ServerCore {
	
	//Global Variable
	public static Boolean serverStatus = false;
	Thread myThread;
	
	
	public static void startAcceptRequest() throws IOException
    { 
		
		/* Note : this Class is create mainly for Accepting Client Connection
		 *  & and assigning a new Thread for handling the Client Request
		 */
		serverStatus = true;
        
        ServerSocket ss = new ServerSocket(2888); //2888 indicate The Port that my Server listening
        /* Note : In order for Server to receive the network Packet,
         * 
         * 		  Client Device Sent in the following Condition:
         * 		  -> Destination IP: My Server IP
         * 		  -> Port: 2888 ..... as my Server is listening on port 2888
         * 			
         * */
          
        
        // SECTION : Accepting Client Connection
        while (serverStatus == true)// if server is started  
        { 
        	
        	
        	System.out.println("Server Started accepting connection");
            Socket s = null; 
              
            try 
            { 
                // socket object to receive incoming client requests 
                s = ss.accept(); //accept any incoming request From port 2888
                  
                // Loop will pause here IF ss.accept() does'nt receive any socket connection request
                
                /* When Connection received */
                
                System.out.println("A new client is connected : " + s); 
                  
                
                System.out.println("Assigning new thread for this client"); 
  
                ClientHandler ch = new ClientHandler(s);// create a new CLient Handler Object
                
                // create a new thread to run the ClientHandler object
                Thread t = new Thread(ch);
                                
                // Invoking the start() method 
                t.start(); 
                
                
                  
            } 
            catch (Exception e){ 
                s.close(); 
                e.printStackTrace(); 
            } 
        } 
    } 

	
}
