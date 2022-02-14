package communication_section.com_handler;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import javax.imageio.ImageIO;

import communication_section.ClientInfo;
import communication_section.ServerCore;
import communication_section.com_handler.client_com_handler.ClientReceiver;
import communication_section.com_handler.client_com_handler.ClientSender;
import communication_section.communicate_object.ComObject;
import communication_section.firebase_cloud_messaging_section.FirebaseCloudMessageController;
import communication_section.firebase_cloud_messaging_section.NotificationInfo;
import database.DatabaseController;
import database.variable.Query;
import database.variable.Table;
import gui.MainScreen;

public class ClientHandler implements Runnable {
	
	private Socket s;
	private InputStream is;
	private OutputStream os;
		
	private Thread clientReceiverThread;
	private Thread clientSenderThread;
	
	private ClientSender cs;
	private ClientReceiver cr;
	
	private String clientUsername;
	
		
	
	public ClientHandler(Socket s) {
		
		this.s = s;// get the socket
		
			
	}
	
	public ClientSender getClientSender() {
		return cs;
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
			String[] relatedStringContent = new String[]{"Hi, Connection To Server Accepted"};
			ComObject outgoingObj = new ComObject("ServerRequest", "Greet", relatedStringContent);
			
			cs.addObjectIntoSentQueue(outgoingObj);//sent the notify out
		
			
			
			
			
			
			
			
			
			
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
			if(clientUsername != null) {
				//mean client is logged in, and currently Online
				System.out.println("Terminate the connection for " + s + "Client Username: " + clientUsername);
			
				//if we terminate their connection, their reference in ServerCore OnlineClientArraylist should be removed
				ServerCore.removeOnlineClientIntoArrayList(clientUsername);
				
			}else {
				System.out.println("Terminate the connection for " + s);
			}
			
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
	/*Note: The Variable inside handleClientRequest(ComObject cObj ) is interupt when multiple thread use this function
	 * at the same time
	 * Therefore, synchronized Keyword is added. (Which limit the function to 1 thread per time)*/
	public synchronized void handleClientRequest(ComObject cObj ) {
		System.out.println("Handle Client Request Function Called");
		//create Variable that used to store a Communication Object(Which contain reply for their request)
		ComObject outgoingObj = null;// initialize a null value
		
		//create Variable to store information received from Client
		String receivedTitle = null;
	    String[] receivedStringContent = null;
	    Table[] receivedTableContent = null;
	    Query[] receivedQueryContent = null;
	    
		//extracting the Information from the Communication Object(send by client)		
		receivedTitle = cObj.getTitle();//get the title of the Client Communication Object(Ex: login, getAccountInfo, etc)
		
		/*Note: not all RelatedContentArray inside the Communication Object will be Used
		 * It depend on the request Needs
		 * RelatedContentArray(Not Used) will be assign a null value*/
		
		//get the received content if they are not null
        if(!(cObj.checkEmptyOrNot("relatedStringContent")))
            receivedStringContent = cObj.getRelatedStringContent();
        if(!(cObj.checkEmptyOrNot("relatedTableContent")))
            receivedTableContent = cObj.getRelatedTableContent();
        if(!(cObj.checkEmptyOrNot("relatedQueryContent")))
            receivedQueryContent = cObj.getRelatedQueryContent();
        
        
        
		
		switch(receivedTitle){
			case "updateTokenRequest":
				outgoingObj = updateTokenRequest(receivedTitle, receivedStringContent);
				
				break;
			case "deleteTokenRequest":
				outgoingObj = deleteTokenRequest(receivedTitle, receivedStringContent);
				
				break;
			case "LogOut":
				outgoingObj = logOutRequest(receivedTitle, receivedStringContent);
				break;
			case "login":
				//Client Request to LOGIN
				outgoingObj = loginRequest(receivedTitle, receivedStringContent);
				
				
				break;
			case "getStudentAccountInfo":
				//Client Request to get his Account Info
				outgoingObj = getStudentAccountInfoRequest(receivedTitle, receivedStringContent);
				
				break;
			case "getEmployerAccountInfo":
				outgoingObj = getEmployerAccountInfoRequest(receivedTitle, receivedStringContent);
				
				break;
			case "getEmployerMyPostInfo":
				outgoingObj = getEmployerMyPostInfoRequest(receivedTitle, receivedStringContent);
				break;
			case "getEmployerMyPostRequest":
				outgoingObj = getEmployerMyPostRequestRequest(receivedTitle, receivedStringContent);
				break;
			case "getStudentMyRequest":
				outgoingObj = getStudentMyRequestRequest(receivedTitle, receivedStringContent);
				break;
			case "getEmployerPost":
			case "refreshEmployerPost":
			case "loadMoreEmployerPost":
				
				//Client Request to get Employer Post
				
				outgoingObj = getEmployerPostRequest(receivedTitle, receivedStringContent);
				
				break;
			case "addAPortfolio":
				
				outgoingObj = addAPortfolioRequest(receivedTitle, receivedStringContent);
				
				break;
			case "deleteAPortfolio":
				outgoingObj = deleteAPortfolioRequest(receivedTitle, receivedStringContent);
				break;
			case "editPortfolio":
				outgoingObj = editPortfolioRequest(receivedTitle, receivedStringContent);
				break;
			case "editStudentInfo":
				outgoingObj = editStudentInfoRequest(receivedTitle, receivedStringContent);
				
				break;
			case "editEmployerInfo":
				outgoingObj = editEmployerInfoRequest(receivedTitle, receivedStringContent);
				break;
			case "deleteSoftwareSkill":
				outgoingObj = deleteSoftwareSkillRequest(receivedTitle, receivedStringContent);
				break;
			case "editSoftwareSkill":
				outgoingObj = editSoftwareSkillRequest(receivedTitle, receivedStringContent);				
				break;
			case "addSoftwareSkill":
				outgoingObj = addSoftwareSkillRequest(receivedTitle, receivedStringContent);
				
				break;
			case "createAPost":
				outgoingObj = createAPostRequest(receivedTitle, receivedStringContent);
				
				break;
			case "editPost":
				outgoingObj = editPostRequest(receivedTitle, receivedStringContent);
				break;
			case "deletePost":
				outgoingObj = deletePostRequest(receivedTitle, receivedStringContent);
				break;
			case "doneHiringPost":
				outgoingObj = doneHiringPostRequest(receivedTitle, receivedStringContent);
				break;
			case "acceptPostRequest":
				outgoingObj = acceptPostRequestRequest(receivedTitle, receivedStringContent);
				break;
			case "reviseInterviewRequest":
				outgoingObj = reviseInterviewRequestRequest(receivedTitle, receivedStringContent);
				break;
			case "rejectPostRequest":
				outgoingObj = rejectPostRequestRequest(receivedTitle, receivedStringContent);
				break;
			case "studentCancelPostRequest":
				outgoingObj = studentCancelPostRequestRequest(receivedTitle, receivedStringContent);
				break;
			case "studentDeclineInterviewRequest":
				outgoingObj = studentDeclineInterviewRequestRequest(receivedTitle, receivedStringContent);
				break;
			case "studentAcceptInterviewRequest":
				outgoingObj = studentAcceptInterviewRequestRequest(receivedTitle, receivedStringContent);
				break;
			case "applyEmployerPostRequest":
				outgoingObj = applyEmployerPostRequest(receivedTitle, receivedStringContent);
				
				break;
			case "studentCheckProfile":
			case "employerCheckProfile":
				outgoingObj = checkProfileRequest(receivedTitle, receivedStringContent);
				
				break;
			case "createAccount":
				outgoingObj = createAccountRequest(receivedTitle, receivedStringContent);
				break;
			case "getStudentMyChatInfo":
				outgoingObj = getStudentMyChatInfoRequest(receivedTitle, receivedStringContent);
				break;
			case "getEmployerMyChatInfo":
				outgoingObj = getEmployerMyChatInfoRequest(receivedTitle, receivedStringContent);
				break;				
			case "searchChatRoom":
				outgoingObj = searchChatRoomRequest(receivedTitle, receivedStringContent);
				break;
			case "sentMessageChatRoom":
				outgoingObj = sentMessageChatRoomRequest(receivedTitle, receivedStringContent);
				break;
			default:
				System.out.println("ERROR! No Matched name For Client Request Name. Please Check! ");	
				
		}
		
		
		//make reply
        if (outgoingObj != null) {
        	
        	
            cs.addObjectIntoSentQueue(outgoingObj);//add object in the ConnectionSender Queue
            			
			
        }else {
        	System.out.println("Please Check outgoingObject ! declare before sent. ");
        }
		
       
		
		
	}
	
	/*Note: The Variable inside handleClientReply(ComObject cObj ) is interupt when multiple thread use this function
	 * at the same time
	 * Therefore, synchronized Keyword is added. (Which limit the function to 1 thread per time)*/
	public synchronized void handleClientReply(ComObject cObj ) {
		System.out.println("handling Client Reply");
		
		//create Variable that used to store a Communication Object(Which contain reply for their request)
		ComObject outgoingObj = null;// initialize a null value
				
		//create Variable to store information received from Client
		String receivedTitle = null;
		String[] receivedStringContent = null;
		Table[] receivedTableContent = null;
		Query[] receivedQueryContent = null;
			    
		
		//extracting the Information from the Communication Object(send by client)		
		receivedTitle = cObj.getTitle();//get the title of the Client Communication Object(Ex: login, getAccountInfo, etc)
				
		/*Note: not all RelatedContentArray inside the Communication Object will be Used
		 * It depend on the request Needs
		 * RelatedContentArray(Not Used) will be assign a null value*/
				
		//get the received content if they are not null
		if(!(cObj.checkEmptyOrNot("relatedStringContent")))
			receivedStringContent = cObj.getRelatedStringContent();
		if(!(cObj.checkEmptyOrNot("relatedTableContent")))
		    receivedTableContent = cObj.getRelatedTableContent();
		if(!(cObj.checkEmptyOrNot("relatedQueryContent")))
		    receivedQueryContent = cObj.getRelatedQueryContent();		
		
		
		switch (receivedTitle) {
        	case "Greet":
        		System.out.println("Client Reply for " + receivedTitle + " is : " + receivedStringContent[0]);
        		break;
        	default:
        		System.out.println("ERROR! No Matched name For Client Reply Name. Please Check! ");	
        			


		}
		
		
		
		
	}
	
	private ComObject updateTokenRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + "from client Username:"+ clientUsername + "from Socket:" + s);
		String username = receivedStringContent[0];
		String token = receivedStringContent[1];
		
		FirebaseCloudMessageController.updateUserToken(username, token);
		
		String[] relatedStringContent = new String[] {"success"};
		
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
		
