package com.boilerplate.firestore.virtualstudy;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class NotificationID {

    @Exclude
    String notificationId;

    public<T extends  NotificationID> T withId(@NonNull final String id) {
        this.notificationId = id;
        return (T) this;
    }

}
