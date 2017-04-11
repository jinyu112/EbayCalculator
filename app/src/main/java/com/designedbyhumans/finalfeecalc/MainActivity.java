package com.designedbyhumans.finalfeecalc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.analytics.GoogleAnalytics;

import com.mopub.mobileads.MoPubView;
import com.revmob.RevMob;
import com.revmob.RevMobAdsListener;
import com.revmob.ads.banner.RevMobBanner;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private static final double THIRTY_CENTS = 0.30;
    private static final double PAYPAL_PERCENTAGE = 0.029;
    private static final double MAX_FEE = 750.0;
    private static final int DISCOUNT_REQ_CODE = 1;
    private static final int CATEGORY_REQ_CODE = 2;
    private static final int HELP_REQ_CODE =3;
    private static final int PROFIT_TEXT_SIZE = 53;
    private static final double SCALE_FACTOR = .9;
    private static final int INPUT_NUM_SIZE = 34;
    private static TextView tv;
    private static helveticaEditText et;
    private static double discountRate = 1.0;
    private static double categoryRate = 0.0;
    private static boolean keyboardState = false;
    private static boolean etTouched = false;
    private static int prevID = 0;
    private static boolean hasAds = true;
    private static boolean hasAds_FromInternalStorage = true;
    InternalDataForAdBoolean checkHasAds;
    private static LinearLayout mainActivityAdLayout;

    private MoPubView moPubView;

    IInAppBillingService mService;
    private static final String ITEM_SKU = "com.designedbyhumans.noads";
    //private static final String ITEM_SKU = "android.test.purchased";

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            //Log.d("BJJ","on service connected");
            Bundle ownedItems = null;
            try {
                if (mService != null) {
                    ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                    ArrayList ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    if (ownedItems != null && ownedSkus != null) {
                        int response = ownedItems.getInt("RESPONSE_CODE"); //if returns 0, request was successful
                        int lenSkus = ownedSkus.size();
                        //Log.d("BJJ", "size of ownedskus: " + ownedSkus.size());
                        //Log.d("BJJ", "response: " + response);
                        if (response == 0 && lenSkus > 0) {
                            for (int i = 0; i < lenSkus; ++i) {
                                String sku = (String) ownedSkus.get(i);
                                //Log.d("BJJ", "PURACHSED ITEM " + i + " === " + sku);

                                // if purchases from google play are found and matches the purchase made, remove ads
                                if (sku.equals(ITEM_SKU)) {
                                    hasAds = false;
                                    checkHasAds.removeAds_InternalStorage(); //set internal flag to false (meaning app does not have ads)
                                    hasAds_FromInternalStorage = false;
                                    removeAdsAfterSuccessPurchase(findViewById(android.R.id.content));
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                Log.d("BJJ","failed to get purchases");
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //in app purchase connection
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        //initiliaze mopub ad stuff
        moPubView = (MoPubView) findViewById(R.id.bannerLayout);
        moPubView.setAdUnitId(getString(R.string.mopub_ad_id)); // Enter your Ad Unit ID from www.mopub.com
        moPubView.loadAd();
        mainActivityAdLayout = (LinearLayout) findViewById(R.id.mainActivityAdLinearLayout);

        //load internal data for hasAds boolean
        checkHasAds = new InternalDataForAdBoolean(this);
        checkHasAds.initHasAdsData(); //this should do nothing if it's not the first time the app is started


        //add ga tracker
        ((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);

        //Define edittexts
        et = (helveticaEditText) findViewById(R.id.editText1);
        et.setCursorVisible(false);
        et.addTextChangedListener(new NumberTextWatcher(et));
        //hide and unhide the keyboard when the edittext is touched
        et.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View view, MotionEvent motionEvent) {
                keyboardState = true;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { //this line makes sure the code is only run once instead of 3 times
                    if (etTouched && prevID == R.id.editText1) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                        keyboardState = false;
                        etTouched = false;
                        prevID = R.id.editText1;
                        return true;
                    } else {
                        keyboardState=false;
                        etTouched = true;
                        prevID = R.id.editText1;
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    prevID = R.id.editText2; //yes this should be edittext2
                }
                return false;
            }
        });
        //prevent longclick or doubleclick for ice cream sandwich and earlier
        et.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {

            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode,
                                               MenuItem item) {
                return false;
            }
        });
        //disables copy and paste pop up in older api versions (confirmed this worked on 4/11/15)
        et.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.clear();
            }
        });
        et.setLongClickable(false);
        et.setTextIsSelectable(false);



        et = (helveticaEditText) findViewById(R.id.editText2);
        et.setCursorVisible(false);
        et.addTextChangedListener(new NumberTextWatcher(et));
        et.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                keyboardState = true;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (etTouched && prevID == R.id.editText2) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                        keyboardState = false;
                        etTouched = false;
                        prevID = R.id.editText2;
                        return true;
                    } else {
                        keyboardState=false;
                        etTouched = true;
                        prevID = R.id.editText2;
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    prevID = R.id.editText3;
                }
                return false;
            }
        });
        //prevent longclick or doubleclick for ice cream sandwich and earlier
        et.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {

            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode,
                                               MenuItem item) {
                return false;
            }
        });
        et.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.clear();
            }
        });
        et.setLongClickable(false);
        et.setTextIsSelectable(false);



        et = (helveticaEditText) findViewById(R.id.editText3);
        et.setCursorVisible(false);
        et.addTextChangedListener(new NumberTextWatcher(et));
        et.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                keyboardState = true;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (etTouched && prevID == R.id.editText3) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                        keyboardState = false;
                        etTouched = false;
                        prevID = R.id.editText3;
                        return true;
                    } else {
                        keyboardState=false;
                        etTouched = true;
                        prevID = R.id.editText3;
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    prevID = R.id.editText4;
                }
                return false;
            }
        });
        //prevent longclick or doubleclick for ice cream sandwich and earlier
        et.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {

            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode,
                                               MenuItem item) {
                return false;
            }
        });
        et.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.clear();
            }
        });
        et.setLongClickable(false);
        et.setTextIsSelectable(false);



        et = (helveticaEditText) findViewById(R.id.editText4);
        et.setCursorVisible(false);
        et.addTextChangedListener(new NumberTextWatcher(et));
        et.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                keyboardState = true;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (etTouched && prevID == R.id.editText4) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                        keyboardState = false;
                        etTouched = false;
                        prevID = R.id.editText4;
                        return true;
                    } else {
                        keyboardState=false;
                        etTouched = true;
                        prevID = R.id.editText4;
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    keyboardState = false;
                }
                return false;
            }
        });
        //prevent longclick or doubleclick for ice cream sandwich and earlier
        et.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {

            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode,
                                               MenuItem item) {
                return false;
            }
        });
        et.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.clear();
            }
        });
        et.setLongClickable(false);
        et.setTextIsSelectable(false);



        //define textviews
        tv = (TextView) findViewById(R.id.category);
        tv.setVisibility(View.INVISIBLE);
        tv = (TextView) findViewById(R.id.categoryClick);
        tv.setVisibility(View.INVISIBLE);

        //total price
        final InputMethodManager[] imm = new InputMethodManager[1];
        tv = (TextView) findViewById(R.id.textView1);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                et = (helveticaEditText) findViewById(R.id.editText1);
                imm[0] = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                clickLabel(imm[0],et);
            }
        });

        //shipping charge
        tv = (TextView) findViewById(R.id.textView2);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                et = (helveticaEditText) findViewById(R.id.editText2);
                imm[0] = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                clickLabel(imm[0],et);
            }
        });
        //shipping cost
        tv = (TextView) findViewById(R.id.textView3);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                et = (helveticaEditText) findViewById(R.id.editText3);
                imm[0] = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                clickLabel(imm[0],et);
            }
        });
        //item cost
        tv = (TextView) findViewById(R.id.textView4);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                et = (helveticaEditText) findViewById(R.id.editText4);
                imm[0] = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                clickLabel(imm[0],et);
            }
        });

        //Define what happens when the calculation button is selected
        ImageView calcButton = (ImageView) findViewById(R.id.button);
        calcButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        double TAOS = 0.0;
                        double profit = 0.0;
                        double ebayFee = 0.0;
                        double paypalFee = 0.0;
                        double soldPrice = 0.0;
                        double shippingCharge = 0.0;
                        double shippingCost = 0.0;
                        double itemCost = 0.0;
                        double shrinkRatio = 0.0;

                        //retrieve the sold price
                        et = (helveticaEditText) findViewById(R.id.editText1);
                        String tempStr = et.getText().toString();
                        tempStr = tempStr.replaceAll("[$,]", "");
                        if (tempStr.isEmpty()) {
                            Toast msg;
                            msg = Toast.makeText(MainActivity.this, "Input Total Price please.", Toast.LENGTH_SHORT);
                            msg.show();
                        } else {
                            try {
                                soldPrice = Double.parseDouble(tempStr);
                            }catch(Exception e) {soldPrice = 0.0;}
                            //retrieve the shipping charge
                            et = (helveticaEditText) findViewById(R.id.editText2);
                            tempStr = et.getText().toString();
                            tempStr = tempStr.replaceAll("[$,]", "");
                            try {
                                shippingCharge = Double.parseDouble(tempStr);
                            } catch(Exception e) {shippingCharge = 0.0;}

                            //retrieve the shipping charge
                            et = (helveticaEditText) findViewById(R.id.editText3);
                            tempStr = et.getText().toString();
                            tempStr = tempStr.replaceAll("[$,]", "");
                            try {
                                shippingCost = Double.parseDouble(tempStr);
                            } catch(Exception e) {shippingCost = 0.0;}

                            //retrieve the shipping charge
                            et = (helveticaEditText) findViewById(R.id.editText4);
                            tempStr = et.getText().toString();
                            tempStr = tempStr.replaceAll("[$,]", "");
                            try {
                                itemCost = Double.parseDouble(tempStr);
                            } catch(Exception e) {itemCost = 0.0;}

                            //Do math
                            TAOS = soldPrice + shippingCharge;
                            //ebayFee = TAOS * ebayFeePercentage;
                            ebayFee = soldPrice*(.1-categoryRate)*discountRate+shippingCharge*(.1-categoryRate);
                            if (ebayFee>MAX_FEE){
                                ebayFee = MAX_FEE;
                            }
                            paypalFee = TAOS * PAYPAL_PERCENTAGE + THIRTY_CENTS;
                            profit = TAOS - shippingCost - itemCost - ebayFee - paypalFee;

                            //Display
                            tv = (TextView) findViewById(R.id.textView6);
                            tv.setText(String.format("%.2f", ebayFee));

                            tv = (TextView) findViewById(R.id.textView8);
                            tv.setText(String.format("%.2f",paypalFee));

                            tv = (TextView) findViewById(R.id.textView10);

                            //resize the profit value if too large to fit in the textview
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,PROFIT_TEXT_SIZE);
                            if (isMarqueed(String.format("%.2f",profit),tv.getWidth(),tv)) {
                                shrinkRatio = calcShrinkRatio(String.format("%.2f",profit),tv.getWidth(),tv); //proportional to how much the text is greater than the tv
                                int shrunkenSize = (int) Math.floor(PROFIT_TEXT_SIZE*SCALE_FACTOR*shrinkRatio);
                                shrunkenSize = Math.max(shrunkenSize,INPUT_NUM_SIZE);
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,shrunkenSize);
                            }else {
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,PROFIT_TEXT_SIZE);
                            }
                            tv.setText(String.format("%.2f",profit));

                        }

                    }
                });


        //Define what happens when the clear button is selected
        ImageView clearButton = (ImageView) findViewById(R.id.buttonclear);
        clearButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        et=(helveticaEditText) findViewById(R.id.editText1);
                        et.setText("0.00");
                        et=(helveticaEditText) findViewById(R.id.editText2);
                        et.setText("0.00");
                        et=(helveticaEditText) findViewById(R.id.editText3);
                        et.setText("0.00");
                        et=(helveticaEditText) findViewById(R.id.editText4);
                        et.setText("0.00");

                        tv=(TextView)findViewById(R.id.textView6);
                        tv.setText("");
                        tv=(TextView)findViewById(R.id.textView8);
                        tv.setText("");
                        tv=(TextView)findViewById(R.id.textView10);
                        tv.setText("");

                        tv = (TextView) findViewById(R.id.discountClick);
                        tv.setText("none");
                        discountRate = 1.0;

                        tv = (TextView) findViewById(R.id.categoryClick);
                        tv.setText("other");
                        tv.setVisibility(View.INVISIBLE);
                        tv = (TextView) findViewById(R.id.category);
                        tv.setVisibility(View.INVISIBLE);
                        categoryRate = 0.0;

                        keyboardState = false;
                        etTouched = false;
                        prevID = 0;
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    //Define what happens when discount textview is selected
    public void goToDiscounts(View view){
        Intent intent = new Intent(this,discounts.class);
        startActivityForResult(intent, DISCOUNT_REQ_CODE);
    }

    //Define what happens when category textview is selected
    public void goToCategories(View view){
        Intent intent = new Intent(this,categories.class);
        startActivityForResult(intent,CATEGORY_REQ_CODE);
    }

    //Define what happens when help button is selected
    public void goToHelp(View view){
        Intent intent = new Intent(this,help.class);
        startActivityForResult(intent,HELP_REQ_CODE);
    }

