package communication_section;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import communication_section.com_handler.ClientHandler;
import gui.MainScreen;

public class ServerCore {
	
	//Global Variable
	public static Boolean serverStatus = false;
	private static ArrayList<ClientInfo> onlineClientInfoArrayList = new ArrayList<>(); 
	
	
	
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
        MainScreen.addLogText("Server Started accepting connection From Port 2888");
          
        
        // SECTION : Accepting Client Connection
        while (serverStatus == true)// if server is started  
        { 
        	
        	
        	System.out.println("Server Started accepting connection");
        	
            Socket s = null; 
              
            try 
            { 
                // socket object to receive incoming client requests 
                s = ss.accept(); //accept any incoming request From port 2888
                s.setTcpNoDelay(true);
                  
                // Loop will pause here IF ss.accept() does'nt receive any socket connection request
                
                /* When Connection received */
                
                System.out.println("A new client is connected : " + s);
                MainScreen.addLogText("A new client is connected : " + s);
                  
                
                System.out.println("Assigning new thread for this client"); 
                MainScreen.addLogText("Assigning new thread for this client");
                
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
	
	public static void addOnlineClientIntoArrayList(String clientUsername, ClientHandler clientHandler ) {
		//if an client is online, we will add it client info into the arraylist
		System.out.println("addOnlineClientIntoArrayList performing");
		
		ClientInfo onlineClient = new ClientInfo(clientUsername, clientHandler);
		
		onlineClientInfoArrayList.add(onlineClient);
		
		
		
	}
	
	public static void removeOnlineClientIntoArrayList(String clientUsername) {
		//if an client is gone offline, we will remove it client info from the arraylist
		
		System.out.println("removeOnlineClientIntoArrayList performing");
		
		
		
		for(int i = 0; i < onlineClientInfoArrayList.size(); i++ ) {
			ClientInfo currentOnlineClientInfoInLoop = onlineClientInfoArrayList.get(i);
			
			if(currentOnlineClientInfoInLoop.getClientUsername().equals(clientUsername)) {
				System.out.println("Successfully remove Client: " + clientUsername + "from online arraylist");
				onlineClientInfoArrayList.remove(i);
			}
			
		}
		
		
		
	}
	
	public static boolean checkClientOnlineOrNot(String clientUsername) {
		//this method is used to check whether an client is currenly Online
		//if an client is currenly online in a device, we will decline the connection
		//only one device is allow to connect to the server For each Client account.
		
		System.out.println("checkClientOnlineOrNot performing");
		
		boolean onlineOrNot = false;
		
		
		
		for(int i = 0; i < onlineClientInfoArrayList.size(); i++ ) {
			ClientInfo currentOnlineClientInfoInLoop = onlineClientInfoArrayList.get(i);
			
			if(currentOnlineClientInfoInLoop.getClientUsername().equals(clientUsername)) {
				System.out.println("The client :  " + clientUsername + "is online.");
				onlineOrNot = true;
			}
			
		}
		
		return onlineOrNot;				
		
	}
	
	public static ClientInfo getClientInfoFromArrayList(String clientUsername) {
		//this method is used to get an ClientInfo that is Currently Online
		
		System.out.println("getClientInfoFromArrayList performing");
		
		ClientInfo onlineClientInfo = null;
		
		
		
		for(int i = 0; i < onlineClientInfoArrayList.size(); i++ ) {
			ClientInfo currentOnlineClientInfoInLoop = onlineClientInfoArrayList.get(i);
			
			if(currentOnlineClientInfoInLoop.getClientUsername().equals(clientUsername)) {
				System.out.println("The client :  " + clientUsername + "is online.");
				onlineClientInfo = currentOnlineClientInfoInLoop;
			}
			
		}
		
		return onlineClientInfo;				
		
	}
	
	

	
}
