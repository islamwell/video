package com.nurulquran.audio.network;

/**
 * Created by phamtuan on 20/10/2015.
 */
public interface HttpListener<T> {
    void onHttpResponse(T response);

}
