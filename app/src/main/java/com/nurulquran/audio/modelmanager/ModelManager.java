package com.nurulquran.audio.modelmanager;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.nurulquran.audio.PacketUtility;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.config.WebserviceConfig;
import com.nurulquran.audio.network.HttpError;
import com.nurulquran.audio.network.HttpGet;
import com.nurulquran.audio.network.HttpListener;

import java.util.HashMap;
import java.util.Map;

public class ModelManager {
    private static String TAG = "ModelManager";

    // demo
    // ==============================


    public static void sendGetRequest(Context context, String url, Map<String, String> params,
                                      boolean isProgress, final ModelManagerListener listener) {

        if (params == null)
            params = new HashMap<>();

        new HttpGet(context, url, params, isProgress, new HttpListener() {
            @Override
            public void onHttpResponse(Object respone) {
                if (respone != null) {
                    listener.onSuccess(respone.toString());
                } else {
                    listener.onError(null);
                }
            }
        }, new HttpError() {
            @Override
            public void onHttpError(VolleyError volleyError) {
                listener.onError(volleyError);
            }
        });
    }

    public static void registerDevice(Context context, final String gcmId) {
        Map<String, String> params = new HashMap<>();
        params.put("gcm_id", gcmId);
        params.put("type", "1");
        params.put("ime", PacketUtility.getImei(context));
        params.put("status", "1");
        new HttpGet(context, WebserviceApi.getUrlRegisterDevice(context), params, false, new HttpListener() {
            @Override
            public void onHttpResponse(Object response) {
                String s = (String) response;
            }
        }, new HttpError() {
            @Override
            public void onHttpError(VolleyError volleyError) {
                volleyError.printStackTrace();
            }
        });
    }

    //get Category
    public static void getCategory(Context context, String page, final ModelManagerListener listener) {
        Map<String, String> params = getParams();
        params.put(WebserviceApi.PAGE_NUMBER, page);
        HttpGet httpGet = new HttpGet(context, WebserviceApi.getCategoriesApi(context), params, false, new HttpListener() {
            @Override
            public void onHttpResponse(Object response) {
                listener.onSuccess((String) response);
            }
        }, new HttpError() {
            @Override
            public void onHttpError(VolleyError volleyError) {
                listener.onError(volleyError);
            }
        });
    }

    public static void getSubCategory(Context context, String idCategory, String page, final ModelManagerListener listener) {
        Map<String, String> params = getParams();
        params.put(WebserviceApi.ID_CATEGORY, idCategory);
        params.put(WebserviceApi.PAGE_NUMBER, page);
        HttpGet httpGet = new HttpGet(context, WebserviceApi.getSubCategoryApi(context), params, false, new HttpListener() {
            @Override
            public void onHttpResponse(Object response) {
                listener.onSuccess((String) response);
            }
        }, new HttpError() {
            @Override
            public void onHttpError(VolleyError volleyError) {
                listener.onError(volleyError);
            }
        });
    }

    public static void getListSongCategory(Context context, String idCategory, String page, final ModelManagerListener listener) {
        Map<String, String> params = getParams();
        params.put(WebserviceApi.ID_CATEGORY, idCategory);
        params.put(WebserviceApi.PAGE_NUMBER, page);
        HttpGet httpGet = new HttpGet(context, WebserviceApi.getSongByCategory(context), params, false, new HttpListener() {
            @Override
            public void onHttpResponse(Object response) {
                listener.onSuccess((String) response);
            }
        }, new HttpError() {
            @Override
            public void onHttpError(VolleyError volleyError) {
                listener.onError(volleyError);
            }
        });
    }

    public static void loadBanner(Context context, final ModelManagerListener listener) {
        HttpGet httpGet = new HttpGet(context, WebserviceApi.getBannerApi(context), null, false, new HttpListener() {
            @Override
            public void onHttpResponse(Object response) {
                listener.onSuccess((String) response);
            }
        }, new HttpError() {
            @Override
            public void onHttpError(VolleyError volleyError) {
                listener.onError(volleyError);
            }
        });
    }

    public static void loadRadio(Context context, final ModelManagerListener listener) {
        HttpGet httpGet = new HttpGet(context, WebserviceApi.getRadioApi(context), null, false, new HttpListener() {
            @Override
            public void onHttpResponse(Object response) {
                listener.onSuccess((String) response);
            }
        }, new HttpError() {
            @Override
            public void onHttpError(VolleyError volleyError) {
                listener.onError(volleyError);
            }
        });
    }

    public static void getCountDownAndCountListen(Context context, String idSong, final ModelManagerListener listener) {
        Map<String, String> params = getParams();
        params.put("id", idSong);
        HttpGet httpGet = new HttpGet(context, WebserviceApi.getSongs(context), params, false, new HttpListener() {
            @Override
            public void onHttpResponse(Object response) {
                listener.onSuccess((String) response);
            }
        }, new HttpError() {
            @Override
            public void onHttpError(VolleyError volleyError) {
                listener.onError(volleyError);
            }
        });
    }

    private static Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        return params;
    }

}