package com.nurulquran.audio.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.nurulquran.audio.R;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.database.DatabaseUtility;
import com.nurulquran.audio.fragment.PlayerThumbFragment;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.object.Song;
import com.nurulquran.audio.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadUpdateActivity extends Activity {
    private final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;
    private String downloadLink;
    private String localLink;
    private DownloadFileAsync downloadFileAsync;

    private  Bundle extras;

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extras = getIntent().getExtras();
        downloadLink = extras.getString("url_song");
        String file = extras.getString("file_name");
        String rootFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                + getString(R.string.app_name) + "/";

        File folder = new File(rootFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        localLink = rootFolder + file;
        Logger.e(localLink);
        Logger.e(downloadLink);
        downloadFileAsync = new DownloadFileAsync();
        downloadFileAsync.execute(downloadLink);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                if (extras.getBoolean("show_progress")==false){
                    mProgressDialog = new ProgressDialog(this);
                    mProgressDialog.setMessage(getString(R.string.downloading));
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                            "Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    downloadFileAsync.cancel(true);
                                    new Handler().postDelayed(new Runnable() {

                                        @Override
                                        public void run() {

                                            deleteDownloadFile(localLink);
                                        }
                                    }, 500);
                                    DownloadUpdateActivity.this.finish();
                                }
                            });
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                    return mProgressDialog;
                }else{

                }

            default:
                return null;
        }
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {
        @SuppressWarnings("deprecation")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (extras.getBoolean("show_progress")==false){
                showDialog(DIALOG_DOWNLOAD_PROGRESS);
            }
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;

            try {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lenghtOfFile = conexion.getContentLength();
                Logger.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(localLink);

                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            if (extras.getBoolean("show_progress")==false){
                int value = Integer.parseInt(progress[0]);
                mProgressDialog.setProgress(value);
            }
//            if (value == 50) {
//                addNewDownload();
//            }
        }

        @Override
        protected void onPostExecute(String unused) {

            Toast.makeText(getBaseContext(), R.string.downloadComplete,
                    Toast.LENGTH_SHORT).show();
            addNewDownload();
            String result = localLink.toString();
            result = result.substring(result.indexOf("/storage"), result.length());
            GlobalValue.getCurrentSong().addMoreDownload();
            Song songObj = new Song();
            songObj.setId(GlobalValue.getCurrentSong().getId());
            songObj.setName(GlobalValue.getCurrentSong().getName());
            songObj.setmTypePathFile(Song.PATH_FILE_DOWNLOAD);
            songObj.setUrl(result);
            songObj.setImage(GlobalValue.getCurrentSong().getImage());
            songObj.setDescription(GlobalValue.getCurrentSong().getDescription());
            songObj.setPosition(GlobalValue.getCurrentSong().getPosition());
            songObj.setShareLink(GlobalValue.getCurrentSong().getShareLink());
            songObj.setListenCount(GlobalValue.getCurrentSong().getListenCount());
            songObj.setDownloadCount(GlobalValue.getCurrentSong().getDownloadCount());
            DatabaseUtility databaseUtility = new DatabaseUtility(getApplicationContext());
            if (databaseUtility.checkFavourite(getBaseContext(), GlobalValue.getCurrentSong().getId())) {
                Log.e("Download", "Song exsit");
            } else {
                databaseUtility.insertFavorite(songObj);
            }
            finish();
        }

    }

    private void getCountDownLoad() {
        ModelManager.getCountDownAndCountListen(getApplicationContext(), GlobalValue.getCurrentSong().getId(), new ModelManagerListener() {
            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }

            @Override
            public void onSuccess(String json) {
                try {
                    JSONObject object = new JSONObject(json);
                    JSONArray jsonArray = object.getJSONArray(WebserviceApi.KEY_DATA);
                    JSONObject obj = jsonArray.getJSONObject(0);
                    PlayerThumbFragment.lblNumberDownload.setText(obj.getString("download"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void deleteDownloadFile(String url) {
        File f = new File(url);
        f.deleteOnExit();
    }

    private void addNewDownload() {
        String getUrl = WebserviceApi.getAddNewDownload(this
        ) + "?id="
                + GlobalValue.getCurrentSong().getId();
        ModelManager.sendGetRequest(getApplicationContext(), getUrl, null, false, new ModelManagerListener() {
            @Override
            public void onError(VolleyError error) {

            }

            @Override
            public void onSuccess(String json) {
                getCountDownLoad();
            }
        });
    }

}