package com.nurulquran.audio.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.object.Radio;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by phamtuan on 12/05/2016.
 */
public class MySharePreferences {
    private static final MySharePreferences MY_SHARE_PREFERENT = new MySharePreferences();
    private SharedPreferences sharedPreferences;
    public static final String RADIO_STORE = "Radio";
    public static final String RADIO_JSON = "item";
    public static final String FUNCTION_PLAYER = "function";
    public static final String REPEAT = "repeat";
    public static final String SHUFFLE = "shuffle";
    public static final String SORT_SONG = "sort song";
    public static final String TYPE_SORT = "type sort";

    public static MySharePreferences getInstance() {
        return MY_SHARE_PREFERENT;
    }

    public SharedPreferences getRadioStore(Context context) {
        sharedPreferences = context.getSharedPreferences(RADIO_STORE, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    public SharedPreferences.Editor getEditorRadioStore() {
        return sharedPreferences.edit();
    }

    public void saveRadio(String radioJson) {
        getEditorRadioStore().putString(RADIO_JSON, radioJson);
        getEditorRadioStore().commit();
    }

    public Radio getRadio(Context context) {
        String str = getRadioStore(context).getString(RADIO_JSON, "");
        Radio radio = null;
        try {
            JSONObject jsonObject = new JSONObject(str);
            if (jsonObject.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                    WebserviceApi.KEY_SUCCESS)) {
                JSONObject items = jsonObject.getJSONObject(WebserviceApi.KEY_DATA);
                radio = new Radio(items.getString(WebserviceApi.LINK_LIVE_STREAM));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return radio;
    }

    /*****
     * function in play
     *
     * @param context
     * @return
     */
    public SharedPreferences getFunctionPlayer(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FUNCTION_PLAYER, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    public void saveValueRepeat(Context context, boolean repeat) {
        getFunctionPlayer(context).edit().putBoolean(REPEAT, repeat).commit();
    }

    public boolean getValueRepeat(Context context) {
        return getFunctionPlayer(context).getBoolean(REPEAT, false);
    }

    public void saveValueShuffle(Context context, boolean shuffle) {
        getFunctionPlayer(context).edit().putBoolean(SHUFFLE, shuffle).commit();
    }

    public boolean getValueShuffle(Context context) {
        return getFunctionPlayer(context).getBoolean(SHUFFLE, false);
    }

    // sort song
    public SharedPreferences getSortSongStore(Context context) {
        return context.getSharedPreferences(SORT_SONG, Context.MODE_PRIVATE);
    }

    public void saveTypeSort(Context context, String typeSort) {
        getSortSongStore(context).edit().putString(TYPE_SORT, typeSort).commit();
    }

    public String getTypeSort(Context context) {
        return getSortSongStore(context).getString(TYPE_SORT, "");
    }
}
