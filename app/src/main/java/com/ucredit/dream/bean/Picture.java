package com.ucredit.dream.bean;


public class Picture {

    public static final String UPLOADED="uploaded";
    public static final String UNUPLOADED="unupload";
    
    private String userId;
    private String customId;
    private long time;
    private String status;
    private String pictureURL;
    private String attachType;
    private String id;//附件id
    private String uploadedTime;
    private String remoteUrl;
    
    
    public Picture() {
        super();
    }

    public Picture(String userId, String customId,long time, String status, String pictureURL) {
        super();
        this.userId = userId;
        this.customId = customId;
        this.time = time;
        this.status = status;
        this.pictureURL = pictureURL;
    }
    
    public String getUploadedTime() {
        return uploadedTime;
    }

    public void setUploadedTime(String uploadedTime) {
        this.uploadedTime = uploadedTime;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }


    public String getAttachType() {
        return attachType;
    }

    public void setAttachType(String attachType) {
        this.attachType = attachType;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getPictureURL() {
        return pictureURL;
    }
    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o==null) {
            return false;
        } else {
            if (o instanceof Picture) {
                Picture picture = (Picture) o;
                if (picture.userId==null||picture.customId==null) {
                    return false;
                }
                if (picture.time == this.time
                    && picture.userId.equals(this.userId)&&picture.customId.equals(this.customId) ) {
                    return true;
                }
            }
        }
        return false;
    }    
    
}
