package communication_section;

import communication_section.com_handler.ClientHandler;

public class ClientInfo {
	/* This info class Model. Is used to store client information.
	 * The Purpose of storing client information is that:
	 * 
	 * -We want to keep track of the client who are currently online
	 * -With this, If an client received a Job Request and he/she is currently online
	 * -Server Will sent updates to the client device
	 * -so They could get The lastest data From time to time(Real-Time update)*/
	
	
	//no setter is need for private variable.
	//If an client gone offline, Their ClientInfo Object will get destroy
	
	private String clientUsername;
	private ClientHandler clientHandler;
	
	public ClientInfo(String clientUsername, ClientHandler clientHandler){
		this.clientUsername = clientUsername;
		this.clientHandler = clientHandler;
		
	}
	

	public String getClientUsername() {
		return clientUsername;
	}

	public ClientHandler getClientHandler() {
		return clientHandler;
	}

	

}
