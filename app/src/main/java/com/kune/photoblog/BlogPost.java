package com.kune.photoblog;

import java.util.Date;

public class BlogPost {

    private String user_id, image_url, description, thumb_image_url;
    private Date timestamp;
    //private Timestamp timestamp;

    public BlogPost() {

    } // Need to have an Empty Constructor as well

    public BlogPost(String user_id, String image_url, String description, String thumb_image_url, Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.description = description;
        this.thumb_image_url = thumb_image_url;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumb_image_url() {
        return thumb_image_url;
    }

    public void setThumb_image_url(String thumb_image_url) {
        this.thumb_image_url = thumb_image_url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
