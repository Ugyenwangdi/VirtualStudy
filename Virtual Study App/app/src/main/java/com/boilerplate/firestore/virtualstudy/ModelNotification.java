package com.boilerplate.firestore.virtualstudy;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ModelNotification extends NotificationID {


    //public @ServerTimestamp

    String hisName, hisUid, myUid, notification,timestamp, hisImage, blog_post_id;



    public ModelNotification() { }

    public ModelNotification(String hisName, String hisUid, String myUid, String notification, String timestamp, String hisImage, String blog_post_id) {
        this.hisName = hisName;
        this.hisUid = hisUid;
        this.myUid = myUid;
        this.notification = notification;
        this.timestamp = timestamp;
        this.hisImage = hisImage;
        this.blog_post_id = blog_post_id;

    }

    public String getHisName() {
        return hisName;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getHisUid() {
        return hisUid;
    }
    public String getMyUid() {
        return myUid;
    }
    public String getNotification() {
        return notification;
    }
    public String getHisImage(){
        return hisImage;
    }
    public String getBlog_post_id(){
        return blog_post_id;
    }


    public void setHisName(String hisName) {
        this.hisName = hisName;
    }
    public void setTimestamp(String timestamp){
        this.timestamp = timestamp;
    }

    public void setHisUid(String hisUid) {
        this.hisUid = hisUid;
    }
    public void setNotification(String notification) {
        this.notification = notification;
    }
    public void setMyUid(String myUid) {
        this.myUid = myUid;
    }
    public void setHisImage(String hisImage){
        this.hisImage = hisImage;
    }
    public void setBlog_post_id(String blog_post_id){
        this.blog_post_id = blog_post_id;
    }

}
