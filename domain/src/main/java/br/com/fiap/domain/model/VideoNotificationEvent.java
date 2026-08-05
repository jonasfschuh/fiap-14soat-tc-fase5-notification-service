package br.com.fiap.domain.model;

public class VideoNotificationEvent {

    private String videoId;
    private String userId;
    private String userEmail;
    private String originalFilename;
    private String eventType;
    private String errorMessage;
    private String timestamp;

    public VideoNotificationEvent() {
    }

    public VideoNotificationEvent(String videoId, String userId, String userEmail, String originalFilename,
                                  String eventType, String errorMessage, String timestamp) {
        this.videoId = videoId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.originalFilename = originalFilename;
        this.eventType = eventType;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
