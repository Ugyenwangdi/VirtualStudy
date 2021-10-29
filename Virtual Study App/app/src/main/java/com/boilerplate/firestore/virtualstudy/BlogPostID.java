package com.boilerplate.firestore.virtualstudy;


import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class BlogPostID {

    @Exclude
    String blogPostId;

    public<T extends  BlogPostID> T withId(@NonNull final String id) {
        this.blogPostId = id;
        return (T) this;
    }
}
