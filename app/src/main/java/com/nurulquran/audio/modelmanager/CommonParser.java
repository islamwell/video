package com.nurulquran.audio.modelmanager;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.object.Album;
import com.nurulquran.audio.object.Banner;
import com.nurulquran.audio.object.CategoryMusic;
import com.nurulquran.audio.object.Radio;
import com.nurulquran.audio.object.Song;

/**
 * Parsing json data received from API
 */
public class CommonParser {
    private static String TAG = "CommonParser";

    public static List<Song> parseSongFromServer(String json) {
        List<Song> list = new ArrayList<Song>();
        try {
            JSONObject entry = new JSONObject(json);
            if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                    WebserviceApi.KEY_SUCCESS)) {
                JSONArray items = entry.getJSONArray(WebserviceApi.KEY_DATA);
                Song song = null;
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    song = new Song();
                    song.setId(getStringValue(item, WebserviceApi.KEY_ID));
                    song.setName(getStringValue(item, WebserviceApi.KEY_NAME));
                    String url = (getStringValue(item, WebserviceApi.KEY_LINK));
                    url.replaceAll(" ", "%20");
                    song.setUrl(url);
                    song.setDescription(getStringValue(item, WebserviceApi.DESCRIPTION));
                    song.setArtist(getStringValue(item,
                            WebserviceApi.KEY_SINGER_NAME));
                    song.setImage(getStringValue(item, WebserviceApi.KEY_IMAGE));
                    song.setShareLink(getStringValue(item,
                            WebserviceApi.KEY_SHARE_LINK));
                    song.setListenCount(getIntValue(item, "listen"));
                    song.setDownloadCount(getIntValue(item, "download"));

                    list.add(song);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<CategoryMusic> parseCategoryFromServer(String json) {
        List<CategoryMusic> list = new ArrayList<CategoryMusic>();
        try {
            JSONObject entry = new JSONObject(json);
            if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                    WebserviceApi.KEY_SUCCESS)) {
                JSONArray items = entry.getJSONArray(WebserviceApi.KEY_DATA);
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);

                    CategoryMusic category = new CategoryMusic();
                    //add Parent
                    category.setId(getIntValue(item, WebserviceApi.KEY_ID));
                    category.setTitle(getStringValue(item,
                            WebserviceApi.KEY_NAME));
                    category.setImage(getStringValue(item,
                            WebserviceApi.KEY_IMAGE));
                    category.setLevel(getStringValue(item, WebserviceApi.LEVEL_CATEGORY));
                    category.setIdParent(getStringValue(item, WebserviceApi.ID_PARENT));
                    category.setCountSub(getStringValue(item, WebserviceApi.COUNT_SUB));
                    list.add(category);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Album> parseAlbumFromServer(String json) {
        List<Album> list = new ArrayList<Album>();
        try {
            JSONObject entry = new JSONObject(json);
            if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                    WebserviceApi.KEY_SUCCESS)) {
                JSONArray items = entry.getJSONArray(WebserviceApi.KEY_DATA);
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);

                    Album album = new Album();

                    album.setId(getIntValue(item, WebserviceApi.KEY_ID));
                    album.setName(getStringValue(item, WebserviceApi.KEY_NAME));
                    album.setImage(getStringValue(item, WebserviceApi.KEY_IMAGE));

                    list.add(album);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList<Banner> parseBanner(String json) {
        try {
            if (!GlobalValue.mListBanner.isEmpty()) {
                GlobalValue.mListBanner.clear();
            }
            if (!GlobalValue.mListBigBanner.isEmpty()) {
                GlobalValue.mListBigBanner.clear();
            }
            JSONObject entry = new JSONObject(json);
            if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                    WebserviceApi.KEY_SUCCESS)) {
                JSONArray items = entry.getJSONArray(WebserviceApi.KEY_DATA);
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    Banner banner = new Banner(item.getString(WebserviceApi.TYPE_BANNER), item.getString(WebserviceApi.URL), item.getString(WebserviceApi.IMAGE_BANNER));

                    if (banner.getmType().equals("1")) {
                        GlobalValue.mListBigBanner.add(banner);
                    } else {
                        GlobalValue.mListBanner.add(banner);
                    }


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return GlobalValue.mListBanner;
    }

    public static Radio parseRadio(String json) {
        Radio radio = null;
        try {

            JSONObject entry = new JSONObject(json);
            if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                    WebserviceApi.KEY_SUCCESS)) {
                JSONObject items = entry.getJSONObject(WebserviceApi.KEY_DATA);
                radio = new Radio( items.getString(WebserviceApi.LINK_LIVE_STREAM));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return radio;
    }

    private static boolean getBooleanValue(JSONObject obj, String name) {
        try {
            return obj.getBoolean(name);
        } catch (Exception e) {
            //Log.e(TAG, "getBooleanValue error");
            return false;
        }
    }

    private static String getStringValue(JSONObject obj, String key) {
        try {
            return obj.isNull(key) ? "" : obj.getString(key);
        } catch (JSONException e) {
            return "";
        }
    }

    private static long getLongValue(JSONObject obj, String key) {
        try {
            return obj.isNull(key) ? 0L : obj.getLong(key);
        } catch (JSONException e) {
            return 0L;
        }
    }

    private static int getIntValue(JSONObject obj, String key) {
        try {
            return obj.isNull(key) ? 0 : obj.getInt(key);
        } catch (JSONException e) {
            return 0;
        }
    }

    private static Double getDoubleValue(JSONObject obj, String key) {
        double d = 0.0;
        try {
            return obj.isNull(key) ? d : obj.getDouble(key);
        } catch (JSONException e) {
            return d;
        }
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isJsonObject(JSONObject parent, String key) {
        try {
            JSONObject jObj = parent.getJSONObject(key);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String string) {
        try {
            Double.parseDouble(string);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
