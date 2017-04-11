package com.designedbyhumans.finalfeecalc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class discounts extends ActionBarActivity {

    private double discountRate = 1.0; //1=none
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discounts);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_discounts, menu);
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

    public void setDiscount(View view){
        int discountID = view.getId();
        Intent intent = new Intent();
        switch (discountID) {
            case R.id.textView11:
                discountRate = 1.0;
                intent.putExtra("option_selected","none");
                break;
            case R.id.textView12:
                discountRate = 1.0;
                intent.putExtra("option_selected","ebay store");
                break;
            case R.id.textView13:
                discountRate = 0.8;
                intent.putExtra("option_selected","top rated");
                break;
            case R.id.textView14:
                discountRate = 0.8;
                intent.putExtra("option_selected","both");
                break;
            default:
                discountRate = 1.0;
                intent.putExtra("option_selected","");
                break;
        };

        intent.putExtra("discountRate",discountRate);
        intent.putExtra("action","set");
        setResult(RESULT_OK,intent);
        finish();
    }
}
