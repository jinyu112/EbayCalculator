package com.designedbyhumans.finalfeecalc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.designedbyhumans.inappbilling.util.IabHelper;
import com.designedbyhumans.inappbilling.util.IabResult;
import com.designedbyhumans.inappbilling.util.Inventory;
import com.designedbyhumans.inappbilling.util.Purchase;
import com.mopub.mobileads.MoPubView;
import com.revmob.RevMob;
import com.revmob.RevMobAdsListener;
import com.revmob.ads.banner.RevMobBanner;

import java.util.ArrayList;


public class help extends ActionBarActivity {

    private MoPubView moPubView;
    private static InternalDataForAdBoolean checkHasAds;
    private static boolean hasAds = true;
    private static boolean hasAds_FromInternalStorage = true;
    private static LinearLayout helpActivityAdLayout;
    private static ImageView noAdsButton;
    private static boolean successfulSetupToIAP;
    IabHelper mHelper;
    private static final String ITEM_SKU = "com.designedbyhumans.noads";
    //private static final String ITEM_SKU = "android.test.purchased";
    IInAppBillingService mService;

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

//    public void consumeItem() {
//        mHelper.queryInventoryAsync(mReceivedInventoryListener);
//    }
//
//    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
//            = new IabHelper.QueryInventoryFinishedListener() {
//        public void onQueryInventoryFinished(IabResult result,
//                                             Inventory inventory) {
//
//            if (result.isFailure()) {
//                //Log.d("BJJ","error consuming item");
//            } else {
//                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
//                        mConsumeFinishedListener);
//                //Log.d("BJJ","consuming item");
//            }
//        }
//    };
//
//    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
//            new IabHelper.OnConsumeFinishedListener() {
//                public void onConsumeFinished(Purchase purchase,
//                                              IabResult result) {
//
//                    if (result.isSuccess()) {
//                        //Log.d("BJJ", "Purchase successful! removing ads...");
//                        //removeAdsAfterSuccessPurchase(findViewById(android.R.id.content)); //current view is android.R.id.content
//                    } else {
//                        // handle error
//                    }
//                }
//            };

    //listener for successful purchase or not (only called when no ads button is touched)
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                //Log.d("BJJ", "Purchase failed....");
                return;
            }
            //if successful purchase set hasads to false and remove ad view
            else if (purchase.getSku().equals(ITEM_SKU)) {
                hasAds = false;
                checkHasAds.removeAds_InternalStorage(); //set internal flag to false (meaning app does not have ads)
                hasAds_FromInternalStorage = false;
                removeAdsAfterSuccessPurchase(findViewById(android.R.id.content)); //current view is android.R.id.content
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        //in app purchase initialization
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        mHelper = new IabHelper(this,getString(R.string.public_key));
        mHelper.startSetup(new
               IabHelper.OnIabSetupFinishedListener() {
                   @Override
                   public void onIabSetupFinished(IabResult result) {
                       if (!result.isSuccess()) {
                          //Log.d("BJJ","in app billing setup failed: " + result);
                           successfulSetupToIAP = false;
                       }
                       else {
                           //Log.d("BJJ","in app billing setup worked!!!!");
                           successfulSetupToIAP = true;
                       }
                   }
               }
        );

        //initiliaze mopub ad stuff
        moPubView = (MoPubView) findViewById(R.id.bannerLayout_help);
        moPubView.setAdUnitId(getString(R.string.mopub_ad_id)); // Enter your Ad Unit ID from www.mopub.com
        moPubView.loadAd();
        helpActivityAdLayout = (LinearLayout) findViewById(R.id.helpActivityAdLinearLayout);
        noAdsButton = (ImageView) findViewById(R.id.noads);

        //load internal data for hasAds boolean
        checkHasAds = new InternalDataForAdBoolean(this);

        //add ga tracker
        ((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);

        ImageView iv = (ImageView) findViewById(R.id.buttonback);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help, menu);
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

    //Define what happens when no ads button is selected
    public void removeAds(View view){
        if (successfulSetupToIAP) {
            mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001, mPurchaseFinishedListener, "mypurchasetoken");
        }
        else {
            Toast.makeText(this, "You need to be connected to the internet and/or logged into Google Play to purchase.",
                    Toast.LENGTH_LONG).show();
        }

//        //Uncomment following lines to consume
//        if (mService==null) Log.d("BJJ","mservice is null");
//        else Log.d("BJJ","mservice is not null");
//
//        String purchasetoken = "inapp:" + getPackageName() + ITEM_SKU;
//        Log.d("BJJ",purchasetoken);
//        try {
//            int repsonse = mService.consumePurchase(3,getPackageName(),purchasetoken);
//            if (repsonse==0) {
//                Log.d("BJJ","Consumption successful");
//            }
//            else {
//                Log.d("BJJ","Consumption unsuccessful");
//            }
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    // This method is called by the mPurchaseFinishedListener after successful purchase
    public void removeAdsAfterSuccessPurchase(View view) {
        hasAds_FromInternalStorage = checkHasAds.checkAdState();
        if (!hasAds || !hasAds_FromInternalStorage) {
            noAdsButton.setVisibility(View.GONE);
            helpActivityAdLayout.setVisibility(View.GONE);
            View adsLayout = (View) findViewById(R.id.bannerLayout_help);
            helpActivityAdLayout.removeView(adsLayout);
        }
    }

    //onactivity result for in app purchase
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode,resultCode,data)) {
            super.onActivityResult(requestCode,resultCode,data);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("BJJ","onstart method called");
        //check for ads and remove ads if passes check
        removeAdsAfterSuccessPurchase(findViewById(android.R.id.content)); //has internal check for both internal flag and
                                                                           //flag set by google play getpurchases
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        moPubView.destroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;

        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

}
