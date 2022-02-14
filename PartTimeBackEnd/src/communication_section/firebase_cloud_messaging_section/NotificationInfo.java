package communication_section.firebase_cloud_messaging_section;

public class NotificationInfo {
	private String firebaseToken;
	private String title;
	private String description;
	
	public NotificationInfo(String firebaseToken, String title, String description) {
		this.firebaseToken = firebaseToken;
		this.title = title;
		this.description = description;
	}
	
	public String getFirebaseToken() {
		return firebaseToken;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	

}
