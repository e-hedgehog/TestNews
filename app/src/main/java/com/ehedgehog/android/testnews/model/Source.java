package com.ehedgehog.android.testnews.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import io.realm.RealmObject;

public class Source extends RealmObject implements Serializable {

    @SerializedName("id")
    private String mId;
    @SerializedName("name")
    private String mName;

    public Source(String id, String name) {
        mId = id;
        mName = name;
    }

    public Source() {
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
