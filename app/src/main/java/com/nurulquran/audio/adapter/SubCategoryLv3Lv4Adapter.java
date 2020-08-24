package com.nurulquran.audio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nurulquran.audio.R;
import com.nurulquran.audio.object.CategoryMusic;

import java.util.ArrayList;

/**
 * Created by phamtuan on 03/05/2016.
 */
public class SubCategoryLv3Lv4Adapter extends BaseAdapter {
    private ArrayList<CategoryMusic> mListSubCategory;
    private Context context;
    private LayoutInflater mLayoutInflater;
    private ViewHolder viewHolder;

    public SubCategoryLv3Lv4Adapter(Context context, ArrayList<CategoryMusic> mListSubCategory) {
        this.context = context;
        this.mListSubCategory = mListSubCategory;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mListSubCategory.size();
    }

    @Override
    public Object getItem(int position) {
        return mListSubCategory.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CategoryMusic categoryMusic = mListSubCategory.get(position);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_sub_category_lv3lv4, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitleSubCategory.setText(categoryMusic.getTitle());
        return convertView;
    }

    private class ViewHolder {
        private TextView tvTitleSubCategory;

        public ViewHolder(View view) {
            tvTitleSubCategory = (TextView) view.findViewById(R.id.titleSubCategory);
            tvTitleSubCategory.setSelected(true);
        }
    }
}
