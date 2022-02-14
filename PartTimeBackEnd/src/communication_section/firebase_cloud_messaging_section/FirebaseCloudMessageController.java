package communication_section.firebase_cloud_messaging_section;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import database.DatabaseController;
import database.variable.Query;

public class FirebaseCloudMessageController {
	
	// Firebase server authentication
    public final static String FCM_SERVER_KEY = "AAAA6sGJ0a0:APA91bG7SKO5GqGfdJC5x85cLMgEf5R0i4m5065DsAsI6yaCw1Ni303op5_NHVRydoC3RO_3iMVy-MmBy3Z2n4qe-o5ym8M9LvMuWMJrWC0iLJDd4lZhBkuZU5NRbk71SawFCVVpGqWZ";
    public final static String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";
	
	public static void updateUserToken(String username, String token) {
		String[] select = {"*"};
		String[] from = {"Users_NotificationToken"};
		String[] where = {"username='" + username +"'"};
		
		Query checkTokenExistOrNotQuery = DatabaseController.getQuery(select, from, where, null);
		
		if(!checkTokenExistOrNotQuery.EmptyOrNot()) {
			//if not empty means token exist already 
			
			//we will need to replace the existing token with the new token
			String[] set = new String[] {"token='" + token + "'" };
			String[] condition = new String[] {"username='" + username + "'"};
			
			if(DatabaseController.updateDataFromTable("Users_NotificationToken", set, condition)) {
				System.out.println("updateUserToken success");
			}
		}else {
			//if no existing token
			//we will need to create one for the user
			String[] values = new String[] {username, token };
			
			if(DatabaseController.insertDataIntoTable("Users_NotificationToken", values)) {
				System.out.println("new UserToken create success");
			}
			
		}
		
	}



	public static void deleteUserToken(String username) {
		String[] select = {"*"};
		String[] from = {"Users_NotificationToken"};
		String[] where = {"username='" + username +"'"};
		
		Query checkTokenExistOrNotQuery = DatabaseController.getQuery(select, from, where, null);
		
		if(!checkTokenExistOrNotQuery.EmptyOrNot()) {
			//if exist
			
			//we will need to delete the token
	
			String[] condition = new String[] {"username='" + username + "'"};
			
			if(DatabaseController.deleteDataFromTable("Users_NotificationToken", condition)) {
				System.out.println("delete user token success");
			}
		}else {
			System.out.println("token doesnt exist success");
			
		}
		
	}
	
	public static String getUserToken(String username) {
		String resultToken = "";
		
		String[] select = {"token"};
		String[] from = {"Users_NotificationToken"};
		String[] where = {"username='" + username +"'"};
		
		Query checkTokenExistOrNotQuery = DatabaseController.getQuery(select, from, where, null);
		
		if(!checkTokenExistOrNotQuery.EmptyOrNot()) {
			//if exist
			
			//we will need to delete the token
			String[][] checkTokenExistOrNotQueryData = checkTokenExistOrNotQuery.getDataInQuery();
			
			resultToken = checkTokenExistOrNotQueryData[1][1];
			
		}else {
			System.out.println("token NOt found");
			
		}
		
		return resultToken;
		
	}
	
	

    public static void pushNotificationToUser(NotificationInfo targetNotificationInfo) throws Exception {

        String serverAuthKey = FCM_SERVER_KEY; // You FCM AUTH key
        String FcmApiUrl = FCM_API_URL;

        //create a http request to Firebase cloud messaging server
        URL url = new URL(FcmApiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);

        //using post method
        conn.setRequestMethod("POST");
        //provide out firebase cloud messaging Server Key to Google FCM server
        conn.setRequestProperty("Authorization", "key=" + serverAuthKey);
        //sent content using json format
        conn.setRequestProperty("Content-Type", "application/json");

        
        JSONObject data = new JSONObject();
        data.put("to", targetNotificationInfo.getFirebaseToken().trim());
        JSONObject info = new JSONObject();
        info.put("title", targetNotificationInfo.getTitle()); // Notification title
        info.put("content", targetNotificationInfo.getDescription()); // Notification body
        data.put("data", info);

        //write the htttp request to the Google FCM server
        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
        osw.write(data.toString());
        osw.flush();
        osw.close();

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

    }

}
