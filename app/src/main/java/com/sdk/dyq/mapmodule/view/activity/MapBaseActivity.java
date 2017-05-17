package com.sdk.dyq.mapmodule.view.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.sdk.dyq.mapmodule.R;

/**
 * Created by yuanqiang on 2017/5/16.
 */

public class MapBaseActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()){
            case R.id.MapTrailActivity:
                intent.setClass(MapBaseActivity.this,MapTrailActivity.class);
                break;
            case R.id.MapAnimActivity:
                intent.setClass(MapBaseActivity.this,MapAnimActivity.class);
                break;
        }
        startActivity(intent);
        this.finish();
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }
}
