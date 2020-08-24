package com.nurulquran.audio.adapter;

import java.util.List;

import com.androidquery.AQuery;
import com.nurulquran.audio.R;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.object.Album;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumAdapter extends BaseAdapter {

    private List<Album> listAlbum;
    private LayoutInflater layoutInflater;
    private AQuery listAq;

    public AlbumAdapter(Context context, List<Album> listAlbum) {
        this.listAlbum = listAlbum;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listAq = new AQuery(context);
    }

    public int getCount() {
        return listAlbum.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_category_music, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Album item = listAlbum.get(position);
        if (item != null) {
            AQuery aq = listAq.recycle(convertView);
            ((TextView) convertView.findViewById(R.id.lblCategotyMusicName)).setText(item.getName());
            ((TextView) convertView.findViewById(R.id.lblCategotyMusicName)).setSelected(true);
            aq.id(R.id.imgCategoryMusic).image(item.getImage(), true, true, 0, R.drawable.img_not_found,
                    GlobalValue.bmNoImageAvailable, AQuery.FADE_IN_NETWORK, 0);
        }

        return convertView;
    }

    class ViewHolder {
        TextView mTvName;
        ImageView ivCategory;

        public ViewHolder(View view) {
            this.ivCategory = (ImageView) view.findViewById(R.id.imgCategoryMusic);
            this.mTvName = (TextView) view.findViewById(R.id.lblCategotyMusicName);
        }
    }
}
