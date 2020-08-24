package com.nurulquran.audio.object;

/**
 * Created by pham on 10/05/2016.
 */
public class Radio {
    private String mLinkLiveStream;
    private String mUrlRadio;
    private String mType;

    public Radio(String mLinkLiveStream) {
        this.mLinkLiveStream = mLinkLiveStream;
    }

    public String getmLinkLiveStream() {
        return mLinkLiveStream;
    }

    public void setmLinkLiveStream(String mLinkLiveStream) {
        this.mLinkLiveStream = mLinkLiveStream;
    }

    public String getmUrlRadio() {
        return mUrlRadio;
    }

    public void setmUrlRadio(String mUrlRadio) {
        this.mUrlRadio = mUrlRadio;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }
}
