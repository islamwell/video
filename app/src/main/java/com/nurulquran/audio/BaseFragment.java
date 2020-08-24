package com.nurulquran.audio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.nurulquran.audio.activity.BaseActivity;
import com.nurulquran.audio.activity.MainActivity;
import com.nurulquran.audio.activity.PlayerActivity;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.database.DatabaseUtility;
import com.nurulquran.audio.fragment.MyDownLoadFragment;
import com.nurulquran.audio.modelmanager.CommonParser;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.object.Radio;

public class BaseFragment extends Fragment {
    private TextView lblHeader;
    public Context self;
    public String TAG;
    public String URL_MAIN;
    protected Radio radio;
    protected ImageView btnMenu;


    public BaseFragment() {
        setArguments(new Bundle());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        self = getActivity();
        TAG = this.getClass().getSimpleName();
        if (GlobalValue.listSongPlay == null) {
            GlobalValue.constructor(getActivity());
        }
        // ((MainActivity)self).hideBannerAd();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (GlobalValue.listSongPlay.isEmpty()) {
            getMainActivity().hideMediaFooter();
        }
    }

    protected void initUIBase(View view) {
        lblHeader = (TextView) view.findViewById(R.id.lblHeader);
        lblHeader.setSelected(true);
    }

    protected void setButtonBack(View view) {
        btnMenu = (ImageView) view.findViewById(R.id.btnMenu);
        btnMenu.setImageResource(R.drawable.ic_arrow_back_white_36dp);
        btnMenu.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
    }

    protected void setButtonMenu(View view) {
        btnMenu = (ImageView) view.findViewById(R.id.btnMenu);
        btnMenu.setImageResource(R.drawable.ic_menu_white_36dp);
        btnMenu.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getMainActivity().menu.showMenu();
                    }
                });
    }
    protected void setButtonMenu(View view , final InputMethodManager inputMethodManager, final EditText editText) {
        btnMenu = (ImageView) view.findViewById(R.id.btnMenu);
        btnMenu.setImageResource(R.drawable.ic_menu_white_36dp);
        btnMenu.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getMainActivity().menu.showMenu();
                        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(),0);
                    }
                });
    }

    protected void setHeaderTitle(String header) {
        lblHeader.setText(header);
    }

    protected void setHeaderTitle(int header) {
        lblHeader.setText(header);
    }

    public MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    public void showDialogNoNetwork() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent settings = new Intent(
                                android.provider.Settings.ACTION_WIFI_SETTINGS);
                        settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(settings);
                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.app_name).setMessage(R.string.noNetwork)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }

    protected void showToast(int idString) {
        Toast.makeText(getActivity(), idString, Toast.LENGTH_SHORT).show();
    }

    protected void gotoPlayer() {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        startActivityForResult(intent, BaseActivity.REQUEST_CODE);
    }

    protected boolean checkUrl(String url) {
        return (url.startsWith("http://") || url.startsWith("https://"));
    }
}
