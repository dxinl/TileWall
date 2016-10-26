package com.mx.dxinl.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.mx.dxinl.tilewall.TileWall;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Deng Xinliang on 2016/7/27.
 */
public class ListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        final List<Integer> itemViewTypes1 = new ArrayList<>();
        itemViewTypes1.add(R.layout.item_image);
        itemViewTypes1.add(R.layout.item_image_text);

        final List<Integer> itemViewTypes2 = new ArrayList<>();
        itemViewTypes2.add(R.layout.item_text);
        itemViewTypes2.add(R.layout.item_image_text);
        itemViewTypes2.add(R.layout.item_image);
        itemViewTypes2.add(R.layout.item_text);
        itemViewTypes2.add(R.layout.item_image_text);
        itemViewTypes2.add(R.layout.item_image);
        itemViewTypes2.add(R.layout.item_text);
        itemViewTypes2.add(R.layout.item_image_text);
        itemViewTypes2.add(R.layout.item_image);


        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 1111111;
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = new TileWall(viewGroup.getContext())
                            .setNumColumns(3)
                            .setNumRows(3)
                            .setForceDividing(true);
                }

                MyAdapter adapter;
                if (i % 2 == 0) {
                    adapter = new MyAdapter(viewGroup.getContext(), itemViewTypes1);
                } else {
                    adapter = new MyAdapter(viewGroup.getContext(), itemViewTypes2);
                }
                ((TileWall) view).setAdapter(adapter);

                return view;
            }
        });
    }
}
