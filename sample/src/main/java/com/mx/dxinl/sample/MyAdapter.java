package com.mx.dxinl.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Deng Xinliang on 2016/7/27.
 */
final class MyAdapter extends BaseAdapter {
    Context context;
    List<Integer> itemViewTypes = new ArrayList<>();

    public MyAdapter(Context context, List<Integer> itemViewTypes) {
        this.context = context;
        this.itemViewTypes.addAll(itemViewTypes);
    }

    public void setItemViewTypes(List<Integer> itemViewTypes) {
        this.itemViewTypes.clear();
        this.itemViewTypes.addAll(itemViewTypes);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemViewTypes.size();
    }

    @Override
    public Object getItem(int i) {
        return itemViewTypes.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return itemViewTypes.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int type = getItemViewType(i);
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(type, viewGroup, false);
        }

        return view;
    }
}
