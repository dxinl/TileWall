package com.mx.dxinl.tilewall;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

        Button startActivityBtn = (Button) findViewById(R.id.start_activity);
        startActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ListActivity.class));
            }
        });
    }

}
