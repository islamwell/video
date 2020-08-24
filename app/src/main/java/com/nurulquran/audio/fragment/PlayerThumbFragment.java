package com.nurulquran.audio.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.androidquery.AQuery;
import com.nurulquran.audio.BaseFragment;
import com.nurulquran.audio.R;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayerThumbFragment extends BaseFragment {

    private TextView lblNameSong, lblArtist;
    public static TextView lblNumberListen, lblNumberDownload;
    private View btnDownload, btnShare;
    private ImageView imgSong;
    private AQuery aq = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_thumb, container,
                false);
        aq = new AQuery(getActivity());
        initUI(view);
        return view;
    }

    private void initUI(View view) {
        imgSong = (ImageView) view.findViewById(R.id.imgSong);
        lblNumberDownload = (TextView) view
                .findViewById(R.id.lblNumberDownload);
        lblNumberListen = (TextView) view.findViewById(R.id.lblNumberListen);
        lblNameSong = (TextView) view.findViewById(R.id.lblNameSong);
        lblArtist = (TextView) view.findViewById(R.id.lblArtist);
        btnShare = view.findViewById(R.id.btnShare);
        btnDownload = view.findViewById(R.id.btnDownload);
        lblNameSong.setSelected(true);
        lblArtist.setSelected(true);

    }

    public void refreshData() {
        if (lblNameSong != null && lblArtist != null) {
            lblNameSong.setText(GlobalValue.getCurrentSong().getName());
            if (GlobalValue.getCurrentSong().getDescription() != null) {
                lblArtist.setText(Html.fromHtml(GlobalValue.getCurrentSong().getDescription()));
            } else {
                lblArtist.setText("");
            }
            getCountDownLoadAndCountDown();
            aq.id(imgSong).image(GlobalValue.getCurrentSong().getImage(), true,
                    false, 0, R.drawable.music_defaut);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshData();
                }
            }, 500);
        }
    }

    private void getCountDownLoadAndCountDown() {
        ModelManager.getCountDownAndCountListen(getActivity(), GlobalValue.getCurrentSong().getId(), new ModelManagerListener() {
            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }

            @Override
            public void onSuccess(String json) {
                try {
                    JSONObject object = new JSONObject(json);
                    JSONArray jsonArray = object.getJSONArray(WebserviceApi.KEY_DATA);
                    JSONObject obj= jsonArray.getJSONObject(0);
                    lblNumberDownload.setText(obj.getString("download"));
                    lblNumberListen.setText(obj.getString("listen"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
