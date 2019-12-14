package com.yangdiansheng.ipc.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {

    private String content;
    private boolean isSendSuccess;

    public Message(String content, boolean isSendSuccess) {
        this.content = content;
        this.isSendSuccess = isSendSuccess;
    }


    protected Message(Parcel in) {
        content = in.readString();
        isSendSuccess = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeByte((byte) (isSendSuccess ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