//secondary activities return data
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String categoryType="other";
        etTouched = false;

        if (requestCode == DISCOUNT_REQ_CODE && resultCode == RESULT_OK){
            String action = data.getStringExtra("action");
            String discountType = "none";
            if (action.equals("set")) {
                discountRate = data.getDoubleExtra("discountRate",1.0);
                tv = (TextView) findViewById(R.id.discountClick);
                discountType = data.getStringExtra("option_selected");
                tv.setText(discountType);

                // if 'ebay store' or 'both' discounts
                if (discountType.equals("ebay store") || discountType.equals("both")) {
                    tv = (TextView) findViewById(R.id.categoryClick);
                    tv.setVisibility(View.VISIBLE);
                    categoryType = tv.getText().toString();
                    retrieveCategoryDiscount(categoryType);
                    tv = (TextView) findViewById(R.id.category);
                    tv.setVisibility(View.VISIBLE);
                }
                else {
                    tv = (TextView) findViewById(R.id.categoryClick);
                    tv.setVisibility(View.INVISIBLE);
                    tv = (TextView) findViewById(R.id.category);
                    tv.setVisibility(View.INVISIBLE);
                    categoryRate=0.0;
                }
            }
        }
        else if (requestCode == CATEGORY_REQ_CODE && resultCode == RESULT_OK) {
            String action = data.getStringExtra("action");
            categoryType = "other";
            if (action.equals("set")) {
                categoryRate = data.getDoubleExtra("categoryRate",1.0);
            }
            tv = (TextView) findViewById(R.id.categoryClick);
            categoryType =data.getStringExtra("option_selected");
            if (isMarqueed(categoryType,tv.getWidth(),tv)) {
                categoryType=truncateText(categoryType,tv.getWidth(),tv);
            }
            tv.setText(categoryType);
        }
    }

    //setting the correct category discount rate when coming from a different discount type
    public void retrieveCategoryDiscount(String categoryStr) {
        if (categoryStr.equals("other")){
            categoryRate = .01;
        } else if (categoryStr.matches("^auto.+")) {
            categoryRate = .02;
        } else if (categoryStr.matches("^coins.+")) {
            categoryRate = .04;
        } else if (categoryStr.matches("^computer.+")) {
            categoryRate = .06;
        } else if (categoryStr.matches("^consumer.+")) {
            categoryRate = .04;
        } else if (categoryStr.matches("^music.+")) {
            categoryRate = .03;
        } else if (categoryStr.equals("stamps")) {
            categoryRate = .04;
        } else {
            categoryRate = 0.0;
        }
    }

    //painfully confusing logic for hiding and unhiding the keyboard upon clicking a label (total price)
    public void clickLabel(InputMethodManager immtemp, helveticaEditText ettemp) {

        //clicking the label once in the edittext view will hide the keyboard
        if (prevID!=ettemp.getId()) {
            ettemp.requestFocus();
            immtemp.showSoftInput(ettemp, InputMethodManager.SHOW_IMPLICIT); // show keyboard
            keyboardState = true;
            etTouched = true;
        }
        else {
            if (keyboardState || etTouched) {
                immtemp.hideSoftInputFromWindow(ettemp.getWindowToken(), 0); //hide keyboard
                keyboardState = false;
                etTouched = false;
            } else {
                ettemp.requestFocus();
                immtemp.showSoftInput(ettemp, InputMethodManager.SHOW_IMPLICIT); // show keyboard
                keyboardState = true;
                etTouched = true;
            }
        }

        prevID = ettemp.getId();

    }

    //checks to see if the string width is too large for the textview
    public boolean isMarqueed(String text, int textWidth, TextView tv) {
        Paint testPaint = new Paint();
        testPaint.set(tv.getPaint());
        boolean isMarquee = true;
        if (textWidth > 0) {
            int availableWidth = (int) (textWidth - tv.getPaddingLeft() - tv.getPaddingRight() - testPaint.measureText(text));
            if (availableWidth > 0) {
                isMarquee = false;
            } else {
                isMarquee = true;
            }
        }
        return isMarquee;
    }

    //truncates the category string by measuring the width of the tv and comparing with the width of the string
    public String truncateText(String text, int textWidth, TextView tv) {
        Paint testPaint = new Paint();
        testPaint.set(tv.getPaint());
        int strLength=text.length();
        double ratioTV2Text = textWidth/testPaint.measureText(text);
        int newStrLength = (int)Math.floor(strLength*ratioTV2Text);
        if (newStrLength>0) {
            text = text.substring(0, Math.min(text.length(), newStrLength - 3));
            text = text.concat("...");
        }
        return text;
    }

    public double calcShrinkRatio(String text, int textWidth, TextView tv) {
        Paint testPaint = new Paint();
        testPaint.set(tv.getPaint());
        double ratioTV2Text = textWidth/testPaint.measureText(text);
        return ratioTV2Text;
    }


    @Override
    protected void onStart() {
        super.onStart();
        //more google analytics
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        removeAdsAfterSuccessPurchase(findViewById(android.R.id.content));
    }

    public void removeAdsAfterSuccessPurchase(View view) {
        hasAds_FromInternalStorage = checkHasAds.checkAdState();
        if (!hasAds || !hasAds_FromInternalStorage) {
            mainActivityAdLayout.setVisibility(View.GONE);
            View adsLayout = (View) findViewById(R.id.bannerLayout);
            mainActivityAdLayout.removeView(adsLayout);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        //more google analytics
        GoogleAnalytics.getInstance(this).reportActivityStop(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();;
        moPubView.destroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        moPubView.loadAd();
    }

}
