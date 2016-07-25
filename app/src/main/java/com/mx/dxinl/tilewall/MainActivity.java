package com.mx.dxinl.tilewall;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.mx.dxinl.library.TileWall;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int flag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final List<Integer> itemViewTypes1 = new ArrayList<>();
        itemViewTypes1.add(R.layout.item_image);
        itemViewTypes1.add(R.layout.item_image_text);
        /*itemViewTypes1.add(R.layout.item_text);
        itemViewTypes1.add(R.layout.item_image);
        itemViewTypes1.add(R.layout.item_image_text);
        itemViewTypes1.add(R.layout.item_text);*/

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

        TileWall tileWall = (TileWall) findViewById(R.id.tileWall);
        final MyAdapter adapter = new MyAdapter(this, itemViewTypes1);
        tileWall.setAdapter(adapter);

        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag == 1) {
                    adapter.setItemViewTypes(itemViewTypes2);
                    flag = 2;
                } else {
                    adapter.setItemViewTypes(itemViewTypes1);
                    flag = 1;
                }
            }
        });
    }

    private final class MyAdapter extends BaseAdapter {
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
}