		return replyObj;		
		
		
		
	}
	
	
	private ComObject deleteTokenRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		String username = receivedStringContent[0];
		
		FirebaseCloudMessageController.deleteUserToken(username);
		
		String[] relatedStringContent = new String[] {"success"};
		
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
		
		return replyObj;		
		
		
		
	}
	
	private ComObject logOutRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		String username = receivedStringContent[0];
		MainScreen.addLogText("Account With Username: (" + username + ") Has Logged out at" + s +" and Removed From the Online Client List");
		ServerCore.removeOnlineClientIntoArrayList(username);
		
		String[] relatedStringContent = new String[] {"success"};
		
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
		
		return replyObj;		
		
		
		
		
	}

	
	
	private ComObject loginRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);		
		System.out.println("login function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;
		
		
		
		
		Table u = DatabaseController.getTable("user");
		u.printTable();
		String[] searchColumn = {"Username", "Password"};
		
		String clientUsername= receivedStringContent[0].toLowerCase();
		String clientPassword = receivedStringContent[1];
		
		//Client send: receivedStringContent[0] is username, receivedStringContent[1] is password, 
		String[] dataToBeCompared = {clientUsername, clientPassword};
		String[][] resultRow = u.searchInTable(searchColumn, dataToBeCompared);
		
		if(resultRow.length >= 1) {
			//if result is morethan 1 row
			//then username & password matches
			
			
			//if mathes, we check whether this account is already Online
			if(!ServerCore.checkClientOnlineOrNot(clientUsername)) {
				//if not online then find the accounttype (student or employer)
				String[] select = {"Acc_type"};
				String[] from = {"User"};
				String[] condition = {"Username='" + clientUsername + "'"};
				Query query = DatabaseController.getQuery(select, from, condition, null);
				
				query.printQuery();
				
				String accountType = query.getDataInQuery()[1][1];
				
				relatedStringContent = new String[] {"success", clientUsername, accountType, clientPassword};
			
			
				this.clientUsername = clientUsername;
				//if the login request is success, 
				//we will need to add this particular clientHandler into the ServerCore's OnlineClientArrayList
				
				ServerCore.addOnlineClientIntoArrayList(this.clientUsername, this);
				MainScreen.addLogText("Account With Username: (" + clientUsername + ") Has Logged on at" + s);
				
			}else {
				relatedStringContent = new String[] {"alreadyOnline"};
			}
			
			
			
		}else {
			//else fail
			relatedStringContent = new String[] {"fail"};
		}
		
		
		ComObject replyObj = new ComObject("ServerReply", "login", relatedStringContent);
		
		return replyObj;
		
		
		
	}
	
	private ComObject getEmployerPostRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("getEmployerPostRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		Query[] relatedQueryContent = null;
				
		
		String[] select = {"ehp.username", "p.profilepic","ehp_pic.picture", "ehp.hirepostid", "p.firstname", "p.lastname", "ehp.title", "ehp.description", "ehp.jobtype", "ehp.offers", "ehp.datetime_posted", "ehp.location", "ehp.workingHours", "ehp.Post_SkillCategory" };
		String[] from = {"employer_hiringpost ehp", "profile p", "user u", "employer_hiringpost_picture ehp_pic"};
		String[] condition = null;// will be define later
		String[] additionCommand = {"order by ehp.datetime_posted DESC", "limit 0,10"};
		Query resultQuery;//store result of Query
		
		
		if(receivedStringContent[0] != null) {
			//get the relatedData Pass by client
			//relatedStringContent[0] is the client pass condition. EX: "<'YYYY-MM-DD HH-MM-SS'"
			
			
			String dateTimeCondition = "ehp.datetime_posted" + receivedStringContent[0];
			
			condition = new String[]{"ehp.username = u.username", "u.profileid=p.profileid", "ehp_pic.hirepostid=ehp.hirepostid", "ehp.poststatus='ongoing'", dateTimeCondition};
			
			
			resultQuery = DatabaseController.getQuery(select, from, condition, additionCommand);
		}else {
			//if doesnt pass mean get the lastest employer post
			condition = new String[]{"ehp.username = u.username", "u.profileid=p.profileid", "ehp_pic.hirepostid=ehp.hirepostid", "ehp.poststatus='ongoing'"};
			
			resultQuery = DatabaseController.getQuery(select, from, condition, additionCommand);
		}
		
		
		
		
		resultQuery.printQuery();
		
		relatedQueryContent = new Query[] {resultQuery};
		
		
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedQueryContent);
		
		return replyObj;
		
		
	}
	
	private ComObject getStudentAccountInfoRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("getAccountInfoRequest function performing for Account Name: "+ receivedStringContent[0]);
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		Query[] relatedQueryContent = null;
		
		String accountUsername = receivedStringContent[0];//Client Send his username
		
		
		//Getting User Account Info into a Query Object
		String[] accountInfoSelect = {"u.username", "u.acc_type", "u.profileid", "p.profilepic", "p.firstname", "p.lastname", "p.generalskill_category","p.aboutme", "p.collegename", "p.coursename"};
		String[] accountInfoFrom = {"user u", "profile p"};
		String[] accountInfoCondition = {"u.username = '"+ accountUsername +"'", "u.profileid=p.profileid"};// will be define later
		
		Query accountInfo = DatabaseController.getQuery(accountInfoSelect, accountInfoFrom, accountInfoCondition, null);
		
	    accountInfo.printQuery();
	    
	    //extract the data inside the User AccountInfo
	    String[][] accountInfoData =  accountInfo.getDataInQuery();
	    
	    String accountProfileId = accountInfoData[1][3];//get the user ProfileId from the Extracted data
	    
	    //Note: Getting ProfileId of User allow other user Related data such as(AccountContact Table, AccountPortfolio Table, AccountSoftwareSkill Table)
	    //ProfileId is the Composite Primarykey Of user Related data TAble
	    
	    //Getting User Account Contact into a Query Object		
	    String[] accountContactSelect = {"contacttype","contactinfo"};
	    String[] accountContactFrom = {"profile_contact"};
	    String[] accountContactWhere = {"profileid = '" + accountProfileId + "'"};
	    Query accountContact = DatabaseController.getQuery(accountContactSelect, accountContactFrom, accountContactWhere, null);
	    
	    //Getting User Account Portfolio into a Query Object
	    String[] accountPortfolioSelect = {"portfolio_id", "portfoliotype", "portfoliopicture", "description", "portfoliourl"};
	    String[] accountPortfolioFrom = {"Profile_Portfolio"};
	    String[] accountPortfolioWhere = {"profileid = '" + accountProfileId + "'"};
	    
	    Query accountPortfolio = DatabaseController.getQuery(accountPortfolioSelect, accountPortfolioFrom, accountPortfolioWhere, null);
	    
	    accountPortfolio.printQuery();
	    
	    //Getting User Account Software Skill info into a Query Object
	    String[] accountSoftwareSkillSelect = {"SoftwareSkillID", "softwarename","skilllevel"};
	    String[] accountSoftwareSkillFrom = {"profile_softwareskill"};
	    String[] accountSoftwareSkillWhere = {"profileid = '" + accountProfileId + "'"};
	    Query accountSoftwareSkill = DatabaseController.getQuery(accountSoftwareSkillSelect, accountSoftwareSkillFrom, accountSoftwareSkillWhere, null);
	    accountSoftwareSkill.printQuery();
	    
	    
	    //Combine all Client Require Data into a Query Array
	    relatedQueryContent = new Query[]{accountInfo, accountContact, accountPortfolio, accountSoftwareSkill};
	    
	    //create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedQueryContent);
		
		return replyObj;
			
	
	}
	
	private ComObject getEmployerAccountInfoRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("getEmployerAccountInfoRequest function performing for Account Name: "+ receivedStringContent[0]);
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		Query[] relatedQueryContent = null;
		
		String accountUsername = receivedStringContent[0];//Client Send his username
		
		
		//Getting User Account Info into a Query Object
		String[] accountInfoSelect = {"u.username", "u.acc_type", "u.profileid", "p.profilepic", "p.firstname", "p.lastname", "p.generalskill_category","p.aboutme"};
		String[] accountInfoFrom = {"user u", "profile p"};
		String[] accountInfoCondition = {"u.username = '"+ accountUsername +"'", "u.profileid=p.profileid"};// will be define later
		
		Query accountInfo = DatabaseController.getQuery(accountInfoSelect, accountInfoFrom, accountInfoCondition, null);
		
	    accountInfo.printQuery();
	    
	    //extract the data inside the User AccountInfo
	    String[][] accountInfoData =  accountInfo.getDataInQuery();
	    
	    String accountProfileId = accountInfoData[1][3];//get the user ProfileId from the Extracted data
	    
	    //Note: Getting ProfileId of User allow other user Related data such as(AccountContact Table, AccountPortfolio Table, AccountSoftwareSkill Table)
	    //ProfileId is the Composite Primarykey Of user Related data TAble
	    
	    //Getting User Account Contact into a Query Object		
	    String[] accountContactSelect = {"contacttype","contactinfo"};
	    String[] accountContactFrom = {"profile_contact"};
	    String[] accountContactWhere = {"profileid = '" + accountProfileId + "'"};
	    Query accountContact = DatabaseController.getQuery(accountContactSelect, accountContactFrom, accountContactWhere, null);
	    
	    accountContact.printQuery();
	    //Combine all Client Require Data into a Query Array
	    relatedQueryContent = new Query[]{accountInfo, accountContact};
	    
	    //create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedQueryContent);
		
		return replyObj;
			
	
	}
	
	private ComObject getEmployerMyPostInfoRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("getEmployerMyPostInfoRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		Query[] relatedQueryContent = null;
		
		String accountUsername = receivedStringContent[0];//Client Send his username
		
		
				
		
		String[] select = {"ehp.username", "p.profilepic","ehp_pic.picture", "ehp.hirepostid", "p.firstname", "p.lastname", "ehp.title", "ehp.description", "ehp.jobtype", "ehp.offers", "ehp.datetime_posted", "ehp.location", "ehp.workingHours", "ehp.Post_SkillCategory", "ehp.PostStatus" };
		String[] from = {"employer_hiringpost ehp", "profile p", "user u", "employer_hiringpost_picture ehp_pic"};
		String[] condition = {"ehp.username = u.username", "u.profileid=p.profileid", "ehp_pic.hirepostid=ehp.hirepostid", "ehp.username='" + accountUsername + "'"};
		String[] additionCommand = {"order by ehp.datetime_posted DESC"};
		Query resultQuery = DatabaseController.getQuery(select, from, condition, additionCommand);//store result of Query
			
					
		resultQuery.printQuery();
		
		relatedQueryContent = new Query[] {resultQuery};
		
		
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedQueryContent);
		
		return replyObj;
		
		
	}
	
	private ComObject getEmployerMyPostRequestRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("getEmployerMyPostRequestRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		Query[] relatedQueryContent = null;
		
		String accountUsername = receivedStringContent[0];//Client Send his username
		
		
				
		
		String[] select = {"ehp.hirepostid","hpp.requestdateTime", "hpp.requesterUsername", "p.firstname", "p.lastname", "p.profilepic", "p.GeneralSkill_Category", "hpp.message", "hpp.status", "hpi.requestDateTime", "hpi.requesterUsername", "hpi.message", "hpi.date", "hpi.time", "hpi.location", "hpi.status", "hpp.Status_added_dateTime" };
		String[] from = {"employer_hiringpost ehp", "Hiring_Post_Pending_Request hpp", "Hiring_Post_interview_Request hpi", "profile p", "user u"};
		String[] condition = {"ehp.hirepostid=hpp.hirepostid", "ehp.hirepostid=hpi.hirepostid", "hpp.requesterUsername=hpi.requesterUsername", "ehp.username='" + accountUsername + "'", "u.username=hpp.requesterUsername", "u.profileid=p.profileid", "u.username=hpi.requesterUsername"};
		String[] additionCommand = {"group by ehp.hirepostid, hpp.requesterUsername", "order by ehp.datetime_posted DESC, ehp.hirepostid ASC, hpp.Status_added_dateTime DESC"};
		Query resultQuery = DatabaseController.getQuery(select, from, condition, additionCommand);//store result of Query
			
					
		resultQuery.printQuery();
		
		relatedQueryContent = new Query[] {resultQuery};
		
		
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedQueryContent);
		
		return replyObj;
		
		
	}
	
	private ComObject getStudentMyRequestRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("getStudentMyRequestRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		Query[] relatedQueryContent = null;
		
		String studentAccountUsername = receivedStringContent[0];//Client Send his username
		
		
				
		
		String[] select = {"ehp.username", "ehp.hirepostid", "hppPro.firstname", "hppPro.lastname", "hpp.requestdateTime", "hpp.message", "hpp.status", "hpi.requestDateTime", "hpi.requesterUsername", "hpi.message", "hpi.date", "hpi.time", "hpi.location", "hpi.status", "hpp.Status_added_dateTime", "p.profilepic", "ehp_pic.picture", "ehp.hirepostid", "p.firstname", "p.lastname", "ehp.title", "ehp.description", "ehp.jobtype", "ehp.offers", "ehp.datetime_posted", "ehp.location", "ehp.workingHours", "ehp.Post_SkillCategory" };
		String[] from = {"employer_hiringpost ehp", "Hiring_Post_Pending_Request hpp", "Hiring_Post_interview_Request hpi", "profile p", "user u", "employer_hiringpost_picture ehp_pic", "profile hppPro", "user hppuser"};
		String[] condition = {"ehp.hirepostid=hpp.hirepostid", "ehp.hirepostid=hpi.hirepostid", "hpp.requesterUsername=hpi.requesterUsername", "hppPro.profileid=hppuser.profileid", "hppuser.username=hpp.requesterusername", "hpp.requesterUsername='" + studentAccountUsername + "'", "ehp.username=u.username", "u.username=ehp.username", "u.profileid=p.profileid", "ehp_pic.hirepostid=ehp.hirepostid" };
		String[] additionCommand = {"group by ehp.hirepostid, hpp.requesterUsername", "order by hpp.Status_added_dateTime DESC, ehp.hirepostid DESC"};
		Query resultQuery = DatabaseController.getQuery(select, from, condition, additionCommand);//store result of Query
			
					
		resultQuery.printQuery();
		
		relatedQueryContent = new Query[] {resultQuery};
		
		
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedQueryContent);
		
		return replyObj;
		
		
	}
	
	
	
	private ComObject addAPortfolioRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("addAPortfolioRequest function performing for Account Name: ");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
		
		//get received Info
		//receivedStringContent Array Content as follow -> profileId, portfolioPicBase64EncodedString, portfolioType,portfolioDesc,portfolioUrl
		String profileId = receivedStringContent[0];
		String portfolioPicBase64EncodedString = receivedStringContent[1];
		String portfolioType = receivedStringContent[2];
		String portfolioDesc = receivedStringContent[3];
		String portfolioUrl = receivedStringContent[4];
		
		//every User portfolio start from 1, if user have 3 portfolio then portfolioId will have (1,2,3)
		//generate the new PorfolioId for the user	
		String[] select = {"MAX(CAST(portfolio_ID AS int)) AS newPortfolioId"};
		String[] from = {"profile_portfolio"};
		String conditionForProfileId = "profileId='"+profileId+"'";
		
	
		
		String[] where = {conditionForProfileId};
		Query countTotalPortfolioOfUser = DatabaseController.getQuery(select, from, where, null);
		String[][] dataInCountTotalPortfolioOfUser = countTotalPortfolioOfUser.getDataInQuery();
		
		countTotalPortfolioOfUser.printQuery();
		int totalNumberOfPortfolio;
		if(dataInCountTotalPortfolioOfUser[1][1] == null) {
			//if null means cannot find any existing record. User haven insert any portfolio
			totalNumberOfPortfolio = 0;
		}else {
			totalNumberOfPortfolio = Integer.parseInt(dataInCountTotalPortfolioOfUser[1][1]);
		}
		
		
		//total number of portfolio of a user + 1(So, This PortfolioId will be unique)
		int newPortfolioId = totalNumberOfPortfolio += 1;
		
		String newPortfolioIdString = Integer.toString(newPortfolioId);
		
		
		
		//Start inserting value received into the database
			
		//in order to insert we need to provide a String array that arranged according the table column
		//Profile_Portfolio Table Column: ProfileID,Portfolio_ID,PortfolioType,PortfolioPicture,Description,PortfolioUrl
		String[] values = {profileId, newPortfolioIdString, portfolioType, portfolioPicBase64EncodedString, portfolioDesc, portfolioUrl };
		
		if(DatabaseController.insertDataIntoTable("Profile_Portfolio", values)) {
			relatedStringContent = new String[]{"success", newPortfolioIdString, portfolioType, portfolioPicBase64EncodedString, portfolioDesc, portfolioUrl};
			System.out.print("insert portfolio Success");
		}else {
			System.out.print("insert portfolio Failed");
			relatedStringContent = new String[]{"failed"};
			
		}
		
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
		
		
		return replyObj;
		
		
		
		
		
		
		
		
	}
	
	
	
	private ComObject deleteAPortfolioRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("deleteAPortfolioRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
		
		//get received Info
		//receivedStringContent Array Content as follow -> profileId, portfolioPicBase64EncodedString, portfolioType,portfolioDesc,portfolioUrl
		String profileId = receivedStringContent[0];
		String portfolioId = receivedStringContent[1];
		
		String positionInArrayList_string = receivedStringContent[2];//this will be send back to client if record successfuly deleted
		
		//Start delete record received into the database by using the received profileId and portfolioId
		
		//Note: Porfile_portfolio TABLE, can track a unique record by providing user's profileid && portfolioId
		String[] condition = {"ProfileID='" + profileId + "'", "Portfolio_ID='" + portfolioId + "'"};
		
		if(DatabaseController.deleteDataFromTable("Profile_Portfolio", condition)) {
			//if successfully deleted 
			relatedStringContent = new String[]{"success", positionInArrayList_string};//notify client this Arraylistrecord at(position) has removed
			System.out.print("delete portfolio Success");
		}else {
			System.out.print("delete portfolio Failed");
			relatedStringContent = new String[]{"failed"};
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
	}
	
	private ComObject editPortfolioRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("editPortfolioRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> profileId, portfolioId,portfolioPicBase64EncodedString, portfolioType,portfolioDesc,portfolioUrl, positionInArrayList_string
		String profileId = receivedStringContent[0];
		String portfolioId = receivedStringContent[1];
		String portfolioPicBase64EncodedString = receivedStringContent[2];
		String portfolioType = receivedStringContent[3];
		String portfolioDesc = receivedStringContent[4];
		String portfolioUrl = receivedStringContent[5];
			
		String positionInArrayList_string = receivedStringContent[6];//this will be send back to client if record successfully edited
				
		//Start edit record received into the database by using the received profileId and portfolioId
				
		//Note: Porfile_portfolio TABLE, can track a unique record by providing user's profileid && portfolioId
		String[] set = {"PortfolioType='" + portfolioType +"'", "PortfolioPicture='" + portfolioPicBase64EncodedString + "'"
				,"Description='" + portfolioDesc + "'", "PortfolioUrl='" + portfolioUrl + "'"};
		String[] condition = {"ProfileID='" + profileId + "'", "Portfolio_ID='" + portfolioId + "'"};
				
		if(DatabaseController.updateDataFromTable("Profile_Portfolio", set, condition)) {
			//if successfully deleted 
			relatedStringContent = new String[]{"success", profileId, portfolioId, portfolioPicBase64EncodedString, portfolioType, portfolioDesc, portfolioUrl, positionInArrayList_string};//notify client this Arraylistrecord at(position) has removed
			System.out.print("edit portfolio Success");
		}else {
			System.out.print("edit portfolio Failed");
			relatedStringContent = new String[]{"failed"};
		}
				
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
					
		return replyObj;
		
		
		
	}
	
	private ComObject editStudentInfoRequest(String receivedTitle, String[] receivedStringContent) {
		
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
		
		//get received Info
		//receivedStringContent Array Content as follow -> profileId, profilePicBase64EncodedString, fName, lName, skillCat, aboutMe, collegeName, courseName, phoneNum, email, whatsappUrl, facebookUrl, twitterUrl 
		String profileId = receivedStringContent[0];
		String profilePicBase64EncodedString = receivedStringContent[1];
		String fName = receivedStringContent[2];
		String lName = receivedStringContent[3];
		String skillCat = receivedStringContent[4];
		String aboutMe = receivedStringContent[5];
		String collegeName = receivedStringContent[6];
		String courseName = receivedStringContent[7];
		String phoneNum = receivedStringContent[8];
		String email = receivedStringContent[9];
		String whatsappUrl = receivedStringContent[10];
		String facebookUrl = receivedStringContent[11];
		String twitterUrl = receivedStringContent[12];
		
		//Section: EditProfileInfo Info in database TABLE profile
		
		//Note: Porfile TABLE, can track a unique record by providing user's profileid
		String[] set = new String[] {"ProfilePic='" + profilePicBase64EncodedString + "'", "FirstName='" + fName + "'", "LastName='" + lName +" '"
				, "GeneralSkill_Category='" + skillCat + "'", "AboutMe='" + aboutMe + "'", "CollegeName='" + collegeName +"'"
				, "CourseName='" + courseName + "'"};
		String[] condition = new String[]{"ProfileID='" + profileId + "'"};
		if(DatabaseController.updateDataFromTable("Profile", set, condition)) {
			//if profile Table Done
			
			//Section: update Contact Info in database TABLE profile
			
			String[] phoneNumset = new String[] {"ContactInfo='" + phoneNum + "'"};
			String[] phoneNumcondition = new String[]{"ContactType='phonenum'", "ProfileId='" + profileId + "'"};
			if(DatabaseController.updateDataFromTable("Profile_Contact", phoneNumset, phoneNumcondition)) {
				String[] emailSet = new String[] {"ContactInfo='" + email + "'"};
				String[] emailCondition = new String[] {"ContactType='email'", "ProfileId='" + profileId + "'" };
				if(DatabaseController.updateDataFromTable("Profile_Contact", emailSet, emailCondition)) {
					//if after execute all task these 3 variable still false means, some of the action failed
					boolean whatsappTaskStatus = false;
					boolean facebookTaskStatus = false;
					boolean twitterTaskStatus = false;
					
					boolean whatsappExist = true;
					boolean facebookExist = true;
					boolean twitterExist = true;
					
					String[] whatsappExistSelect = new String[] {"*"};
					String[] whatsappExistTable = new String[] {"Profile_Contact"};
					String[] whatsappExistWhere = new String[] {"ContactType='whatsapp'", "ProfileId='" + profileId + "'"};
					Query whatsappExistQuery = DatabaseController.getQuery(whatsappExistSelect, whatsappExistTable, whatsappExistWhere, null);
					if(whatsappExistQuery.EmptyOrNot()) {
						//if empty then make remark
						whatsappExist = false;							
					}
					
					String[] facebookExistSelect = new String[] {"*"};
					String[] facebookExistTable = new String[] {"Profile_Contact"};
					String[] facebookExistWhere = new String[] {"ContactType='facebook'", "ProfileId='" + profileId + "'"};
					Query facebookExistQuery = DatabaseController.getQuery(facebookExistSelect, facebookExistTable, facebookExistWhere, null);
					if(facebookExistQuery.EmptyOrNot()) {
						//if empty then make remark
						facebookExist = false;							
					}
					
					String[] twitterExistSelect = new String[] {"*"};
					String[] twitterExistTable = new String[] {"Profile_Contact"};
					String[] twitterExistWhere = new String[] {"ContactType='twitter'", "ProfileId='" + profileId + "'"};
					Query twitterExistQuery = DatabaseController.getQuery(twitterExistSelect, twitterExistTable, twitterExistWhere, null);
					if(twitterExistQuery.EmptyOrNot()) {
						//if empty then make remark
						twitterExist = false;							
					}
					
					//Section: update Contact Info(whatsapp,twitter,facebook) in database TABLE profile
					
					if(whatsappUrl != null) {
						//not null then replace the existing data
						
						if(!whatsappExist) {
							//if whatsapp record is not in database, insert data
							String[] whatsappValues = new String[] {profileId, "whatsapp", whatsappUrl};
							if(DatabaseController.insertDataIntoTable("Profile_Contact", whatsappValues)) {
								whatsappTaskStatus = true;
							}else {
								whatsappTaskStatus = false;
							}
						}else {
							//means there is a whatsapp record in database
							//replace it
							String[] whatsappSet = new String[] {"ContactInfo='" + whatsappUrl + "'"};
							String[] whatsappCondition = new String[] {"ContactType='whatsapp'", "ProfileId='" + profileId + "'" };
				
							if(DatabaseController.updateDataFromTable("Profile_Contact", whatsappSet, whatsappCondition)) {
								whatsappTaskStatus = true;
							}else {
								whatsappTaskStatus = false;
							}
						}
					}else {
						//if whatsapp url is null
						//deleted it
						String[] whatsappDelete = new String[] {"ContactType='whatsapp'", "ProfileId='" + profileId + "'"};
						
						if(DatabaseController.deleteDataFromTable("Profile_Contact", whatsappDelete)) {
							whatsappTaskStatus = true;
						}else {
							whatsappTaskStatus = false;
						}											
					}
					
					//process facebook request
					if(facebookUrl != null) {
						//not null then replace the existing data
						
						if(!facebookExist) {
							//if facebook record is not in database, insert data
							String[] facebookValues = new String[] {profileId, "facebook", facebookUrl};
							if(DatabaseController.insertDataIntoTable("Profile_Contact", facebookValues)) {
								facebookTaskStatus = true;
							}else {
								facebookTaskStatus = false;
							}
						}else {
							//means there is a facebook record in database
							//replace it
							String[] facebookSet = new String[] {"ContactInfo='" + facebookUrl + "'"};
							String[] facebookCondition = new String[] {"ContactType='facebook'", "ProfileId='" + profileId + "'" };
				
							if(DatabaseController.updateDataFromTable("Profile_Contact", facebookSet, facebookCondition)) {
								facebookTaskStatus = true;
							}else {
								facebookTaskStatus = false;
							}
						}
					}else {
						//if facebook url is null
						//deleted it
						String[] facebookDelete = new String[] {"ContactType='facebook'", "ProfileId='" + profileId + "'"};
						
						if(DatabaseController.deleteDataFromTable("Profile_Contact", facebookDelete)) {
							facebookTaskStatus = true;
						}else {
							facebookTaskStatus = false;
						}											
					}
					
					//process twitter request
					if(twitterUrl != null) {
						//not null then replace the existing data
						
						if(!twitterExist) {
							//if twitter record is not in database, insert data
							String[] twitterValues = new String[] {profileId, "twitter", twitterUrl};
							if(DatabaseController.insertDataIntoTable("Profile_Contact", twitterValues)) {
								twitterTaskStatus = true;
							}else {
								twitterTaskStatus = false;
							}
						}else {
							//means there is a twitter record in database
							//replace it
							String[] twitterSet = new String[] {"ContactInfo='" + twitterUrl + "'"};
							String[] twitterCondition = new String[] {"ContactType='twitter'", "ProfileId='" + profileId + "'" };
				
							if(DatabaseController.updateDataFromTable("Profile_Contact", twitterSet, twitterCondition)) {
								twitterTaskStatus = true;
							}else {
								twitterTaskStatus = false;
							}
						}
					}else {
						//if twitter url is null
						//deleted it
						String[] twitterDelete = new String[] {"ContactType='twitter'", "ProfileId='" + profileId + "'"};
						
						if(DatabaseController.deleteDataFromTable("Profile_Contact", twitterDelete)) {
							twitterTaskStatus = true;
						}else {
							twitterTaskStatus = false;
						}											
					}
					
					if(whatsappTaskStatus && facebookTaskStatus && twitterTaskStatus) {
						System.out.println("EditStudentInfo Success");
						relatedStringContent = new String[] {"success", profileId, profilePicBase64EncodedString, fName, lName, skillCat, aboutMe, collegeName, courseName, phoneNum, email, whatsappUrl, facebookUrl, twitterUrl };
						
						
					}else {
						System.out.println("EditStudentInfo Failed");
						relatedStringContent = new String[] {"failed"};
					}								
					
					
					
					
				}else {
					System.out.println("EditStudentInfo Failed");
					relatedStringContent = new String[] {"failed"};				
				}
							
				
			}else {
				System.out.println("EditStudentInfo Failed");
				relatedStringContent = new String[] {"failed"};				
			}
			
			
		}else {
			System.out.println("EditStudentInfo Failed");
			relatedStringContent = new String[] {"failed"};
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
		
		return replyObj;
		
	}
	
	private ComObject editEmployerInfoRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
		
		//get received Info
		//receivedStringContent Array Content as follow -> profileId, profilePicBase64EncodedString, fName, lName, aboutMe, phoneNum, email, whatsappUrl, facebookUrl, twitterUrl
		String profileId = receivedStringContent[0];
		String profilePicBase64EncodedString = receivedStringContent[1];
		String fName = receivedStringContent[2];
		String lName = receivedStringContent[3];
		String aboutMe = receivedStringContent[4];
		String phoneNum = receivedStringContent[5];
		String email = receivedStringContent[6];
		String whatsappUrl = receivedStringContent[7];
		String facebookUrl = receivedStringContent[8];
		String twitterUrl = receivedStringContent[9];
		
		//Section: EditProfileInfo Info in database TABLE profile
		
		//Note: Profile TABLE, can track a unique record by providing user's profileid
		String[] set = new String[] {"ProfilePic='" + profilePicBase64EncodedString + "'", "FirstName='" + fName + "'", "LastName='" + lName +" '"
				, "AboutMe='" + aboutMe + "'"};
		String[] condition = new String[]{"ProfileID='" + profileId + "'"};
		if(DatabaseController.updateDataFromTable("Profile", set, condition)) {
			//if profile Table Done
			
			//Section: update Contact Info in database TABLE profile
			
			String[] phoneNumset = new String[] {"ContactInfo='" + phoneNum + "'"};
			String[] phoneNumcondition = new String[]{"ContactType='phonenum'", "ProfileId='" + profileId + "'"};
			if(DatabaseController.updateDataFromTable("Profile_Contact", phoneNumset, phoneNumcondition)) {
				String[] emailSet = new String[] {"ContactInfo='" + email + "'"};
				String[] emailCondition = new String[] {"ContactType='email'", "ProfileId='" + profileId + "'" };
				if(DatabaseController.updateDataFromTable("Profile_Contact", emailSet, emailCondition)) {
					//if after execute all task these 3 variable still false means, some of the action failed
					boolean whatsappTaskStatus = false;
					boolean facebookTaskStatus = false;
					boolean twitterTaskStatus = false;
					
					boolean whatsappExist = true;
					boolean facebookExist = true;
					boolean twitterExist = true;
					
					String[] whatsappExistSelect = new String[] {"*"};
					String[] whatsappExistTable = new String[] {"Profile_Contact"};
					String[] whatsappExistWhere = new String[] {"ContactType='whatsapp'", "ProfileId='" + profileId + "'"};
					Query whatsappExistQuery = DatabaseController.getQuery(whatsappExistSelect, whatsappExistTable, whatsappExistWhere, null);
					if(whatsappExistQuery.EmptyOrNot()) {
						//if empty then make remark
						whatsappExist = false;							
					}
					
					String[] facebookExistSelect = new String[] {"*"};
					String[] facebookExistTable = new String[] {"Profile_Contact"};
					String[] facebookExistWhere = new String[] {"ContactType='facebook'", "ProfileId='" + profileId + "'"};
					Query facebookExistQuery = DatabaseController.getQuery(facebookExistSelect, facebookExistTable, facebookExistWhere, null);
					if(facebookExistQuery.EmptyOrNot()) {
						//if empty then make remark
						facebookExist = false;							
					}
					
					String[] twitterExistSelect = new String[] {"*"};
					String[] twitterExistTable = new String[] {"Profile_Contact"};
					String[] twitterExistWhere = new String[] {"ContactType='twitter'", "ProfileId='" + profileId + "'"};
					Query twitterExistQuery = DatabaseController.getQuery(twitterExistSelect, twitterExistTable, twitterExistWhere, null);
					if(twitterExistQuery.EmptyOrNot()) {
						//if empty then make remark
						twitterExist = false;							
					}
					
					//Section: update Contact Info(whatsapp,twitter,facebook) in database TABLE profile
					
					if(whatsappUrl != null) {
						//not null then replace the existing data
						
						if(!whatsappExist) {
							//if whatsapp record is not in database, insert data
							String[] whatsappValues = new String[] {profileId, "whatsapp", whatsappUrl};
							if(DatabaseController.insertDataIntoTable("Profile_Contact", whatsappValues)) {
								whatsappTaskStatus = true;
							}else {
								whatsappTaskStatus = false;
							}
						}else {
							//means there is a whatsapp record in database
							//replace it
							String[] whatsappSet = new String[] {"ContactInfo='" + whatsappUrl + "'"};
							String[] whatsappCondition = new String[] {"ContactType='whatsapp'", "ProfileId='" + profileId + "'" };
				
							if(DatabaseController.updateDataFromTable("Profile_Contact", whatsappSet, whatsappCondition)) {
								whatsappTaskStatus = true;
							}else {
								whatsappTaskStatus = false;
							}
						}
					}else {
						//if whatsapp url is null
						//deleted it
						String[] whatsappDelete = new String[] {"ContactType='whatsapp'", "ProfileId='" + profileId + "'"};
						
						if(DatabaseController.deleteDataFromTable("Profile_Contact", whatsappDelete)) {
							whatsappTaskStatus = true;
						}else {
							whatsappTaskStatus = false;
						}											
					}
					
					//process facebook request
					if(facebookUrl != null) {
						//not null then replace the existing data
						
						if(!facebookExist) {
							//if facebook record is not in database, insert data
							String[] facebookValues = new String[] {profileId, "facebook", facebookUrl};
							if(DatabaseController.insertDataIntoTable("Profile_Contact", facebookValues)) {
								facebookTaskStatus = true;
							}else {
								facebookTaskStatus = false;
							}
						}else {
							//means there is a facebook record in database
							//replace it
							String[] facebookSet = new String[] {"ContactInfo='" + facebookUrl + "'"};
							String[] facebookCondition = new String[] {"ContactType='facebook'", "ProfileId='" + profileId + "'" };
				
							if(DatabaseController.updateDataFromTable("Profile_Contact", facebookSet, facebookCondition)) {
								facebookTaskStatus = true;
							}else {
								facebookTaskStatus = false;
							}
						}
					}else {
						//if facebook url is null
						//deleted it
						String[] facebookDelete = new String[] {"ContactType='facebook'", "ProfileId='" + profileId + "'"};
						
						if(DatabaseController.deleteDataFromTable("Profile_Contact", facebookDelete)) {
							facebookTaskStatus = true;
						}else {
							facebookTaskStatus = false;
						}											
					}
					
					//process twitter request
					if(twitterUrl != null) {
						//not null then replace the existing data
						
						if(!twitterExist) {
							//if twitter record is not in database, insert data
							String[] twitterValues = new String[] {profileId, "twitter", twitterUrl};
							if(DatabaseController.insertDataIntoTable("Profile_Contact", twitterValues)) {
								twitterTaskStatus = true;
							}else {
								twitterTaskStatus = false;
							}
						}else {
							//means there is a twitter record in database
							//replace it
							String[] twitterSet = new String[] {"ContactInfo='" + twitterUrl + "'"};
							String[] twitterCondition = new String[] {"ContactType='twitter'", "ProfileId='" + profileId + "'" };
				
							if(DatabaseController.updateDataFromTable("Profile_Contact", twitterSet, twitterCondition)) {
								twitterTaskStatus = true;
							}else {
								twitterTaskStatus = false;
							}
						}
					}else {
						//if twitter url is null
						//deleted it
						String[] twitterDelete = new String[] {"ContactType='twitter'", "ProfileId='" + profileId + "'"};
						
						if(DatabaseController.deleteDataFromTable("Profile_Contact", twitterDelete)) {
							twitterTaskStatus = true;
						}else {
							twitterTaskStatus = false;
						}											
					}
					
					if(whatsappTaskStatus && facebookTaskStatus && twitterTaskStatus) {
						System.out.println("EditStudentInfo Success");
						relatedStringContent = new String[] {"success", profileId, profilePicBase64EncodedString, fName, lName, aboutMe, phoneNum, email, whatsappUrl, facebookUrl, twitterUrl };
						
						
					}else {
						System.out.println("EditStudentInfo Failed");
						relatedStringContent = new String[] {"failed"};
					}								
					
					
					
					
				}else {
					System.out.println("EditStudentInfo Failed");
					relatedStringContent = new String[] {"failed"};				
				}
							
				
			}else {
				System.out.println("EditStudentInfo Failed");
				relatedStringContent = new String[] {"failed"};				
			}
			
			
		}else {
			System.out.println("EditStudentInfo Failed");
			relatedStringContent = new String[] {"failed"};
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
		
		return replyObj;
		
	}
	
	
	private ComObject deleteSoftwareSkillRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("deleteSoftwareSkillRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> profileId, skillId
		String profileId = receivedStringContent[0];
		String skillId = receivedStringContent[1];
		String positionInArrayList_string = receivedStringContent[2];
		
		System.out.println("deleteSoftwareSkillRequest received skill id is " + skillId);
		
		String[] condition = new String[] {"ProfileId='" + profileId + "'", "SoftwareSkillID='" + skillId + "'" };
		
		if(DatabaseController.deleteDataFromTable("Profile_SoftwareSkill", condition)) {
			System.out.println("deleteSoftwareSkillRequest success");
			relatedStringContent = new String[] {"success", skillId};
		}else {
			System.out.println("deleteSoftwareSkillRequest Failed");
			relatedStringContent = new String[] {"failed"};
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
		
	}
	
	private ComObject editSoftwareSkillRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("editSoftwareSkillRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> profileId, skillId, skillName, skillLevel
		String profileId = receivedStringContent[0];
		String skillId = receivedStringContent[1];
		String skillName = receivedStringContent[2];
		String skillLevel = receivedStringContent[3];
		String positionInArrayList_string = receivedStringContent[4];
		
		String[] set = new String[] {"SoftwareName='" + skillName + "'", "SkillLevel='" + skillLevel + "'" };
		String[] condition = new String[] {"ProfileId='" + profileId + "'", "SoftwareSkillID='" + skillId + "'" };
		
		if(DatabaseController.updateDataFromTable("Profile_SoftwareSkill", set, condition)) {
			System.out.println("editSoftwareSkillRequest success");
			relatedStringContent = new String[] {"success", profileId, skillId, skillName, skillLevel, positionInArrayList_string};
		}else {
			System.out.println("editSoftwareSkillRequest Failed");
			relatedStringContent = new String[] {"failed"};
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
		
	}
	
	private ComObject addSoftwareSkillRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("addSoftwareSkillRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> profileId, skillName, skillLevel
		String profileId = receivedStringContent[0];
		String skillName = receivedStringContent[1];
		String skillLevel = receivedStringContent[2];
		
		//every User softwareskillid start from 1, if user have 3 softwareskill then softwareskillId will have (1,2,3)
		//generate the new softwareskillId for the user	
		String[] select = {"MAX(CAST(SoftwareSkillID AS int)) AS newPortfolioId"};
		String[] from = {"Profile_SoftwareSkill"};
		String conditionForProfileId = "profileId='"+profileId+"'";
		
				
		String[] where = {conditionForProfileId};
		Query countTotalSoftwareSkillOfUser = DatabaseController.getQuery(select, from, where, null);
		String[][] dataInCountTotalSoftwareSkillOfUser = countTotalSoftwareSkillOfUser.getDataInQuery();
		
		countTotalSoftwareSkillOfUser.printQuery();
		
		int totalNumberOfSoftwareSkill;
		
		if(dataInCountTotalSoftwareSkillOfUser[1][1] == null){
			//if null means student doest not insert a software skill yet
			totalNumberOfSoftwareSkill = 0;
			
		}else {
			totalNumberOfSoftwareSkill = Integer.parseInt(dataInCountTotalSoftwareSkillOfUser[1][1]);
		}
		
		
		//total number of portfolio of a user + 1(So, This PortfolioId will be unique)
		int newSoftwareSkillId = totalNumberOfSoftwareSkill += 1;
		
		String newSoftwareSkillIdString = Integer.toString(newSoftwareSkillId);
				
				
		//add Software Skill into Database
				
		
		String[] values = new String[] {profileId, newSoftwareSkillIdString, skillName, skillLevel};
		
		if(DatabaseController.insertDataIntoTable("Profile_SoftwareSkill", values)) {
			System.out.println("addSoftwareSkillRequest success");
			relatedStringContent = new String[] {"success", newSoftwareSkillIdString, skillName, skillLevel};
		}else {
			System.out.println("addSoftwareSkillRequest Failed");
			relatedStringContent = new String[] {"failed"};
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
		
	}
	
	private ComObject createAPostRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("CreateAPostRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> userName, postTitle, postDesc, jobType, offers, post_skillCategory, location, workHours, postImageBase64EncodedString
		String username = receivedStringContent[0];
		String postTitle = receivedStringContent[1];
		String postDesc = receivedStringContent[2];
		String jobType = receivedStringContent[3];
		String offers = receivedStringContent[4];
		String post_skillCategory = receivedStringContent[5];
		String location = receivedStringContent[6];
		String workHours = receivedStringContent[7];
		String postImageBase64EncodedString = receivedStringContent[8];
		
		
		//Section: Generate a HirepostId
		//in order to insert a new data a new HirepostId need to be generated
		//every User softwareskillid start from 1, if user have 3 softwareskill then softwareskillId will have (1,2,3)
		//generate the new softwareskillId for the user	
		String[] select = {"MAX(CAST(HirePostID AS int)) AS newHirePostId"};
		String[] from = {"Employer_HiringPost"};
		String[] condition = {};
		
		Query newHirePostIdQuery = DatabaseController.getQuery(select, from, null, null);
		
		newHirePostIdQuery.printQuery();
		
		String[][] newHirePostIdQueryData = newHirePostIdQuery.getDataInQuery();
		
		int largestHirePostId = Integer.parseInt(newHirePostIdQueryData[1][1]);
		
		//total number of portfolio of a user + 1(So, This PortfolioId will be unique)
		int newHirePostId = largestHirePostId + 1;
				
		String newHirePostIdString = Integer.toString(newHirePostId);
		
		//Section: add Hire Post into Database
		Date date = new Date();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		
		String currentDateTime = formatter.format(date);  
		
		String[] values;
		if(jobType.equals("PartTime")) {
			values = new String[] {newHirePostIdString, username, postTitle, postDesc, jobType, offers, currentDateTime, post_skillCategory, location, workHours, "ongoing" };
		}else {
			//freelance
			values = new String[] {newHirePostIdString, username, postTitle, postDesc, jobType, offers, currentDateTime, post_skillCategory, null, null, "ongoing"};
		}
		
		if(DatabaseController.insertDataIntoTable("Employer_HiringPost", values)){
			//if added in
			//add postPicture into Employer_HiringPost_Picture;
			
			String[] picValues = new String[] {newHirePostIdString, "1", postImageBase64EncodedString };
			
			if(DatabaseController.insertDataIntoTable("Employer_HiringPost_Picture", picValues)) {
				System.out.println("createAPostRequest success");
				relatedStringContent = new String[] {"success", username, postImageBase64EncodedString, newHirePostIdString, postTitle, postDesc, jobType, offers, currentDateTime, location, workHours, post_skillCategory, "ongoing"};
			}else {
				relatedStringContent = new String[] {"failed"};
			}
			
			
		}else {
			relatedStringContent = new String[] {"failed"};
		}
		
		
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
						
		return replyObj;	
		
		
	}
	
	private ComObject editPostRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("editPostRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> employerUsername,hirePostId, postTitle, postDesc, jobType, offers, post_skillCategory, location, workHours, postImageBase64EncodedString, positionInArrayList_string
		String employerUsername = receivedStringContent[0];
		String hirePostId = receivedStringContent[1];
		String postTitle = receivedStringContent[2];
		String postDesc = receivedStringContent[3];
		String jobType = receivedStringContent[4];
		String offers = receivedStringContent[5];
		String post_skillCategory = receivedStringContent[6];
		String location = receivedStringContent[7];
		String workHours = receivedStringContent[8];
		String postImageBase64EncodedString = receivedStringContent[9];
		String positionInArrayList_string = receivedStringContent[10];
		
		
		
		//Section: edit Hire Post into Database
		
		//after edit the postDateTime will be updated
		Date date = new Date();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		
		String currentDateTime = formatter.format(date);  
		
		
		String[] set;
		String[] condition = new String[] {"HirePostId='" + hirePostId + "'"};
		if(jobType.equals("PartTime")) {
			set = new String[] { "Title='" + postTitle + "'", "Description='" + postDesc + "'"
					, "JobType='" + jobType + "'", "Offers='" + offers + "'", "DateTime_Posted='" + currentDateTime + "'"
					, "Post_SkillCategory='" + post_skillCategory + "'", "Location='" + location + "'", "WorkingHours='" + workHours +"'" };
		}else {
			//freelance
			set = new String[] { "Title='" + postTitle + "'", "Description='" + postDesc + "'"
					, "JobType='" + jobType + "'", "Offers='" + offers + "'", "DateTime_Posted='" + currentDateTime + "'"
					, "Post_SkillCategory='" + post_skillCategory + "'", "Location=NULL", "WorkingHours=NULL"};
		}
		
		if(DatabaseController.updateDataFromTable("Employer_HiringPost", set, condition)){
			//if Edited in
			//edit postPicture into Employer_HiringPost_Picture;
			
			String[] picSet = new String[] {"Picture='" + postImageBase64EncodedString +"'" };
			String[] picCondition = new String[] {"HirePostId='" + hirePostId + "'", "PicId='1'"};
			
			if(DatabaseController.updateDataFromTable("Employer_HiringPost_Picture", picSet, picCondition)) {
				System.out.println("editPostRequest success");
				relatedStringContent = new String[] {"success", employerUsername, postImageBase64EncodedString, hirePostId, postTitle, postDesc, jobType, offers, currentDateTime, location, workHours, post_skillCategory, positionInArrayList_string};
			}else {
				relatedStringContent = new String[] {"failed"};
			}
			
			
		}else {
			relatedStringContent = new String[] {"failed"};
		}
		
		
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
						
		return replyObj;	
		
		
	}
	
	private ComObject deletePostRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("deletePostRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> postId, positionInArrayList_string
		String postId = receivedStringContent[0];
		String positionInArrayList_string = receivedStringContent[1];
		
		String[] condition = new String[] {"HirePostID='" + postId + "'"};
		
		if(DatabaseController.deleteDataFromTable("Employer_HiringPost", condition)) {
			
			if(DatabaseController.deleteDataFromTable("Employer_HiringPost_Picture", condition)) {
				System.out.println("deletePostRequest success");
				relatedStringContent = new String[] {"success", postId, positionInArrayList_string};
				
			}else {
				System.out.println("deletePostRequest Failed");
				relatedStringContent = new String[] {"failed"};
			}
			
		}else {
			System.out.println("deletePostRequest Failed");
			relatedStringContent = new String[] {"failed"};
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
		
	}
	
	private ComObject doneHiringPostRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("doneHiringPostRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> postId, positionInArrayList_string
		String postId = receivedStringContent[0];
		String positionInArrayList_string = receivedStringContent[1];
		
		String[] set = new String[] {"PostStatus='done'"};
		String[] condition = new String[] {"HirePostID='" + postId + "'"};
		
		if(DatabaseController.updateDataFromTable("Employer_HiringPost", set, condition)) {
			System.out.println("doneHiringPostRequest success");
			relatedStringContent = new String[] {"success", positionInArrayList_string};
			
	
		}else {
			System.out.println("doneHiringPostRequest Failed");
			relatedStringContent = new String[] {"failed"};
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
		
	}
	
	private ComObject acceptPostRequestRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("acceptPostRequestRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> hirePostId,requesterUsername,interviewDate,interviewTime,interviewLocation,interviewMessage,positionInArrayList_string
		String hirePostId = receivedStringContent[0];
		String requesterUsername = receivedStringContent[1];
		String interviewDate = receivedStringContent[2];
		String interviewTime = receivedStringContent[3];
		String interviewLocation = receivedStringContent[4];
		String interviewMessage = receivedStringContent[5];
		String positionInArrayList_string = receivedStringContent[6];
		
		//firstly check whether the student request still exist,
		//if doesnt exist means student ady cancel the request
		String[] checkSelect = {"*"};
		String[] checkFrom = {"Hiring_Post_Pending_Request"};
		String[] checkCondition = {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'"};
		
		Query checkQuery = DatabaseController.getQuery(checkSelect, checkFrom, checkCondition, null);
		
		
		if(!checkQuery.EmptyOrNot()) {
			
		

			//get current Date Time
			Date date = new Date();
					
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
					
			String currentDateTime = formatter.format(date);  
			
			
			String[] postRequestSet = new String[] {"Status='accepted'", "Status_added_dateTime='" + currentDateTime + "'"};
			String[] postRequestCondition = new String[] {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'" };
			
			if(DatabaseController.updateDataFromTable("Hiring_Post_Pending_Request", postRequestSet, postRequestCondition)) {
				
				String[] interviewRequestSet = new String[] {"RequestDateTime='" + currentDateTime + "'", "Message='" + interviewMessage + "'"
						,"Date='" + interviewDate + "'", "Time='" + interviewTime + "'", "Location='" + interviewLocation + "'", "Status='pending'"
						, "Status_added_dateTime='" + currentDateTime + "'"};
				String[] interviewRequestCondition = new String[] {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'" };
				
				if(DatabaseController.updateDataFromTable("Hiring_Post_Interview_Request", interviewRequestSet, interviewRequestCondition )) {
					System.out.println("acceptPostRequestRequest success");
					
					relatedStringContent = new String[] {"success",hirePostId,requesterUsername,interviewDate,interviewTime,interviewLocation,interviewMessage,positionInArrayList_string, currentDateTime};
					
					
					//Send notification to student that request
					String stdUsername = requesterUsername;			
					
					String stdToken = FirebaseCloudMessageController.getUserToken(stdUsername);
					String notificationTitle = "You Have A New Interview Request.";
					String notificationDesc = "Interview Infomation:"
							+ "\nDate : " + interviewDate
							+ "\nTime : " + interviewTime
							+ "\nLocation : " + interviewLocation
							+ "\nMessage : " + interviewMessage;
					
					if(!stdToken.equals("")) {
						//if token is found
						NotificationInfo myNotificationInfo = new NotificationInfo(stdToken, notificationTitle, notificationDesc);
						try {
							FirebaseCloudMessageController.pushNotificationToUser(myNotificationInfo);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							System.out.println("Error Pushing Notification to user: " + stdUsername);
							e.printStackTrace();
						}
					}
					
					
					
					//if this request is success, We check whether the employer of this post is online
					//if online we Forward an update request to him
					
					//step 1 : we get the student username from the hirepostid
					
					System.out.println("AcceptPostRequestRequest -> student Username is " + stdUsername);
					if(ServerCore.checkClientOnlineOrNot(stdUsername)) {
						//if the student is online
						
						
						//get their ClientInfo Object
						ClientInfo targetedClientInfo = ServerCore.getClientInfoFromArrayList(stdUsername);
						String[] select = {"ehp.username", "ehp.hirepostid", "hppPro.firstname", "hppPro.lastname", "hpp.requestdateTime", "hpp.message", "hpp.status", "hpi.requestDateTime", "hpi.requesterUsername", "hpi.message", "hpi.date", "hpi.time", "hpi.location", "hpi.status", "hpp.Status_added_dateTime", "p.profilepic", "ehp_pic.picture", "ehp.hirepostid", "p.firstname", "p.lastname", "ehp.title", "ehp.description", "ehp.jobtype", "ehp.offers", "ehp.datetime_posted", "ehp.location", "ehp.workingHours", "ehp.Post_SkillCategory" };
						String[] from = {"employer_hiringpost ehp", "Hiring_Post_Pending_Request hpp", "Hiring_Post_interview_Request hpi", "profile p", "user u", "employer_hiringpost_picture ehp_pic", "profile hppPro", "user hppuser"};
						String[] condition = {"ehp.hirepostid=hpp.hirepostid", "ehp.hirepostid=hpi.hirepostid", "ehp.hirepostid='" + hirePostId+ "'", "hpp.requesterUsername=hpi.requesterUsername", "hppPro.profileid=hppuser.profileid", "hppuser.username=hpp.requesterusername", "hpp.requesterUsername='" + stdUsername + "'", "ehp.username=u.username", "u.username=ehp.username", "u.profileid=p.profileid", "ehp_pic.hirepostid=ehp.hirepostid" };
						String[] additionCommand = {"group by ehp.hirepostid, hpp.requesterUsername", "order by hpp.Status_added_dateTime DESC, ehp.hirepostid DESC"};
						Query resultQuery = DatabaseController.getQuery(select, from, condition, additionCommand);//store result of Query
							
									
						resultQuery.printQuery();
						
						Query[] stdUpdatePostRequestStatusRelatedQueryContent = new Query[] {resultQuery};
					
						if(targetedClientInfo != null) {
							ComObject updateEmpComObj = new ComObject("ServerRequest", "stdUpdatePostRequestStatus", stdUpdatePostRequestStatusRelatedQueryContent);
							
							ClientHandler targetedClientHandler = targetedClientInfo.getClientHandler();
							targetedClientHandler.getClientSender().addObjectIntoSentQueue(updateEmpComObj);
						}
						
						
					}
					
					
				}else {
					System.out.println("acceptPostRequestRequest Failed");
					relatedStringContent = new String[] {"failed"};
				}
				
			}else {
				System.out.println("acceptPostRequestRequest Failed");
				relatedStringContent = new String[] {"failed"};
			}
		}else {
			System.out.println("acceptPostRequestRequest Failed => postRequestNotExist");
			relatedStringContent = new String[] {"postRequestNotExist"};
		}
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
		
	}
	
	private ComObject reviseInterviewRequestRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("reviseInterviewRequestRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> hirePostId,requesterUsername,interviewDate,interviewTime,interviewLocation,interviewMessage,positionInArrayList_string
		String hirePostId = receivedStringContent[0];
		String requesterUsername = receivedStringContent[1];
		String interviewDate = receivedStringContent[2];
		String interviewTime = receivedStringContent[3];
		String interviewLocation = receivedStringContent[4];
		String interviewMessage = receivedStringContent[5];
		String positionInArrayList_string = receivedStringContent[6];
		
		//firstly check whether the InterviewRequest is still pending
		//if student already rejected or accepted the interview request, Interview Timetable cannot be revised
		String[] checkSelect = {"Status", "Date", "Time", "Location"};
		String[] checkFrom = {"Hiring_Post_Interview_Request"};
		String[] checkCondition = {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'"};
		
		Query checkQuery = DatabaseController.getQuery(checkSelect, checkFrom, checkCondition, null);
		String[][] checkQuery2dData = checkQuery.getDataInQuery();
		
		if(checkQuery2dData[1][1].equals("pending")) {
			//if still pending, revise Interview Timetable can be done
			
			//store old interview info, we will use to push notification to the student
			String oldInterviewDate = checkQuery2dData[1][2];
			String oldInterviewTime = checkQuery2dData[1][3];
			String oldInterviewLocation = checkQuery2dData[1][4];
			
			
			//get current Date Time
			Date date = new Date();
					
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
					
			String currentDateTime = formatter.format(date);  
			
			
			String[] interviewRequestSet = new String[] {"RequestDateTime='" + currentDateTime + "'", "Message='" + interviewMessage + "'"
					,"Date='" + interviewDate + "'", "Time='" + interviewTime + "'", "Location='" + interviewLocation + "'", "Status='pending'"
					, "Status_added_dateTime='" + currentDateTime + "'"};
			String[] interviewRequestCondition = new String[] {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'" };
			
			if(DatabaseController.updateDataFromTable("Hiring_Post_Interview_Request", interviewRequestSet, interviewRequestCondition )) {
				System.out.println("reviseInterviewRequestRequest success");
				
				relatedStringContent = new String[] {"success",hirePostId,requesterUsername,interviewDate,interviewTime,interviewLocation,interviewMessage,positionInArrayList_string, currentDateTime};
				
				
				//Send notification to student that request
				String stdUsername = requesterUsername;			
				
				String stdToken = FirebaseCloudMessageController.getUserToken(stdUsername);
				String notificationTitle = "Your Interview Request Has Been Revised! Please Accept Or Decline";
				String notificationDesc = "Interview Infomation:"
						+ "\nOld Date : " + oldInterviewDate + " -> New Date : " + interviewDate
						+ "\nOld Time : " + oldInterviewTime + " -> New Time : " + interviewTime
						+ "\nOld Location : " + oldInterviewLocation + " -> New Location : " + interviewLocation;
				
				if(!stdToken.equals("")) {
					//if token is found
					NotificationInfo myNotificationInfo = new NotificationInfo(stdToken, notificationTitle, notificationDesc);
					try {
						FirebaseCloudMessageController.pushNotificationToUser(myNotificationInfo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("Error Pushing Notification to user: " + stdUsername);
						e.printStackTrace();
					}
				}
				
				
				
				//if this request is success, We check whether the employer of this post is online
				//if online we Forward an update request to him
				
				//step 1 : we get the student username from the hirepostid
				
				System.out.println("reviseInterviewRequestRequest -> student Username is " + stdUsername);
				if(ServerCore.checkClientOnlineOrNot(stdUsername)) {
					//if the student is online
					
					
					//get their ClientInfo Object
					ClientInfo targetedClientInfo = ServerCore.getClientInfoFromArrayList(stdUsername);
					String[] select = {"ehp.username", "ehp.hirepostid", "hppPro.firstname", "hppPro.lastname", "hpp.requestdateTime", "hpp.message", "hpp.status", "hpi.requestDateTime", "hpi.requesterUsername", "hpi.message", "hpi.date", "hpi.time", "hpi.location", "hpi.status", "hpp.Status_added_dateTime", "p.profilepic", "ehp_pic.picture", "ehp.hirepostid", "p.firstname", "p.lastname", "ehp.title", "ehp.description", "ehp.jobtype", "ehp.offers", "ehp.datetime_posted", "ehp.location", "ehp.workingHours", "ehp.Post_SkillCategory" };
					String[] from = {"employer_hiringpost ehp", "Hiring_Post_Pending_Request hpp", "Hiring_Post_interview_Request hpi", "profile p", "user u", "employer_hiringpost_picture ehp_pic", "profile hppPro", "user hppuser"};
					String[] condition = {"ehp.hirepostid=hpp.hirepostid", "ehp.hirepostid=hpi.hirepostid", "ehp.hirepostid='" + hirePostId+ "'", "hpp.requesterUsername=hpi.requesterUsername", "hppPro.profileid=hppuser.profileid", "hppuser.username=hpp.requesterusername", "hpp.requesterUsername='" + stdUsername + "'", "ehp.username=u.username", "u.username=ehp.username", "u.profileid=p.profileid", "ehp_pic.hirepostid=ehp.hirepostid" };
					String[] additionCommand = {"group by ehp.hirepostid, hpp.requesterUsername", "order by hpp.Status_added_dateTime DESC, ehp.hirepostid DESC"};
					Query resultQuery = DatabaseController.getQuery(select, from, condition, additionCommand);//store result of Query
						
								
					resultQuery.printQuery();
					
					Query[] stdUpdatePostRequestStatusRelatedQueryContent = new Query[] {resultQuery};
				
					if(targetedClientInfo != null) {
						ComObject updateEmpComObj = new ComObject("ServerRequest", "stdUpdatePostRequestStatus", stdUpdatePostRequestStatusRelatedQueryContent);
						
						ClientHandler targetedClientHandler = targetedClientInfo.getClientHandler();
						targetedClientHandler.getClientSender().addObjectIntoSentQueue(updateEmpComObj);
					}
					
					
				}
				
				
			}else {
				System.out.println("reviseInterviewRequestRequest Failed");
				relatedStringContent = new String[] {"failed"};
			}
				
			
		}else {
			System.out.println("reviseInterviewRequestRequest Failed => postRequestNotExist");
			relatedStringContent = new String[] {"alreadyAcceptOrReject"};
		}
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
		
	}
	
	private ComObject rejectPostRequestRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("rejectPostRequestRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> hirePostId,requesterUsername, positionInArrayList_string
		String hirePostId = receivedStringContent[0];
		String requesterUsername = receivedStringContent[1];
		String positionInArrayList_string = receivedStringContent[2];
		
		
		//firstly check whether the student request still exist,
		//if doesnt exist means student ady cancel the request
		String[] checkSelect = {"*"};
		String[] checkFrom = {"Hiring_Post_Pending_Request"};
		String[] checkCondition = {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'"};
			
		Query checkQuery = DatabaseController.getQuery(checkSelect, checkFrom, checkCondition, null);
				
				
		if(!checkQuery.EmptyOrNot()) {
			//get current Date Time
			Date date = new Date();
					
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
					
			String currentDateTime = formatter.format(date);  
			
			
			String[] postRequestSet = new String[] {"Status='rejected'", "Status_added_dateTime='" + currentDateTime + "'"};
			String[] postRequestCondition = new String[] {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'" };
			
			if(DatabaseController.updateDataFromTable("Hiring_Post_Pending_Request", postRequestSet, postRequestCondition)) {
				
				System.out.println("rejectPostRequestRequest success");
				
				relatedStringContent = new String[] {"success", hirePostId, requesterUsername, positionInArrayList_string, currentDateTime};
				
				
				//get The Hiring Post info for the use in notification
				String[] notificationSelect = {"JobType","Title", "Offers"};
				String[] notificationFrom = {"Employer_HiringPost"};
				String[] notificationWhere = {"HirePostID='" + hirePostId + "'"};
				
				Query notificationQuery = DatabaseController.getQuery(notificationSelect, notificationFrom, notificationWhere, null);
				String[][] notificationQuery2dDataArray = notificationQuery.getDataInQuery();
				
				String jobTypeForNotification = notificationQuery2dDataArray[1][1];
				String titleForNotification = notificationQuery2dDataArray[1][2];
				String offersForNotification = notificationQuery2dDataArray[1][3];
				
				//Send notification to student that request
				String stdUsername = requesterUsername;			
				
				String stdToken = FirebaseCloudMessageController.getUserToken(stdUsername);
				String notificationTitle = "Your Request To Hiring Post Is Rejected";
				String notificationDesc = "Rejected Post Infomation:"
						+ "\nJobType : " + jobTypeForNotification
						+ "\nPost Title : " + titleForNotification
						+ "\nPost Offers : " + offersForNotification;
				
				if(!stdToken.equals("")) {
					//if token is found
					NotificationInfo myNotificationInfo = new NotificationInfo(stdToken, notificationTitle, notificationDesc);
					try {
						FirebaseCloudMessageController.pushNotificationToUser(myNotificationInfo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("Error Pushing Notification to user: " + stdUsername);
						e.printStackTrace();
					}
				}
				
				
				//if this request is success, We check whether the employer of this post is online
				//if online we Forward an update request to him
							
				
				System.out.println("rejectPostRequestRequest -> student Username is " + stdUsername);
				if(ServerCore.checkClientOnlineOrNot(stdUsername)) {
					//if the student is online
					
					
					//get their ClientInfo Object
					ClientInfo targetedClientInfo = ServerCore.getClientInfoFromArrayList(stdUsername);
					String[] select = {"ehp.hirepostid", "hppPro.firstname", "hppPro.lastname", "hpp.requestdateTime", "hpp.message", "hpp.status", "hpi.requestDateTime", "hpi.requesterUsername", "hpi.message", "hpi.date", "hpi.time", "hpi.location", "hpi.status", "hpp.Status_added_dateTime", "p.profilepic", "ehp_pic.picture", "ehp.hirepostid", "p.firstname", "p.lastname", "ehp.title", "ehp.description", "ehp.jobtype", "ehp.offers", "ehp.datetime_posted", "ehp.location", "ehp.workingHours", "ehp.Post_SkillCategory" };
					String[] from = {"employer_hiringpost ehp", "Hiring_Post_Pending_Request hpp", "Hiring_Post_interview_Request hpi", "profile p", "user u", "employer_hiringpost_picture ehp_pic", "profile hppPro", "user hppuser"};
					String[] condition = {"ehp.hirepostid=hpp.hirepostid", "ehp.hirepostid=hpi.hirepostid", "ehp.hirepostid='" + hirePostId+ "'", "hpp.requesterUsername=hpi.requesterUsername", "hppPro.profileid=hppuser.profileid", "hppuser.username=hpp.requesterusername", "hpp.requesterUsername='" + stdUsername + "'", "ehp.username=u.username", "u.username=ehp.username", "u.profileid=p.profileid", "ehp_pic.hirepostid=ehp.hirepostid" };
					String[] additionCommand = {"group by ehp.hirepostid, hpp.requesterUsername", "order by hpp.Status_added_dateTime DESC, ehp.hirepostid DESC"};
					Query resultQuery = DatabaseController.getQuery(select, from, condition, additionCommand);//store result of Query
						
								
					resultQuery.printQuery();
					
					Query[] stdUpdatePostRequestStatusRelatedQueryContent = new Query[] {resultQuery};
				
					if(targetedClientInfo != null) {
						ComObject updateEmpComObj = new ComObject("ServerRequest", "stdUpdatePostRequestStatus", stdUpdatePostRequestStatusRelatedQueryContent);
						
						ClientHandler targetedClientHandler = targetedClientInfo.getClientHandler();
						targetedClientHandler.getClientSender().addObjectIntoSentQueue(updateEmpComObj);
					}
					
					
				}
				
				
			}else {
				System.out.println("rejectPostRequestRequest Failed");
				relatedStringContent = new String[] {"failed"};
			}
		}else {
			
			System.out.println("rejectPostRequestRequest Failed => postRequestNotExist");
			relatedStringContent = new String[] {"postRequestNotExist"};
			
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
		
	}
	
	private ComObject studentCancelPostRequestRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("studentCancelPostRequestRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> hirePostId,requesterUsername, positionInArrayList_string
		String hirePostId = receivedStringContent[0];
		String requesterUsername = receivedStringContent[1];
		String positionInArrayList_string = receivedStringContent[2];
		
		
		
		String[] deletePostRequestCondition = new String[] {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'" };
		
		if(DatabaseController.deleteDataFromTable("Hiring_Post_Pending_Request", deletePostRequestCondition)) {
			
			String[] deleteInterviewRequestCondition = new String[] {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'" };
			
			if(DatabaseController.deleteDataFromTable("Hiring_Post_Interview_Request", deleteInterviewRequestCondition)) {
				System.out.println("studentCancelPostRequestRequest success");
			
				relatedStringContent = new String[] {"success", hirePostId, positionInArrayList_string};
				
				//if this request is success, We check whether the employer of this post is online
				//if online we Forward an update request to him
				
				//step 1 : we get the employer username from the hirepostid
				String[] empSelect = {"username"};
				String[] empFrom = {"Employer_HiringPost"};
				String[] empCondition = {"HirePostID='" + hirePostId + "'"};
				
				Query empQuery = DatabaseController.getQuery(empSelect, empFrom, empCondition, null);
				String[][] empQuery2dDataArray = empQuery.getDataInQuery();
				
				//section:notification!
				//get the Student Name for the notification
				String[] stdNameSelect = {"p.FirstName","p.LastName"};
				String[] stdNameFrom = {"user u", "profile p"};
				String[] stdNameWhere = {"u.ProfileID=p.ProfileId", "u.username='" + requesterUsername + "'"};
				
				Query stdNameQuery = DatabaseController.getQuery(stdNameSelect, stdNameFrom, stdNameWhere, null);
				String[][] stdNameQuery2dDataArray = stdNameQuery.getDataInQuery();
				
				String studentFullName = stdNameQuery2dDataArray[1][1] + " " + stdNameQuery2dDataArray[1][2];
								
				
				//get the hire post info for the notification
				String[] notificationSelect = {"JobType","Title", "Offers"};
				String[] notificationFrom = {"Employer_HiringPost"};
				String[] notificationWhere = {"HirePostID='" + hirePostId + "'"};
				
				Query notificationQuery = DatabaseController.getQuery(notificationSelect, notificationFrom, notificationWhere, null);
				String[][] notificationQuery2dDataArray = notificationQuery.getDataInQuery();
				
				String jobTypeForNotification = notificationQuery2dDataArray[1][1];
				String titleForNotification = notificationQuery2dDataArray[1][2];
				String offersForNotification = notificationQuery2dDataArray[1][3];
				
				//Send notification to employer that request
				String empUsername = empQuery2dDataArray[1][1];;			
				
				String empToken = FirebaseCloudMessageController.getUserToken(empUsername);
				String notificationTitle = studentFullName + " Has Canceled his/her Request In Your Post";
				String notificationDesc = "Post Infomation:"
						+ "\nJobType : " + jobTypeForNotification
						+ "\nPost Title : " + titleForNotification
						+ "\nPost Offers : " + offersForNotification;
				
				if(!empToken.equals("")) {
					//if token is found
					NotificationInfo myNotificationInfo = new NotificationInfo(empToken, notificationTitle, notificationDesc);
					try {
						FirebaseCloudMessageController.pushNotificationToUser(myNotificationInfo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("Error Pushing Notification to user: " + empToken);
						e.printStackTrace();
					}
				}
				
				System.out.println("studentCancelPostRequestRequest -> employer Username is " + empUsername);
				if(ServerCore.checkClientOnlineOrNot(empUsername)) {
					//if the employer is online
					
					//get their ClientInfo Object
					ClientInfo targetedClientInfo = ServerCore.getClientInfoFromArrayList(empUsername);
					
										
					String[] deleteEmpRequestRelatedQueryContent = new String[] {hirePostId, requesterUsername};
					
					if(targetedClientInfo != null) {
						ComObject updateEmpComObj = new ComObject("ServerRequest", "deleteEmployerPostRequestRequest", deleteEmpRequestRelatedQueryContent);
						
						ClientHandler targetedClientHandler = targetedClientInfo.getClientHandler();
						targetedClientHandler.getClientSender().addObjectIntoSentQueue(updateEmpComObj);
					}
					
				}
				
				
			}else {
				System.out.println("studentCancelPostRequestRequest Failed");
				
				relatedStringContent = new String[] {"failed"};
			}
			
			
			
		}else {
			System.out.println("studentCancelPostRequestRequest Failed");
			relatedStringContent = new String[] {"failed"};
		}
		
		//create a new Communication Object that include the relatedstring(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
		
	}
	
	private ComObject studentAcceptInterviewRequestRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("studentAcceptInterviewRequestRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> hirePostId,requesterUsername,positionInArrayList_string
		String hirePostId = receivedStringContent[0];
		String requesterUsername = receivedStringContent[1];
		String positionInArrayList_string = receivedStringContent[2];
		

		//get current Date Time
		Date date = new Date();
				
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
				
		String currentDateTime = formatter.format(date);  
		
		//update post request time
		String[] postRequestSet = new String[] { "Status_added_dateTime='" + currentDateTime + "'"};
		String[] postRequestCondition = new String[] {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'" };
		
		if(DatabaseController.updateDataFromTable("Hiring_Post_Pending_Request", postRequestSet, postRequestCondition)) {
			
			String[] interviewRequestSet = new String[] {"Status='accepted'", "Status_added_dateTime='" + currentDateTime + "'"};
			String[] interviewRequestCondition = new String[] {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'" };
			
			if(DatabaseController.updateDataFromTable("Hiring_Post_Interview_Request", interviewRequestSet, interviewRequestCondition )) {
				System.out.println("studentAcceptInterviewRequestRequest success");
				
				relatedStringContent = new String[] {"success",hirePostId,requesterUsername, positionInArrayList_string, currentDateTime};
				
				//section:notification!
				//step 1 : we get the employer username from the hirepostid
				String[] empSelect = {"username"};
				String[] empFrom = {"Employer_HiringPost"};
				String[] empCondition = {"HirePostID='" + hirePostId + "'"};
				
				Query empQuery = DatabaseController.getQuery(empSelect, empFrom, empCondition, null);
				String[][] empQuery2dDataArray = empQuery.getDataInQuery();
				
				String empUsername = empQuery2dDataArray[1][1];
				
				//get the Student Name for the notification
							
				String[] stdNameSelect = {"p.FirstName","p.LastName"};
				String[] stdNameFrom = {"user u", "profile p"};
				String[] stdNameWhere = {"u.ProfileID=p.ProfileId", "u.username='" + requesterUsername + "'"};
				
				Query stdNameQuery = DatabaseController.getQuery(stdNameSelect, stdNameFrom, stdNameWhere, null);
				String[][] stdNameQuery2dDataArray = stdNameQuery.getDataInQuery();
				
				String studentFullName = stdNameQuery2dDataArray[1][1] + " " + stdNameQuery2dDataArray[1][2];
								
				
				
				//get the hire post info for the notification
				String[] notificationSelect = {"JobType","Title", "Offers"};
				String[] notificationFrom = {"Employer_HiringPost"};
				String[] notificationWhere = {"HirePostID='" + hirePostId + "'"};
				
				Query notificationQuery = DatabaseController.getQuery(notificationSelect, notificationFrom, notificationWhere, null);
				String[][] notificationQuery2dDataArray = notificationQuery.getDataInQuery();
				
				String jobTypeForNotification = notificationQuery2dDataArray[1][1];
				String titleForNotification = notificationQuery2dDataArray[1][2];
				String offersForNotification = notificationQuery2dDataArray[1][3];
				
				//get the interview info for the notification
				String[] interviewRequestSelect = {"Date","Time", "Location"};
				String[] interviewRequestFrom = {"Hiring_Post_Interview_Request"};
				String[] interviewRequestWhere = {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'" };
				
				Query interviewRequestQuery = DatabaseController.getQuery(interviewRequestSelect, interviewRequestFrom, interviewRequestWhere, null);
				String[][] interviewRequestQuery2dDataArray = interviewRequestQuery.getDataInQuery();
				
				String interviewRequestDate = interviewRequestQuery2dDataArray[1][1];
				String interviewRequestTime = interviewRequestQuery2dDataArray[1][2];
				String interviewRequestLocation = interviewRequestQuery2dDataArray[1][3];
				
				//Send notification to employer that request
								
				String empToken = FirebaseCloudMessageController.getUserToken(empUsername);
				String notificationTitle = studentFullName + " Has Accepted Your Interview Request";
				String notificationDesc = "Interview Infomation:"
						+ "\nDate : " + interviewRequestDate
						+ "\nTime : " + interviewRequestTime
						+ "\nLocation : " + interviewRequestLocation
						+"\n\nPost Infomation:"
						+ "\nJobType : " + jobTypeForNotification
						+ "\nPost Title : " + titleForNotification
						+ "\nOffers : " + offersForNotification;
				
				if(!empToken.equals("")) {
					//if token is found
					NotificationInfo myNotificationInfo = new NotificationInfo(empToken, notificationTitle, notificationDesc);
					try {
						FirebaseCloudMessageController.pushNotificationToUser(myNotificationInfo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("Error Pushing Notification to user: " + empToken);
						e.printStackTrace();
					}
				}
				
				
				
				//if this request is success, We check whether the employer of this post is online
				//if online we Forward an update request to him
				
				
				System.out.println("studentDeclineInterviewRequestRequest -> employer Username is " + empUsername);
				if(ServerCore.checkClientOnlineOrNot(empUsername)) {
					//if the employer is online
					
					//get their ClientInfo Object
					ClientInfo targetedClientInfo = ServerCore.getClientInfoFromArrayList(empUsername);
					
					String[] empUpdateSelect = {"ehp.hirepostid","hpp.requestdateTime", "hpp.requesterUsername", "p.firstname", "p.lastname", "p.profilepic", "p.GeneralSkill_Category", "hpp.message", "hpp.status", "hpi.requestDateTime", "hpi.requesterUsername", "hpi.message", "hpi.date", "hpi.time", "hpi.location", "hpi.status", "hpp.Status_added_dateTime" };
					String[] empUpdateFrom = {"employer_hiringpost ehp", "Hiring_Post_Pending_Request hpp", "Hiring_Post_interview_Request hpi", "profile p", "user u"};
					String[] empUpdateCondition = {"ehp.hirepostid=hpp.hirepostid", "ehp.hirepostid='" + hirePostId + "'", "hpp.requesterUsername='" + requesterUsername + "'", "ehp.hirepostid=hpi.hirepostid", "hpp.requesterUsername=hpi.requesterUsername", "u.username=hpp.requesterUsername", "u.profileid=p.profileid", "u.username=hpi.requesterUsername"};
	
					Query empUpdateQuery = DatabaseController.getQuery(empUpdateSelect, empUpdateFrom, empUpdateCondition, null);//store result of Query
													
					empUpdateQuery.printQuery();
					
					Query[] updateEmpRelatedQueryContent = new Query[] {empUpdateQuery};
					
					if(targetedClientInfo != null) {
						ComObject updateEmpComObj = new ComObject("ServerRequest", "updateEmployerInterviewRequestStatusRequest", updateEmpRelatedQueryContent);
						
						ClientHandler targetedClientHandler = targetedClientInfo.getClientHandler();
						targetedClientHandler.getClientSender().addObjectIntoSentQueue(updateEmpComObj);
					}
					
				}
			}else {
				System.out.println("studentAcceptInterviewRequestRequest Failed");
				relatedStringContent = new String[] {"failed"};
			}
			
		}else {
			System.out.println("studentAcceptInterviewRequestRequest Failed");
			relatedStringContent = new String[] {"failed"};
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
		
	}
	
	private ComObject studentDeclineInterviewRequestRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("studentDeclineInterviewRequestRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
				
		//get received Info
		//receivedStringContent Array Content as follow -> hirePostId,requesterUsername,positionInArrayList_string
		String hirePostId = receivedStringContent[0];
		String requesterUsername = receivedStringContent[1];
		String positionInArrayList_string = receivedStringContent[2];
		

		//get current Date Time
		Date date = new Date();
				
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
				
		String currentDateTime = formatter.format(date);  
		
		//update post request time
		String[] postRequestSet = new String[] { "Status_added_dateTime='" + currentDateTime + "'"};
		String[] postRequestCondition = new String[] {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'" };
		
		if(DatabaseController.updateDataFromTable("Hiring_Post_Pending_Request", postRequestSet, postRequestCondition)) {
			
			String[] interviewRequestSet = new String[] {"Status='rejected'", "Status_added_dateTime='" + currentDateTime + "'"};
			String[] interviewRequestCondition = new String[] {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'" };
			
			if(DatabaseController.updateDataFromTable("Hiring_Post_Interview_Request", interviewRequestSet, interviewRequestCondition )) {
				System.out.println("studentDeclineInterviewRequestRequest success");
				
				relatedStringContent = new String[] {"success",hirePostId,requesterUsername, positionInArrayList_string, currentDateTime};
				
				
				//if this request is success, We check whether the employer of this post is online
				//if online we Forward an update request to him
				
				//step 1 : we get the employer username from the hirepostid
				String[] empSelect = {"username"};
				String[] empFrom = {"Employer_HiringPost"};
				String[] empCondition = {"HirePostID='" + hirePostId + "'"};
				
				Query empQuery = DatabaseController.getQuery(empSelect, empFrom, empCondition, null);
				String[][] empQuery2dDataArray = empQuery.getDataInQuery();
				
				String empUsername = empQuery2dDataArray[1][1];		
				
				//section:notification!
				//get the Student Name for the notification
				String[] stdNameSelect = {"p.FirstName","p.LastName"};
				String[] stdNameFrom = {"user u", "profile p"};
				String[] stdNameWhere = {"u.ProfileID=p.ProfileId", "u.username='" + requesterUsername + "'"};
				
				Query stdNameQuery = DatabaseController.getQuery(stdNameSelect, stdNameFrom, stdNameWhere, null);
				String[][] stdNameQuery2dDataArray = stdNameQuery.getDataInQuery();
				
				String studentFullName = stdNameQuery2dDataArray[1][1] + " " + stdNameQuery2dDataArray[1][2];
								
				
				//get the hire post info for the notification
				String[] notificationSelect = {"JobType","Title", "Offers"};
				String[] notificationFrom = {"Employer_HiringPost"};
				String[] notificationWhere = {"HirePostID='" + hirePostId + "'"};
				
				Query notificationQuery = DatabaseController.getQuery(notificationSelect, notificationFrom, notificationWhere, null);
				String[][] notificationQuery2dDataArray = notificationQuery.getDataInQuery();
				
				String jobTypeForNotification = notificationQuery2dDataArray[1][1];
				String titleForNotification = notificationQuery2dDataArray[1][2];
				String offersForNotification = notificationQuery2dDataArray[1][3];
				
				//Send notification to employer that request
							
				String empToken = FirebaseCloudMessageController.getUserToken(empUsername);
				String notificationTitle = studentFullName + " Has Rejected Your Interview Request";
				String notificationDesc = "Post Infomation:"
						+ "\nJobType : " + jobTypeForNotification
						+ "\nPost Title : " + titleForNotification
						+ "\nPost Offers : " + offersForNotification;
				
				if(!empToken.equals("")) {
					//if token is found
					NotificationInfo myNotificationInfo = new NotificationInfo(empToken, notificationTitle, notificationDesc);
					try {
						FirebaseCloudMessageController.pushNotificationToUser(myNotificationInfo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("Error Pushing Notification to user: " + empToken);
						e.printStackTrace();
					}
				}
				
				
				System.out.println("studentDeclineInterviewRequestRequest -> employer Username is " + empUsername);
				if(ServerCore.checkClientOnlineOrNot(empUsername)) {
					//if the employer is online
					
					//get their ClientInfo Object
					ClientInfo targetedClientInfo = ServerCore.getClientInfoFromArrayList(empUsername);
					
					String[] empUpdateSelect = {"ehp.hirepostid","hpp.requestdateTime", "hpp.requesterUsername", "p.firstname", "p.lastname", "p.profilepic", "p.GeneralSkill_Category", "hpp.message", "hpp.status", "hpi.requestDateTime", "hpi.requesterUsername", "hpi.message", "hpi.date", "hpi.time", "hpi.location", "hpi.status", "hpp.Status_added_dateTime" };
					String[] empUpdateFrom = {"employer_hiringpost ehp", "Hiring_Post_Pending_Request hpp", "Hiring_Post_interview_Request hpi", "profile p", "user u"};
					String[] empUpdateCondition = {"ehp.hirepostid=hpp.hirepostid", "ehp.hirepostid='" + hirePostId + "'", "hpp.requesterUsername='" + requesterUsername + "'", "ehp.hirepostid=hpi.hirepostid", "hpp.requesterUsername=hpi.requesterUsername", "u.username=hpp.requesterUsername", "u.profileid=p.profileid", "u.username=hpi.requesterUsername"};
	
					Query empUpdateQuery = DatabaseController.getQuery(empUpdateSelect, empUpdateFrom, empUpdateCondition, null);//store result of Query
													
					empUpdateQuery.printQuery();
					
					Query[] updateEmpRelatedQueryContent = new Query[] {empUpdateQuery};
					
					if(targetedClientInfo != null) {
						ComObject updateEmpComObj = new ComObject("ServerRequest", "updateEmployerInterviewRequestStatusRequest", updateEmpRelatedQueryContent);
						
						ClientHandler targetedClientHandler = targetedClientInfo.getClientHandler();
						targetedClientHandler.getClientSender().addObjectIntoSentQueue(updateEmpComObj);
					}
					
				}
				
			}else {
				System.out.println("studentDeclineInterviewRequestRequest Failed");
				relatedStringContent = new String[] {"failed"};
			}
			
		}else {
			System.out.println("studentDeclineInterviewRequestRequest Failed");
			relatedStringContent = new String[] {"failed"};
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
				
		return replyObj;
		
		
	}
	
	private ComObject applyEmployerPostRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("applyEmployerPostRequest function performing");
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;//used to store the result(ex: OK, Failed)
		Query[] relatedQueryContent = null;	//used to store the Query
		
		
		//get received Info
		//receivedStringContent Array Content as follow -> hirePostId,requesterUsername, leaveAMessage
		String hirePostId = receivedStringContent[0];
		String requesterUsername = receivedStringContent[1];
		String leaveAMessage = receivedStringContent[2];
		
		
		

		//firstly check whether this post is still hiring or exist		
		String[] PostCheckSelect = {"postStatus"};
		String[] PostCheckFrom = {"Employer_HiringPost"};
		String[] PostCheckCondition = {"HirePostID='" + hirePostId + "'"};
		
		Query postCheckQuery = DatabaseController.getQuery(PostCheckSelect, PostCheckFrom, PostCheckCondition, null);
		
		
		//make sure the post is not deleted
		if(!postCheckQuery.EmptyOrNot()) {
			
			String[][] postCheckQueryData2DArray = postCheckQuery.getDataInQuery();
			
			
			if(postCheckQueryData2DArray[1][1].equals("ongoing")) {
				//if the post is still ongoing - > means hiring
				
			
				//secondly Check when User Has already apply for this post. 
				//Note: Each Post Can only be apply once.For a user				
				
				String[] select = {"*"};
				String[] from = {"Hiring_Post_Pending_Request"};
				String[] condition = {"HirePostID='" + hirePostId + "'", "RequesterUsername='" + requesterUsername + "'"};
						
				
				Query checkQuery = DatabaseController.getQuery(select, from, condition, null);
				
				
				if(checkQuery.EmptyOrNot()) {
					//if this query is empty means We can proceed to help user to apply for that post
				
					//get current Date Time
					Date date = new Date();
							
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
							
					String currentDateTime = formatter.format(date);  
					
					//update post request time
					//post_pending_request table column : HirePostID, RequestDateTime, RequesterUsername, Message, Status, Status_added_dateTime
					String[] postValues = new String[] { hirePostId, currentDateTime, requesterUsername, leaveAMessage, "pending", currentDateTime };
					
					if(DatabaseController.insertDataIntoTable("Hiring_Post_Pending_Request", postValues)) {
						//if success 
						//Note: Every Time We enter a record To Hiring_Post_Pending_Request, A Empty Record should also Entered in Hiring_Post_Interview_Request
						//post_pending_request table column : HirePostID, RequestDateTime, RequesterUsername, Message, Date, Time, Location, Status, Status_added_dateTime
						String[] interviewValues = new String[] {hirePostId, null, requesterUsername, null, null, null, null, null, null};
						
						if(DatabaseController.insertDataIntoTable("Hiring_Post_Interview_Request", interviewValues )) {
							System.out.println("applyEmployerPostRequest success");
							
							//get the query of current record
							String[] recordSelect = {"ehp.username", "ehp.hirepostid", "hppPro.firstname", "hppPro.lastname", "hpp.requestdateTime", "hpp.message", "hpp.status", "hpi.requestDateTime", "hpi.requesterUsername", "hpi.message", "hpi.date", "hpi.time", "hpi.location", "hpi.status", "hpp.Status_added_dateTime", "p.profilepic", "ehp_pic.picture", "ehp.hirepostid", "p.firstname", "p.lastname", "ehp.title", "ehp.description", "ehp.jobtype", "ehp.offers", "ehp.datetime_posted", "ehp.location", "ehp.workingHours", "ehp.Post_SkillCategory" };
							String[] recordFrom = {"employer_hiringpost ehp", "Hiring_Post_Pending_Request hpp", "Hiring_Post_interview_Request hpi", "profile p", "user u", "employer_hiringpost_picture ehp_pic", "profile hppPro", "user hppuser"};
							String[] recordCondition = {"ehp.hirepostid=hpp.hirepostid", "ehp.hirepostid=hpi.hirepostid", "hpp.requesterUsername=hpi.requesterUsername", "hppPro.profileid=hppuser.profileid", "hppuser.username=hpp.requesterusername", "hpp.requesterUsername='" + requesterUsername + "'", "hpp.HirePostId='" + hirePostId + "'","ehp.username=u.username", "u.username=ehp.username", "u.profileid=p.profileid", "ehp_pic.hirepostid=ehp.hirepostid" };
							String[] recordadditionCommand = {"group by ehp.hirepostid, hpp.requesterUsername", "order by hpp.Status_added_dateTime DESC, ehp.hirepostid DESC"};
							Query resultQuery = DatabaseController.getQuery(recordSelect, recordFrom, recordCondition, recordadditionCommand);//store result of Query
								
							
							relatedStringContent = new String[] {"success"};
							relatedQueryContent = new Query[] {resultQuery};
							
							
							
							
							//step 1 : we get the employer username from the hirepostid
							String[] empSelect = {"username"};
							String[] empFrom = {"Employer_HiringPost"};
							String[] empCondition = {"HirePostID='" + hirePostId + "'"};
							
							Query empQuery = DatabaseController.getQuery(empSelect, empFrom, empCondition, null);
							String[][] empQuery2dDataArray = empQuery.getDataInQuery();
							
							String empUsername = empQuery2dDataArray[1][1];
							
							//section:notification!
							//get the Student Name for the notification
							String[] stdNameSelect = {"p.FirstName","p.LastName"};
							String[] stdNameFrom = {"user u", "profile p"};
							String[] stdNameWhere = {"u.ProfileID=p.ProfileId", "u.username='" + requesterUsername + "'"};
							
							Query stdNameQuery = DatabaseController.getQuery(stdNameSelect, stdNameFrom, stdNameWhere, null);
							String[][] stdNameQuery2dDataArray = stdNameQuery.getDataInQuery();
							
							String studentFullName = stdNameQuery2dDataArray[1][1] + " " + stdNameQuery2dDataArray[1][2];
											
							
							//get the hire post info for the notification
							String[] notificationSelect = {"JobType","Title", "Offers"};
							String[] notificationFrom = {"Employer_HiringPost"};
							String[] notificationWhere = {"HirePostID='" + hirePostId + "'"};
							
							Query notificationQuery = DatabaseController.getQuery(notificationSelect, notificationFrom, notificationWhere, null);
							String[][] notificationQuery2dDataArray = notificationQuery.getDataInQuery();
							
							String jobTypeForNotification = notificationQuery2dDataArray[1][1];
							String titleForNotification = notificationQuery2dDataArray[1][2];
							String offersForNotification = notificationQuery2dDataArray[1][3];
							
							//Send notification to employer that request
										
							String empToken = FirebaseCloudMessageController.getUserToken(empUsername);
							String notificationTitle = studentFullName + " Has Apply For Your Hiring Post";
							String notificationDesc = "Message: " + leaveAMessage 
									+ "\n\nPost Infomation:"
									+ "\nJobType : " + jobTypeForNotification
									+ "\nPost Title : " + titleForNotification
									+ "\nPost Offers : " + offersForNotification;
							
							if(!empToken.equals("")) {
								//if token is found
								NotificationInfo myNotificationInfo = new NotificationInfo(empToken, notificationTitle, notificationDesc);
								try {
									FirebaseCloudMessageController.pushNotificationToUser(myNotificationInfo);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									System.out.println("Error Pushing Notification to user: " + empToken);
									e.printStackTrace();
								}
							}
							
							
							//if this request is success, We check whether the employer of this post is online
							//if online we Forward an update request to him							
							System.out.println("applyEmployerPostRequest -> employer Username is " + empUsername);
							if(ServerCore.checkClientOnlineOrNot(empUsername)) {
								//if the employer is online
								
								//get their ClientInfo Object
								ClientInfo targetedClientInfo = ServerCore.getClientInfoFromArrayList(empUsername);
								
								String[] empUpdateSelect = {"ehp.hirepostid","hpp.requestdateTime", "hpp.requesterUsername", "p.firstname", "p.lastname", "p.profilepic", "p.GeneralSkill_Category", "hpp.message", "hpp.status", "hpi.requestDateTime", "hpi.requesterUsername", "hpi.message", "hpi.date", "hpi.time", "hpi.location", "hpi.status", "hpp.Status_added_dateTime" };
								String[] empUpdateFrom = {"employer_hiringpost ehp", "Hiring_Post_Pending_Request hpp", "Hiring_Post_interview_Request hpi", "profile p", "user u"};
								String[] empUpdateCondition = {"ehp.hirepostid=hpp.hirepostid", "ehp.hirepostid='" + hirePostId + "'", "hpp.requesterUsername='" + requesterUsername + "'", "ehp.hirepostid=hpi.hirepostid", "hpp.requesterUsername=hpi.requesterUsername", "u.username=hpp.requesterUsername", "u.profileid=p.profileid", "u.username=hpi.requesterUsername"};
				
								Query empUpdateQuery = DatabaseController.getQuery(empUpdateSelect, empUpdateFrom, empUpdateCondition, null);//store result of Query
																
								empUpdateQuery.printQuery();
								
								Query[] updateEmpRelatedQueryContent = new Query[] {empUpdateQuery};
								
								if(targetedClientInfo != null) {
									System.out.println("Add new PostRequest into sent queue Failed");
									
									ComObject updateEmpComObj = new ComObject("ServerRequest", "updateEmployerNewPostRequest", updateEmpRelatedQueryContent);
									
									ClientHandler targetedClientHandler = targetedClientInfo.getClientHandler();
									targetedClientHandler.getClientSender().addObjectIntoSentQueue(updateEmpComObj);
								}
								
							}
							
						}else {
							System.out.println("applyEmployerPostRequest Failed");
							relatedStringContent = new String[] {"failed"};
						}
						
					}else {
						System.out.println("applyEmployerPostRequest Failed");
						relatedStringContent = new String[] {"failed"};
					}
					
				}else {
					relatedStringContent = new String[] {"UserAlreadyApply"};
				}
			
			}else {
				relatedStringContent = new String[] {"PostNotExist"};
			}
		}else {
			relatedStringContent = new String[] {"PostNotExist"};
		}
			
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent, relatedQueryContent);
				
		return replyObj;
		
		
	}
	
	private ComObject checkProfileRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("studentCheckProfileRequest function performing for Account Name: "+ receivedStringContent[0]);
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		Query[] relatedQueryContent = null;
		String[] relatedStringContent = null;	
		
		
		String accountUsername = receivedStringContent[0];//Client Send his username
		
		//check whether the check profile username is targeted a student or employer account
		
		String[] checkAccTypeSelect = {"Acc_type"};
		String[] checkAccTypeFrom = {"User"};
		String[] checkAccTypeWhere = {"Username='" + accountUsername + "'"};
		
		Query checkAccTypeQuery = DatabaseController.getQuery(checkAccTypeSelect, checkAccTypeFrom, checkAccTypeWhere, null);
		String[][] checkAccTypeQuery2dData = checkAccTypeQuery.getDataInQuery();
		
		String resultForAccType = checkAccTypeQuery2dData[1][1];
		
		
		
		
		if(resultForAccType.trim().toLowerCase().equals("student")) {
			//Getting User Account Info into a Query Object
			String[] accountInfoSelect = {"u.username", "u.acc_type", "u.profileid", "p.profilepic", "p.firstname", "p.lastname", "p.generalskill_category","p.aboutme", "p.collegename", "p.coursename"};
			String[] accountInfoFrom = {"user u", "profile p"};
			String[] accountInfoCondition = {"u.username = '"+ accountUsername +"'", "u.profileid=p.profileid"};// will be define later
			
			Query accountInfo = DatabaseController.getQuery(accountInfoSelect, accountInfoFrom, accountInfoCondition, null);
			
		    accountInfo.printQuery();
		    
		    //extract the data inside the User AccountInfo
		    String[][] accountInfoData =  accountInfo.getDataInQuery();
		    
		    String accountProfileId = accountInfoData[1][3];//get the user ProfileId from the Extracted data
		    
		    //Note: Getting ProfileId of User allow other user Related data such as(AccountContact Table, AccountPortfolio Table, AccountSoftwareSkill Table)
		    //ProfileId is the Composite Primarykey Of user Related data TAble
		    
		    //Getting User Account Contact into a Query Object		
		    String[] accountContactSelect = {"contacttype","contactinfo"};
		    String[] accountContactFrom = {"profile_contact"};
		    String[] accountContactWhere = {"profileid = '" + accountProfileId + "'"};
		    Query accountContact = DatabaseController.getQuery(accountContactSelect, accountContactFrom, accountContactWhere, null);
		    
		    //Getting User Account Portfolio into a Query Object
		    String[] accountPortfolioSelect = {"portfolio_id", "portfoliotype", "portfoliopicture", "description", "portfoliourl"};
		    String[] accountPortfolioFrom = {"Profile_Portfolio"};
		    String[] accountPortfolioWhere = {"profileid = '" + accountProfileId + "'"};
		    
		    Query accountPortfolio = DatabaseController.getQuery(accountPortfolioSelect, accountPortfolioFrom, accountPortfolioWhere, null);
		    
		    accountPortfolio.printQuery();
		    
		    //Getting User Account Software Skill info into a Query Object
		    String[] accountSoftwareSkillSelect = {"SoftwareSkillID", "softwarename","skilllevel"};
		    String[] accountSoftwareSkillFrom = {"profile_softwareskill"};
		    String[] accountSoftwareSkillWhere = {"profileid = '" + accountProfileId + "'"};
		    Query accountSoftwareSkill = DatabaseController.getQuery(accountSoftwareSkillSelect, accountSoftwareSkillFrom, accountSoftwareSkillWhere, null);
		    accountSoftwareSkill.printQuery();
		    
		    
		    //Combine all Client Require Data into a Query Array
		    relatedQueryContent = new Query[]{accountInfo, accountContact, accountPortfolio, accountSoftwareSkill};
		    relatedStringContent = new String[] {resultForAccType};
		    //create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
			ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent, relatedQueryContent);
			
			return replyObj;
		}else {
			//must be a employer account
			//Getting User Account Info into a Query Object
			String[] accountInfoSelect = {"u.username", "u.acc_type", "u.profileid", "p.profilepic", "p.firstname", "p.lastname", "p.generalskill_category","p.aboutme"};
			String[] accountInfoFrom = {"user u", "profile p"};
			String[] accountInfoCondition = {"u.username = '"+ accountUsername +"'", "u.profileid=p.profileid"};// will be define later
			
			Query accountInfo = DatabaseController.getQuery(accountInfoSelect, accountInfoFrom, accountInfoCondition, null);
			
		    accountInfo.printQuery();
		    
		    //extract the data inside the User AccountInfo
		    String[][] accountInfoData =  accountInfo.getDataInQuery();
		    
		    String accountProfileId = accountInfoData[1][3];//get the user ProfileId from the Extracted data
		    
		    //Note: Getting ProfileId of User allow other user Related data such as(AccountContact Table, AccountPortfolio Table, AccountSoftwareSkill Table)
		    //ProfileId is the Composite Primarykey Of user Related data TAble
		    
		    //Getting User Account Contact into a Query Object		
		    String[] accountContactSelect = {"contacttype","contactinfo"};
		    String[] accountContactFrom = {"profile_contact"};
		    String[] accountContactWhere = {"profileid = '" + accountProfileId + "'"};
		    Query accountContact = DatabaseController.getQuery(accountContactSelect, accountContactFrom, accountContactWhere, null);
		    
		    accountContact.printQuery();
		    //Combine all Client Require Data into a Query Array
		    relatedQueryContent = new Query[]{accountInfo, accountContact};
		    relatedStringContent = new String[] {resultForAccType};
		    
		    //create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
			ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent, relatedQueryContent);
			
			return replyObj;
			
			
			
			
		}
			
	
	}
	
	private ComObject createAccountRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("createAccountRequest function performing for Account Name: "+ receivedStringContent[0]);
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;	
		
		String accType = receivedStringContent[0].toLowerCase();
		
		
		String firstName = null;
        String lastName = null;
        String username = null;
        String password = null;
        String email = null;
        String contactNum = null;

        String generalSkillCat = null;
        String collegeName = null;
        String courseName = null;
        
        
		String profilePicBase64EncodedString = null;
        
		//get received Info
		if(accType.equals("student")) {
			
			
			 
			//receivedStringContent Array Content as follow -> accType,firstName,lastName,username,password,email ,contactNumber, generalSkillCat, collegeName,courseName
			 firstName = receivedStringContent[1];
             lastName = receivedStringContent[2];
             username = receivedStringContent[3].toLowerCase();
             password = receivedStringContent[4];
             email = receivedStringContent[5];
             contactNum = receivedStringContent[6];

             generalSkillCat = receivedStringContent[7];
             collegeName = receivedStringContent[8];
             courseName = receivedStringContent[9];
			
             BufferedImage defaultStudentProfilePic;
             try {
	             if(generalSkillCat.equals("Programmer")) {
	            	 defaultStudentProfilePic = ImageIO.read(getClass().getClassLoader().getResource("default_programmer_profilepic.png"));
	             }else {
	            	 defaultStudentProfilePic = ImageIO.read(getClass().getClassLoader().getResource("default_designer_profilepic.png"));
	            	 
	             }
	             profilePicBase64EncodedString = convertBufferedImageIntoImageString(defaultStudentProfilePic);
	             
             }catch(Exception e) {
            	 e.printStackTrace();
             }
             
             
		}else {
			//this is an create employer account request
			//receivedStringContent Array Content as follow ->accType,firstName,lastName,username,password,email ,contactNumber
			firstName = receivedStringContent[1];
            lastName = receivedStringContent[2];
            username = receivedStringContent[3].toLowerCase();
            password = receivedStringContent[4];
            email = receivedStringContent[5];
            contactNum = receivedStringContent[6];
            
            generalSkillCat = "Employer";
            collegeName = null;
            courseName = null;
            
            try {
            BufferedImage defaultEmployerProfilePic = ImageIO.read(getClass().getClassLoader().getResource("default_employer_profilepic.png"));
            profilePicBase64EncodedString = convertBufferedImageIntoImageString(defaultEmployerProfilePic);
		
            }catch(Exception e) {
            	e.printStackTrace();
            }
		}
		
		String[] checkUsernameSelect = {"*"};
		String[] checkUsernameFrom = {"User"};
		String[] checkUsernameWhere = {"username='" + username +"'" };
		
		Query checkUsernameQuery = DatabaseController.getQuery(checkUsernameSelect, checkUsernameFrom, checkUsernameWhere, null);
		
		if(checkUsernameQuery.EmptyOrNot()) {
			//if this is empty, means the username did not exist in the database
			//proceed to create
			
			//get current Date Time
			Date date = new Date();
					
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
					
			String currentDateTime = formatter.format(date);  
			
			//Section: Generate a profileID
			//in order to insert a new data a new profileid need to be generated
			//generate the new profileid for the user	
			String[] select = {"MAX(CAST(ProfileID  AS int)) AS newProfileId"};
			String[] from = {"Profile"};
			
			Query newProfileIdQuery = DatabaseController.getQuery(select, from, null, null);
			
			newProfileIdQuery.printQuery();
			
			String[][] newProfileIdQueryData = newProfileIdQuery.getDataInQuery();
			
			int largestProfileId = Integer.parseInt(newProfileIdQueryData[1][1]);
			
			//largest number of profileid of a all user + 1(So, This PortfolioId will be unique)
			int newProfileId = largestProfileId + 1;
					
			String newProfileIdString = Integer.toString(newProfileId);
			
			
			//Section: insert new account data into database
			
			String[] userTableValues = {username, password, currentDateTime, accType, newProfileIdString};
			
			if(DatabaseController.insertDataIntoTable("User", userTableValues)) {
				
				String[] profileTableValues = {newProfileIdString, profilePicBase64EncodedString, firstName, lastName, generalSkillCat, "No Description Yet", collegeName, courseName};
				
				if(DatabaseController.insertDataIntoTable("Profile", profileTableValues )) {
					
					String[] ProfileContactTablePhoneNumValues = {newProfileIdString, "phonenum", contactNum};
					if(DatabaseController.insertDataIntoTable("Profile_Contact", ProfileContactTablePhoneNumValues)) {
						String[] ProfileContactTableEmailValues = {newProfileIdString, "email", email};
						
						if(DatabaseController.insertDataIntoTable("Profile_Contact", ProfileContactTableEmailValues)){
							
							relatedStringContent = new String[] {"success"};
							
						}else {
							relatedStringContent = new String[] {"failed"};
							
							//remove the previous added record in user table, since this request is failed
							//remove the previous added record in user profile
							//remove the previous added record in profile_contact for phonenum
							String[] deleteUserWhere = {"username='" + username +"'"};
							DatabaseController.deleteDataFromTable("User", deleteUserWhere);
							
							String[] deleteProfileWhere = {"profileid='" + newProfileIdString +"'"};
							DatabaseController.deleteDataFromTable("Profile", deleteProfileWhere);
							
							String[] deletePhoneNumWhere = {"profileid='" + newProfileIdString +"'", "ContactType='phonenum'" };
							DatabaseController.deleteDataFromTable("Profile_Contact", deletePhoneNumWhere);
							
						}
					}else {
						relatedStringContent = new String[] {"failed"};
						
						//remove the previous added record in user table, since this request is failed
						String[] deleteUserWhere = {"username='" + username +"'"};
						DatabaseController.deleteDataFromTable("User", deleteUserWhere);
						
						//remove the previous added record in user profile
						String[] deleteProfileWhere = {"profileid='" + newProfileIdString +"'"};
						DatabaseController.deleteDataFromTable("Profile", deleteProfileWhere);
						
						
					}
					
					
					
				}else {
					
					relatedStringContent = new String[] {"failed"};
					
					//remove the previous added record in user table, since this request is failed
					String[] deleteUserWhere = {"username='" + username +"'"};
					DatabaseController.deleteDataFromTable("User", deleteUserWhere);
					
				}
				
			}else {
				relatedStringContent = new String[] {"failed"};
			}
			
			
			
			
		}else {
			//username Already Exist
			relatedStringContent = new String[] {"usernameAlreadyExist"};
		}
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent);
						
		return replyObj;
	}
	
	private ComObject getStudentMyChatInfoRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("getStudentMyChatInfoRequest function performing for Account Name: "+ receivedStringContent[0]);
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;	
		
		String username = receivedStringContent[0];
		
		//*** get all ChatRoom that involved our user ***
		String[] getChatRoomSelect = {"*"};
		String[] getChatRoomFrom = {"Chat_Room"};
		String[] getChatRoomWhere = {"user1='" + username + "' or user2='"+username+"'"};
		
		Query getChatRoomQuery = DatabaseController.getQuery(getChatRoomSelect, getChatRoomFrom, getChatRoomWhere, null);
		
		String[][] data = getChatRoomQuery.getDataInQuery();
		
		//------------------------find involved opponent list and chatroomid------------------------
		ArrayList<String> involvedUser = new ArrayList<String>();
		ArrayList<String> involvedChatRoom = new ArrayList<String>();
		
		//loop every row in the data
		for(int loop=1;loop < data.length;loop++ ) {
			String chatRoomId=data[loop][1];
			String user1Name=data[loop][2];	
			String user2Name=data[loop][3];
			
			involvedChatRoom.add(chatRoomId);
			
			//if user1Name not equal to myself
			if(user1Name != username) {
				involvedUser.add(user1Name);
			}
			
			//if user2Name not equal to myself
			if(user2Name != username) {
				involvedUser.add(user2Name);
			}			
		}
		
		//involvedUserInString will be like if there are 2 user then
		//involvedUserInString = "'jack','john'"
		String involvedUserInString="";
		for(int loop=0;loop<involvedUser.size();loop++) {
			
			//the first one no need , infront
			if(loop == 0) {
				involvedUserInString += "'"+involvedUser.get(loop)+"'";
			}else {
				involvedUserInString += ",'"+involvedUser.get(loop)+"'";
			}
		}	
		
		//involvedChatRoomInString will be like if there are 2 chatroomid then
		//involvedChatRoomInString = "1,2"
		String involvedChatRoomInString="";
		for(int loop=0;loop<involvedChatRoom.size();loop++) {
			
			//the first one no need , infront
			if(loop == 0) {
				involvedChatRoomInString += involvedChatRoom.get(loop);
			}else {
				involvedChatRoomInString += ","+involvedChatRoom.get(loop);
			}
		}	
		
		//----------------------------------------------------------------
		
		
		//*** get all user information of opponent***
		String[] getOpponentInfoSelect = {"u.username,p.profilepic,p.firstname,p.lastname,p.generalSkill_Category"};
		String[] getOpponentInfoFrom = {"user u, profile p"};
		String[] getOpponentInfoWhere = {"u.ProfileID=p.ProfileID and u.username in ("+involvedUserInString+")"};
		
		Query getOpponentInfo = DatabaseController.getQuery(getOpponentInfoSelect, getOpponentInfoFrom, getOpponentInfoWhere, null);
		
		getOpponentInfo.printQuery();
		
		//*** get all messages of chat room that involved our user ***
		String[] getAllMessagesSelect = {"*"};
		String[] getAllMessagesFrom = {"Chat_Room_Message"};
		String[] getAllMessagesWhere = {"ChatRoomId in ("+involvedChatRoomInString+")"};
		String[] getAllMessageAddition = {"order by chatroomid ASC, messagedatetime ASC"};
		Query getAllMessages = DatabaseController.getQuery(getAllMessagesSelect, getAllMessagesFrom, getAllMessagesWhere, getAllMessageAddition);
		getAllMessages.printQuery();
				
		Query[] relatedQueryContent = new Query[] {getChatRoomQuery,getOpponentInfo,getAllMessages};
		
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedQueryContent);
						
		return replyObj;
	}
	
	private ComObject getEmployerMyChatInfoRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("getEmployerMyChatInfoRequest function performing for Account Name: "+ receivedStringContent[0]);
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;	
		
		String username = receivedStringContent[0];
		
		//*** get all ChatRoom that involved our user ***
		String[] getChatRoomSelect = {"*"};
		String[] getChatRoomFrom = {"Chat_Room"};
		String[] getChatRoomWhere = {"user1='" + username + "' or user2='"+username+"'"};
		
		Query getChatRoomQuery = DatabaseController.getQuery(getChatRoomSelect, getChatRoomFrom, getChatRoomWhere, null);
		
		String[][] data = getChatRoomQuery.getDataInQuery();
		
		//------------------------find involved opponent list and chatroomid------------------------
		ArrayList<String> involvedUser = new ArrayList<String>();
		ArrayList<String> involvedChatRoom = new ArrayList<String>();
		
		//loop every row in the data
		for(int loop=1;loop < data.length;loop++ ) {
			String chatRoomId=data[loop][1];
			String user1Name=data[loop][2];	
			String user2Name=data[loop][3];
			
			involvedChatRoom.add(chatRoomId);
			
			//if user1Name not equal to myself
			if(user1Name != username) {
				involvedUser.add(user1Name);
			}
			
			//if user2Name not equal to myself
			if(user2Name != username) {
				involvedUser.add(user2Name);
			}			
		}
		
		//involvedUserInString will be like if there are 2 user then
		//involvedUserInString = "'jack','john'"
		String involvedUserInString="";
		for(int loop=0;loop<involvedUser.size();loop++) {
			
			//the first one no need , infront
			if(loop == 0) {
				involvedUserInString += "'"+involvedUser.get(loop)+"'";
			}else {
				involvedUserInString += ",'"+involvedUser.get(loop)+"'";
			}
		}	
		
		//involvedChatRoomInString will be like if there are 2 chatroomid then
		//involvedChatRoomInString = "1,2"
		String involvedChatRoomInString="";
		for(int loop=0;loop<involvedChatRoom.size();loop++) {
			
			//the first one no need , infront
			if(loop == 0) {
				involvedChatRoomInString += involvedChatRoom.get(loop);
			}else {
				involvedChatRoomInString += ","+involvedChatRoom.get(loop);
			}
		}	
		
		//----------------------------------------------------------------
		
		
		//*** get all user information of opponent***
		String[] getOpponentInfoSelect = {"u.username,p.profilepic,p.firstname,p.lastname,p.generalSkill_Category"};
		String[] getOpponentInfoFrom = {"user u, profile p"};
		String[] getOpponentInfoWhere = {"u.ProfileID=p.ProfileID and u.username in ("+involvedUserInString+")"};
		
		Query getOpponentInfo = DatabaseController.getQuery(getOpponentInfoSelect, getOpponentInfoFrom, getOpponentInfoWhere, null);
		
		getOpponentInfo.printQuery();
		
		//*** get all messages of chat room that involved our user ***
		String[] getAllMessagesSelect = {"*"};
		String[] getAllMessagesFrom = {"Chat_Room_Message"};
		String[] getAllMessagesWhere = {"ChatRoomId in ("+involvedChatRoomInString+")"};
		String[] getAllMessageAddition = {"order by chatroomid ASC, messagedatetime ASC"};
		Query getAllMessages = DatabaseController.getQuery(getAllMessagesSelect, getAllMessagesFrom, getAllMessagesWhere, getAllMessageAddition);
		getAllMessages.printQuery();
				
		Query[] relatedQueryContent = new Query[] {getChatRoomQuery,getOpponentInfo,getAllMessages};
		
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedQueryContent);
						
		return replyObj;
	}
	
	private ComObject searchChatRoomRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("searchChatRoomRequest function performing for Account Name: "+ receivedStringContent[0]);
		
		
		String keyword = receivedStringContent[0];		
		String username = receivedStringContent[1];	//requester username, so we exclude it from the search as user cannot chat with himself
		String loggedOnAccType = receivedStringContent[2];
		
		//*** get all user information of opponent***
		String[] getOpponentInfoSelect = {"u.username,p.profilepic,p.firstname,p.lastname,p.generalSkill_Category,concat(p.firstname, p.lastname) as fullname"};
		String[] getOpponentInfoFrom = {"user u, profile p"};
		String[] getOpponentInfoWhere = {"u.ProfileID=p.ProfileID and REPLACE(concat(p.firstname, p.lastname), ' ', '') LIKE REPLACE('"+ keyword +"%', ' ', '') and u.username!='"+username+"'"};
		
		Query getOpponentInfo = DatabaseController.getQuery(getOpponentInfoSelect, getOpponentInfoFrom, getOpponentInfoWhere, null);
		
		getOpponentInfo.printQuery();
		
		
		Query[] relatedQueryContent = new Query[] {getOpponentInfo};
		String[] relatedStringContent = new String[] {loggedOnAccType};
		
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent, relatedQueryContent);
						
		return replyObj;
	}
	
	
	private ComObject sentMessageChatRoomRequest(String receivedTitle, String[] receivedStringContent) {
		MainScreen.addLogText("Received Request: " + receivedTitle + " from client Username: ("+ clientUsername + ") from Socket:" + s);
		System.out.println("sentMessageChatRoomRequest function performing for Account Name: "+ receivedStringContent[0]);
		
		//create Variable to store all related Content Array for placing it to the Communication Object
		String[] relatedStringContent = null;	
		Query[] relatedQueryContent = null;	
		
		String message = receivedStringContent[0];		
		String from = receivedStringContent[1];	
		String to = receivedStringContent[2];	
		String chatRoomId = receivedStringContent[3];	
		String loggedOnAccType = receivedStringContent[4];	
		
		if(chatRoomId.equals("-1")) {
			//means chatroom doesnt exist yet
			
			//Section:generating a new chatroom id 
			String[] chatRoomIdSelect = {"MAX(CAST(messageID  AS int)) AS newChatRoomId"};
			String[] chatRoomIdFrom = {"Chat_Room_Message"};
			
			
			Query chatRoomIdQuery = DatabaseController.getQuery(chatRoomIdSelect, chatRoomIdFrom, null, null);
			
			chatRoomIdQuery.printQuery();
			
			String[][] newChatRoomIdQueryData = chatRoomIdQuery.getDataInQuery();
			
			int largestChatRoomId= Integer.parseInt(newChatRoomIdQueryData[1][1]);
			
			//new chatroom = largest number of Chatroom +1
			int newChatRoomId = largestChatRoomId + 1;
			
			String newChatRoomIdString = Integer.toString(newChatRoomId);
			
			//Section:creating a new chatroom into the database
			String[] chatRoomValues = {newChatRoomIdString, from, to};
			
			if(DatabaseController.insertDataIntoTable("Chat_Room", chatRoomValues)) {
				//if new chat room create succesful
				
				
				//get current Date Time
				Date date = new Date();
						
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
						
				String currentDateTime = formatter.format(date);  
				
				//since this is a new chatroom message id will oly start from 1
				
				//Section:inserting the message into the database
				String[] values = {newChatRoomIdString, "1", currentDateTime, from, to, message};
				
				if(DatabaseController.insertDataIntoTable("Chat_Room_Message", values)) {
					//successfully insert
					
					//*** get the specific ChatRoom that involved our user ***
					String[] getSenderChatRoomSelect = {"*"};
					String[] getSenderChatRoomFrom = {"Chat_Room"};
					String[] getSenderChatRoomWhere = {"chatRoomId='" + newChatRoomIdString + "'"};
					
					Query getSenderChatRoomQuery = DatabaseController.getQuery(getSenderChatRoomSelect, getSenderChatRoomFrom, getSenderChatRoomWhere, null);
					
					String[][] chatData = getSenderChatRoomQuery.getDataInQuery();
					
					//*** get the sender user information of opponent***
					String[] getSenderOpponentInfoSelect = {"u.username,p.profilepic,p.firstname,p.lastname,p.generalSkill_Category"};
					String[] getSenderOpponentInfoFrom = {"user u, profile p"};
					String[] getSenderOpponentInfoWhere = {"u.ProfileID=p.ProfileID and u.username in ('"+to+"')"};
					
					Query getSenderOpponentInfo = DatabaseController.getQuery(getSenderOpponentInfoSelect, getSenderOpponentInfoFrom, getSenderOpponentInfoWhere, null);
					
					getSenderOpponentInfo.printQuery();
					
					//*** get message from chat room that involved our user ***
					String[] getSenderAllMessagesSelect = {"*"};
					String[] getSenderAllMessagesFrom = {"Chat_Room_Message"};
					String[] getSenderAllMessagesWhere = {"ChatRoomId in ('"+newChatRoomIdString+"')"};
					String[] getSenderAllMessageAddition = {"order by chatroomid ASC, messagedatetime ASC"};
					Query getSenderAllMessages = DatabaseController.getQuery(getSenderAllMessagesSelect, getSenderAllMessagesFrom, getSenderAllMessagesWhere, getSenderAllMessageAddition);
					getSenderAllMessages.printQuery();
							
					
					relatedQueryContent = new Query[] {getSenderChatRoomQuery, getSenderOpponentInfo, getSenderAllMessages};
					relatedStringContent = new String[]{"success", loggedOnAccType, "newChatRoom" };		
					
					
					
					//section:notification!
					//get the from Name for the notification
					String[] fromNameSelect = {"p.FirstName","p.LastName"};
					String[] fromNameFrom = {"user u", "profile p"};
					String[] fromNameWhere = {"u.ProfileID=p.ProfileId", "u.username='" + from + "'"};
					
					Query fromNameQuery = DatabaseController.getQuery(fromNameSelect, fromNameFrom, fromNameWhere, null);
					String[][] fromNameQuery2dDataArray = fromNameQuery.getDataInQuery();
					
					String fromFirstName = fromNameQuery2dDataArray[1][1];
					String fromFullName = fromNameQuery2dDataArray[1][1] + " " + fromNameQuery2dDataArray[1][2];
									
					
					
					//Send notification to receiver that request
								
					String messageReceiverToken = FirebaseCloudMessageController.getUserToken(to);
					String notificationTitle = fromFullName + "Has Sent A Message";
					String notificationDesc = fromFirstName+ ": "+ message;
							
					
					if(!messageReceiverToken.equals("")) {
						//if token is found
						NotificationInfo myNotificationInfo = new NotificationInfo(messageReceiverToken, notificationTitle, notificationDesc);
						try {
							FirebaseCloudMessageController.pushNotificationToUser(myNotificationInfo);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							System.out.println("Error Pushing Notification to user: " + messageReceiverToken);
							e.printStackTrace();
						}
					}
					
					
					//if this request is success, We check whether the receiver of the message online
					//if online we Forward an message request to him							
					System.out.println("SendchatMessageRequest -> receiver Username is " + to);
					if(ServerCore.checkClientOnlineOrNot(to)) {
						//if the receiver is online
						
						//get their ClientInfo Object
						ClientInfo targetedClientInfo = ServerCore.getClientInfoFromArrayList(to);
						
						//*** get the specific ChatRoom that involved our user ***
						String[] getChatRoomSelect = {"*"};
						String[] getChatRoomFrom = {"Chat_Room"};
						String[] getChatRoomWhere = {"chatRoomId='" + newChatRoomIdString + "'"};
						
						Query getChatRoomQuery = DatabaseController.getQuery(getChatRoomSelect, getChatRoomFrom, getChatRoomWhere, null);
						
						String[][] data = getChatRoomQuery.getDataInQuery();
						
						//*** get the sender user information of opponent***
						String[] getOpponentInfoSelect = {"u.username,p.profilepic,p.firstname,p.lastname,p.generalSkill_Category"};
						String[] getOpponentInfoFrom = {"user u, profile p"};
						String[] getOpponentInfoWhere = {"u.ProfileID=p.ProfileID and u.username in ('"+from+"')"};
						
						Query getOpponentInfo = DatabaseController.getQuery(getOpponentInfoSelect, getOpponentInfoFrom, getOpponentInfoWhere, null);
						
						getOpponentInfo.printQuery();
						
						//*** get message from chat room that involved our user ***
						String[] getAllMessagesSelect = {"*"};
						String[] getAllMessagesFrom = {"Chat_Room_Message"};
						String[] getAllMessagesWhere = {"ChatRoomId in ('"+newChatRoomIdString+"')"};
						String[] getAllMessageAddition = {"order by chatroomid ASC, messagedatetime ASC"};
						Query getAllMessages = DatabaseController.getQuery(getAllMessagesSelect, getAllMessagesFrom, getAllMessagesWhere, getAllMessageAddition);
						getAllMessages.printQuery();
								
						
						String[] updateReceiverRelatedStringContent = new String[]{"newChatRoom"};
						Query[] updateReceiverRelatedQueryContent = new Query[] {getChatRoomQuery,getOpponentInfo,getAllMessages};
						
						
						if(targetedClientInfo != null) {					
							
							ComObject updateReceiverComObj = new ComObject("ServerRequest", "updateReceiverMessageRequest", updateReceiverRelatedStringContent, updateReceiverRelatedQueryContent);
							
							ClientHandler targetedClientHandler = targetedClientInfo.getClientHandler();
							targetedClientHandler.getClientSender().addObjectIntoSentQueue(updateReceiverComObj);
						}
						
					}
					//*************************************************************************
					
				}else {
					relatedStringContent = new String[]{"failed",loggedOnAccType};
				}
				
			}else {
				relatedStringContent = new String[]{"failed",loggedOnAccType};
			}
			
			
			
			
		}else {
			//means chatroom has already exist
			
			//Section:generating a new message id for the particular chatroom
			String[] messageIdSelect = {"MAX(CAST(messageID  AS int)) AS newMessageId"};
			String[] messageIdFrom = {"Chat_Room_Message"};
			String[] messageIdWhere = {"ChatRoomId='"+chatRoomId+"'"};
			
			
			Query newMessageIdQuery = DatabaseController.getQuery(messageIdSelect, messageIdFrom, messageIdWhere, null);
			
			newMessageIdQuery.printQuery();
			
			String[][] newMessageIdQueryData = newMessageIdQuery.getDataInQuery();
			
			int largestMessageId= Integer.parseInt(newMessageIdQueryData[1][1]);
			
			//largest number of messageid of the particular chatroom
			int newMessageId = largestMessageId + 1;
					
			String newMessageIdString = Integer.toString(newMessageId);
			
			
			//get current Date Time
			Date date = new Date();
					
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
					
			String currentDateTime = formatter.format(date);  
			
			//Section:inserting the message into the database
			String[] values = {chatRoomId, newMessageIdString, currentDateTime, from, to, message};
			
			if(DatabaseController.insertDataIntoTable("Chat_Room_Message", values)) {
				//successfully insert
				//*** get all messages of chat room that involved our user ***
				String[] getSuccessMessagesSelect = {"*"};
				String[] getSuccessMessagesFrom = {"Chat_Room_Message"};
				String[] getSuccessMessagesWhere = {"ChatRoomId='"+ chatRoomId + "'", "messageId='"+newMessageIdString+"'"};
				String[] getSuccessMessageAddition = {"order by chatroomid ASC, messagedatetime ASC"};
				Query getAllMessages = DatabaseController.getQuery(getSuccessMessagesSelect, getSuccessMessagesFrom, getSuccessMessagesWhere, getSuccessMessageAddition);
				getAllMessages.printQuery();
				
				relatedQueryContent = new Query[] {getAllMessages};
				relatedStringContent = new String[]{"success", loggedOnAccType, "existingChatRoom" };		
				
				
				//section:notification!
				//get the from Name for the notification
				String[] fromNameSelect = {"p.FirstName","p.LastName"};
				String[] fromNameFrom = {"user u", "profile p"};
				String[] fromNameWhere = {"u.ProfileID=p.ProfileId", "u.username='" + from + "'"};
				
				Query fromNameQuery = DatabaseController.getQuery(fromNameSelect, fromNameFrom, fromNameWhere, null);
				String[][] fromNameQuery2dDataArray = fromNameQuery.getDataInQuery();
				
				String fromFirstName = fromNameQuery2dDataArray[1][1];
				String fromFullName = fromNameQuery2dDataArray[1][1] + " " + fromNameQuery2dDataArray[1][2];
								
				
				
				//Send notification to receiver that request
							
				String messageReceiverToken = FirebaseCloudMessageController.getUserToken(to);
				String notificationTitle = fromFullName + "Has Sent A Message";
				String notificationDesc = fromFirstName+ ": "+ message;
						
				
				if(!messageReceiverToken.equals("")) {
					//if token is found
					NotificationInfo myNotificationInfo = new NotificationInfo(messageReceiverToken, notificationTitle, notificationDesc);
					try {
						FirebaseCloudMessageController.pushNotificationToUser(myNotificationInfo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("Error Pushing Notification to user: " + messageReceiverToken);
						e.printStackTrace();
					}
				}
				
				
				//if this request is success, We check whether the receiver of the message online
				//if online we Forward an message request to him							
				System.out.println("SendchatMessageRequest -> receiver Username is " + to);
				if(ServerCore.checkClientOnlineOrNot(to)) {
					//if the receiver is online
					
					//get their ClientInfo Object
					ClientInfo targetedClientInfo = ServerCore.getClientInfoFromArrayList(to);
					
					//*** get all messages of chat room that involved our user ***
					String[] getReceiverMessagesSelect = {"*"};
					String[] getReceiverMessagesFrom = {"Chat_Room_Message"};
					String[] getReceiverMessagesWhere = {"ChatRoomId='"+ chatRoomId + "'", "messageId='"+newMessageIdString+"'"};
					String[] getReceiverMessageAddition = {"order by chatroomid ASC, messagedatetime ASC"};
					Query getReceiverMessages = DatabaseController.getQuery(getReceiverMessagesSelect, getReceiverMessagesFrom, getReceiverMessagesWhere, getReceiverMessageAddition);
					
					String[] updateReceiverRelatedStringContent = new String[] {"existingChatRoom"};
					Query[] updateReceiverRelatedQueryContent = new Query[] {getReceiverMessages};
					
					if(targetedClientInfo != null) {					
						
						ComObject updateReceiverComObj = new ComObject("ServerRequest", "updateReceiverMessageRequest", updateReceiverRelatedStringContent, updateReceiverRelatedQueryContent);
						
						ClientHandler targetedClientHandler = targetedClientInfo.getClientHandler();
						targetedClientHandler.getClientSender().addObjectIntoSentQueue(updateReceiverComObj);
					}
					
				}
				
			}else {
				relatedStringContent = new String[]{"failed",loggedOnAccType};
			}
			
		}
		
		
		
		//create a new Communication Object that include the relatedQueryContent(Client Require Data Array)
		ComObject replyObj = new ComObject("ServerReply", receivedTitle, relatedStringContent, relatedQueryContent );
						
		return replyObj;
	}
	
	
	
	private String convertBufferedImageIntoImageString(BufferedImage myBufferedImage) {
		//This function is to convert Buffered image into image string
		
		ByteArrayOutputStream myByteArrayOutputStream = new ByteArrayOutputStream();
		
		//convert Buffered image into into byte array and store in the stream
    	try {
			ImageIO.write(myBufferedImage, "png", myByteArrayOutputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//get the byte array from the stream
    	byte[] myImageByteArray = myByteArrayOutputStream.toByteArray();
    	
    	//convert byte array into Base 64 Encoded String 
    	String myImageString = Base64.getEncoder().encodeToString(myImageByteArray);
    	
    	System.out.println("Converted String" + myImageString);
    	
    	return myImageString;
		
	}
	
	private BufferedImage convertImageStringIntoBufferedImage(String myImageString) {
		//This function is to convert image string into Buffered image
		
		//get the byte[] 
		byte[] myImageByte = Base64.getDecoder().decode(myImageString);//decode image string into byte array
		
		ByteArrayInputStream myByteArrayInputStream = new ByteArrayInputStream(myImageByte);//store the byte into ByteArrayInputStream
		BufferedImage myBufferedImage = null;
		try {
			myBufferedImage = ImageIO.read(myByteArrayInputStream);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("problem occure when reading byte into BufferedImage");
			e.printStackTrace();
		}
		
		return myBufferedImage;
		
	}
	
	
	
	
	
	
	
	
}
