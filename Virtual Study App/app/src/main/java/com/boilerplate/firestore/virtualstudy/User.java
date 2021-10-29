package com.boilerplate.firestore.virtualstudy;

public class User {

    private String name;
    private String image;
    private String username;

    public User() {



    }

    public User(String name, String image, String username) {
        this.name = name;
        this.image = image;
        this.username = username;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



}
