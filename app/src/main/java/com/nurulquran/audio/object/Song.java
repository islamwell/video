package com.nurulquran.audio.object;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.nurulquran.audio.config.WebserviceConfig;
import com.nurulquran.audio.util.Logger;
import com.nurulquran.audio.util.StringUtil;

public class Song implements Parcelable{
    private String id;
    private String idType;
    private String name;
    private String url;
    private String image;
    private String artist;
    private String shareLink;
    private int listenCount = 0, downloadCount = 0;
    private int position;
    private boolean isSelected;
    private String description;
    public static final int PATH_FILE_DOWNLOAD = 123123123;
    private int mTypePathFile;

    public Song() {
    }

    public Song(String name, String artist, String url) {
        this.name = name;
        this.artist = artist;
        this.url = url;
        image = "";
    }

    public Song(JSONObject object) {
        Logger.e(object);
        try {
            id = object.getString("id");
            name = object.getString("name");
            url = object.getString("url");
            image = object.getString("image");
            description = object.getString("description");
            position = object.getInt("position");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Song(Parcel in) {
        id = in.readString();
        idType = in.readString();
        name = in.readString();
        url = in.readString();
        image = in.readString();
        artist = in.readString();
        shareLink = in.readString();
        listenCount = in.readInt();
        downloadCount = in.readInt();
        position = in.readInt();
        isSelected = in.readByte() != 0;
        description = in.readString();
        mTypePathFile = in.readInt();
    }


    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShareLink() {
        return shareLink;
    }

    public void setShareLink(String shareLink) {
        this.shareLink = shareLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (mTypePathFile == PATH_FILE_DOWNLOAD) {
            this.url = url;
        } else {
            if (StringUtil.checkUrl(url)) {
                this.url = url;
            } else {
                this.url = WebserviceConfig.URL_SONG + url;
            }
        }

    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        if (StringUtil.checkUrl(image)) {
            this.image = image;
        } else {
            this.image = WebserviceConfig.URL_IMAGE + image;
        }
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getListenCount() {
        return listenCount;
    }

    public void setListenCount(int listenCount) {
        this.listenCount = listenCount;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public void addMoreDownload() {
        this.downloadCount++;
    }

    public void addNewView() {
        this.listenCount++;
    }

    public int getmTypePathFile() {
        return mTypePathFile;
    }

    public void setmTypePathFile(int mTypePathFile) {
        this.mTypePathFile = mTypePathFile;
    }

    public boolean compare(Song otherSong) {
        return otherSong.getId().equals(id) && otherSong.getName().equals(name)
                && otherSong.getArtist().equals(artist);
    }

    @SuppressLint("DefaultLocale")
    public boolean checkNameAndArtist(String keyword) {
        String key = StringUtil.unAccent(keyword.toLowerCase());
        return StringUtil.unAccent(name.toLowerCase()).contains(key)
                || StringUtil.unAccent(artist.toLowerCase()).contains(key);
    }

    public boolean checkMusicType(String idMusicType) {
        try {
            return idMusicType.equals(idType);
        } catch (Exception e) {
            return false;
        }
    }

    public JSONObject getJsonObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("id_type", idType);
            object.put("name", name);
            object.put("url", url);
            object.put("image", image);
            object.put("description", description);
            object.put("position", position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(idType);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(image);
        dest.writeString(artist);
        dest.writeString(shareLink);
        dest.writeInt(listenCount);
        dest.writeInt(downloadCount);
        dest.writeInt(position);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeString(description);
        dest.writeInt(mTypePathFile);
    }
}
