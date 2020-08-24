package com.nurulquran.audio.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nurulquran.audio.BaseFragment;
import com.nurulquran.audio.R;
import com.nurulquran.audio.config.WebserviceConfig;
import com.nurulquran.audio.slidingmenu.SlidingMenu;

/**
 * Created by pham on 28/04/2016.
 */
public class MultipleYourReward extends BaseFragment {
    private View view;
    private TextView tvLink;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_multiple_your_reward, container, false);
        initUIBase(view);
        setButtonMenu(view);
        setHeaderTitle(R.string.title_multiple_your_reward);
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
        tvLink = (TextView) view.findViewById(R.id.tvLink);
        tvLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = tvLink.getText().toString();
                if (checkUrl(url)) {

                } else {
                    url = "http://"+url;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }

}
