package com.nurulquran.audio.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.nurulquran.audio.BaseFragment;
import com.nurulquran.audio.R;
import com.nurulquran.audio.activity.BaseActivity;
import com.nurulquran.audio.slidingmenu.SlidingMenu;

public class AboutFragment extends BaseFragment implements OnClickListener{

    private TextView tvLink1, tvLink2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        initUIBase(view);
        setButtonMenu(view);
        setHeaderTitle(R.string.title_about_us);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getMainActivity().menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            getMainActivity().setVisibilityFooter();
            getMainActivity().hideBannerAd();
        }
    }

    @Override
    protected void initUIBase(View view) {
        super.initUIBase(view);
        tvLink1 = (TextView) view.findViewById(R.id.tvlink1);
        tvLink2 = (TextView) view.findViewById(R.id.tvLink2);
        tvLink1.setOnClickListener(this);
        tvLink2.setOnClickListener(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvlink1:
                openWebPage(tvLink1.getText().toString());
                break;
            case R.id.tvLink2:
                openWebPage(tvLink2.getText().toString());
                break;
        }
    }
    private void openWebPage(String url) {
        if (checkUrl(url)) {

        }else {
            url = "http://"+url;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
        startActivity(intent);
    }
}
