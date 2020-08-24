package com.nurulquran.audio.object;

/**
 * Created by pham on 05/05/2016.
 */
public class Banner {
    private String url;
    private String image;
    private String mType;

    public Banner(String mType,String url, String image) {
        this.url = url;
        this.image = image;
        this.mType =mType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

}
