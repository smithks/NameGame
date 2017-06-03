package com.willowtreeapps.namegame.network.api.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object to map with JSON containing social information.
 * @author Keegan Smith
 */

public class SocialLink implements Parcelable{

    private String type;
    private String callToAction;
    private String url;

    public SocialLink(String type, String callToAction, String url){
        this.type = type;
        this.callToAction = callToAction;
        this.url = url;
    }

    private SocialLink(Parcel in) {
        type = in.readString();
        callToAction = in.readString();
        url = in.readString();
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCallToAction() {
        return callToAction;
    }

    public void setCallToAction(String callToAction) {
        this.callToAction = callToAction;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(type);
        parcel.writeString(callToAction);
        parcel.writeString(url);
    }

    public static final Creator<SocialLink> CREATOR = new Creator<SocialLink>() {
        @Override
        public SocialLink createFromParcel(Parcel in) {
            return new SocialLink(in);
        }

        @Override
        public SocialLink[] newArray(int size) {
            return new SocialLink[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


}
