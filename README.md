# FYP_StudentJob_AndroidApp(OwYeong & Lim)
------------------------------------------------------------------------------------------------
FYP- StudentJob- Port In Instruction
------------------------------------------------------------------------------------------------
Slight Description:
In Order For this project to worked The Mobile Device(Android App) should 
connected to the same Local Area Network with the Server Application(Eclipse).
This is because Android App uses the local ip address to located the server location.
When this project is gone to an real implementation, the Local ip address will be replace with the Global Ip address / Domain name

---------------------------------------------------------------------------------------
Note: Please Make You install the lastest Xampp Version v3.2.2
There are 3 application which need to be run in order for this program to run
1) Xampp(MySql) -> xamp Installer is included in this project
2) Eclispe java ee
3) Android Studio

Before You Start The Start Step 1,
Please Extract all rar File in the SourceCode folder using WinRar


Step 1: Importing Database Into The Project
Note: Before Doing These Step, Please Make Sure You Do Not Have A Sql Database Named (FYP_StudentJob).
      if Yes, Please remove the database by using the commands(DROP DATABASE FYP_StudentJob;)
	

  i  ) Open The Xampp Control Panel, And Start The MySQL Server.  
         
  ii) Double Click on the fypStudentJobDatabase.txt(Recommended Open Using Notepad)  
    
  iii ) Copy all the content in the notepad.(Shortcut: Select all(Ctrl+A) Then Copy(Ctrl+C))  
    
  iv  ) double click on the mysql.bat ,This will open a commandPrompt.  
    
  v ) Finally Paste the content into it.(Shortcut: Paste(Ctrl + V)).  
    
      


Step 2: Importing the Server Application  
Note: The server Application is mainly used to connect client together and handle client request  
  i  ) Open Eclipse Java EE  
    
  ii ) On the Eclipse Navigation Bar.  
       Navigate:  
       File -> Import -> General -> Existing Projects Into WorkSpace  
       Click Next, a pop-up window will Shown  
         
  iii) On the root directory, Click Browse and Select the file where This Project is located\
       ../SourceCode/FYP_StudentJob_Server_Eclipse/PartTimeBackEnd  
       Click Select Folder.  
         
  iv ) The StudentJob Server Application is successfully imported into the Project   
    
  v  ) Now in eclipse Project Side Navigation Bar, open the File:   
       PartTimeBackEnd -> src -> gui -> LoginScreen   
        
  vi ) Click On Run Button(green in color),(Shortcut: Ctrl+ F11)  
  vii) Application show be run now, a Login screen will prompt for the account  
  
  
     ----------------------------------------------------------------------
       Server Application Control Panel Account
       Username: admin
       Password: admin
     ----------------------------------------------------------------------
     
     
  viii) Login to the following account, The Admin Control Panel will show.  
    
  ix) Click On The Button, Connect To Database.  
      Note: If a Dialop Pop up, please check whether you have successfully done Step 1
      The Button Should Appear Green If the Connection To MySql Database Is Successful
        
  x)  Click On Start Server,   
      The Button Should Appear Green If the server Is Successful  
  xi) Now, the server should now accepting incoming client connection and request action could be track on the Server Log.   
    
       
Step 3: Importing the Android Studio Application      
  Note: This Project is created in the lastest version of Android Studio, To Prevent any unexpected Error, Please install the Lastest  
	Android Studio Version  
  
  i  ) Open Android studio and on the top navigation bar:  
       File -> new -> Import Project  
  ii ) On the root directory, Click Browse and Select the file where This Project is located  
       ../SourceCode/FYP_StudentJob_AndroidStudio/freelance_parttime_app  
       Click Ok, The Project Should now imported   
  iii) On the side Project navigation bar, Change the View structure into Project View  
       Navigate to the Following File and open:  
       freelance_parttime_app -> app -> src -> main -> java -> communication_section -> ServerLocation.java  
                  
       you should see a line similar with the following
       
       
       --------------------------------------------------------------------------------------------
       private static InetSocketAddress serverLoc = new InetSocketAddress("192.168.1.188" , 2888);
       --------------------------------------------------------------------------------------------
       
       
       Note: To Find Out Your Ip address:
       Open Command Prompt, Type "ipconfig", Your Ip Address should appear in the IPv4 Address
  
  iv ) Change the following Ip Address(192.168.1.188) into your own Ip Address.  
       This indicate the android app where the server is locate.  

  iv ) You Are All set Enjoy.  
 

	Default Account For Testing Application(Android App):
	----------------------------------------
	Account For Student
	username:student
	password:student
	-----------------------------------------
	Account For Employer
	username:employer
	password:employer
