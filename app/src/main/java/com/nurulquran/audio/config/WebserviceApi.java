package com.nurulquran.audio.config;

import android.content.Context;

import com.nurulquran.audio.R;

public class WebserviceApi {
    public static String MUSIC_DOMAIN = "index.php/api/";

    public static String getAlbumApi(Context context) {
        return context.getString(R.string.server_domain) + MUSIC_DOMAIN + "album";
    }

    public static String getCategoriesApi(Context context) {
        return context.getString(R.string.server_domain) + MUSIC_DOMAIN + "category";
    }
    public static String getSubCategoryApi(Context context) {
        return context.getString(R.string.server_domain)+MUSIC_DOMAIN+"getSubs";
    }

    public static String getSongs(Context context) {
        return context.getString(R.string.server_domain) + MUSIC_DOMAIN + "songView";
    }

    public static String getSongByCategory(Context context) {
        return context.getString(R.string.server_domain) + MUSIC_DOMAIN + "songCategory";
    }

    public static String getSongByAlbum(Context context) {
        return context.getString(R.string.server_domain) + MUSIC_DOMAIN + "songAlbum";
    }

    public static String getSearchSong(Context context) {
        return context.getString(R.string.server_domain) + MUSIC_DOMAIN + "nameSong";
    }

    public static String getTopSong(Context context) {
        return context.getString(R.string.server_domain) + MUSIC_DOMAIN + "topSong";
    }

    public static String getAddNewView(Context context) {
        return context.getString(R.string.server_domain) + MUSIC_DOMAIN + "listenSong";
    }

    public static String getAddNewDownload(Context context) {
        return context.getString(R.string.server_domain) + MUSIC_DOMAIN + "downloadSong";
    }
public static String getBannerApi(Context context) {
    return context.getString(R.string.server_domain)+MUSIC_DOMAIN+"banner";
}
    public static String getRadioApi(Context context) {
        return context.getString(R.string.server_domain)+MUSIC_DOMAIN+"radio";
    }
    public static String getUrlRegisterDevice(Context context) {
        return context.getString(R.string.server_domain)+MUSIC_DOMAIN+"deviceRegister";
    }
    /*******************************
     * API KEYS
     ************************************/
//category
    public static final String ID_PARENT = "parentId";
    public static final String LEVEL_CATEGORY = "level";
    public static final String COUNT_SUB="countSub";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_IMAGE = "image";
    public static final String ALL_PAGE="allpage";


    public static String KEY_STATUS = "status";
    public static String KEY_ALL_PAGE = "allpage";
    public static String KEY_SUCCESS = "success";
    public static String KEY_DATA = "data";


    public static String KEY_SINGER_NAME = "singerName";
    public static String KEY_LINK = "link";


    public static String KEY_SHARE_LINK = "link_app";

    public static String KEY_ISPARENT = "isParent";

    /******************
     * Parameters key.
     *******************/
//    category
    public static final String ID_CATEGORY = "categoryId";
    public static final String PAGE_NUMBER = "page";
    //Banner

    public static final String URL="url";
    public static final String IMAGE_BANNER="image";
    public static final String TYPE_BANNER="type";

//Song
    public static final String DESCRIPTION="description";
//    radio
    public static final String LINK_LIVE_STREAM="link";
    public static final String LINK_RADIO="mixlr";
    public static final String TYPE_RADIO="type";
}
