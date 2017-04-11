package com.designedbyhumans.finalfeecalc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class categories extends ActionBarActivity {

    private double categoryRate = 1.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_categories, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setCategory(View view){
        int discountID = view.getId();
        Intent intent = new Intent();
        switch (discountID) {
            case R.id.textView21:
                categoryRate = 0.01;
                intent.putExtra("option_selected","other");
                break;
            case R.id.textView22:
                categoryRate = .02;
                intent.putExtra("option_selected","automotive tools, supplies, parts & accessories");
                break;
            case R.id.textView23:
                categoryRate = 0.04;
                intent.putExtra("option_selected","coins & paper money");
                break;
            case R.id.textView24:
                categoryRate = 0.06;
                intent.putExtra("option_selected","computer/tablets & networking and video game consoles");
                break;
            case R.id.textView25:
                categoryRate = 0.04;
                intent.putExtra("option_selected","consumer electronics, cameras, & photos");
                break;
            case R.id.textView26:
                categoryRate = 0.03;
                intent.putExtra("option_selected","musical instruments and gear");
                break;
            case R.id.textView27:
                categoryRate = 0.04;
                intent.putExtra("option_selected","stamps");
                break;
            default:
                categoryRate = 0.0;
                intent.putExtra("option_selected","");
                break;
        };

        intent.putExtra("categoryRate",categoryRate);
        intent.putExtra("action","set");
        setResult(RESULT_OK,intent);
        finish();
    }
}
