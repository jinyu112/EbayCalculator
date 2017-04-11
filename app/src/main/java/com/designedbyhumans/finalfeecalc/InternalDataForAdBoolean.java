package com.designedbyhumans.finalfeecalc;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class InternalDataForAdBoolean {

    private Context context;
    String filenameInit = "AdsSettings";

    public InternalDataForAdBoolean(Context in) {
        this.context = in;
    }

    //use internal storage to save the training time data
    public void initHasAdsData() {
        FileOutputStream outputStream;

        //if the file exists, do nothing
        if (fileExistence()) {
            //do nothing

        }
        else { //this should be the first time the class is defined and initialized so create the file and set the value to be true
            try {
                outputStream = context.openFileOutput(filenameInit, context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                oos.writeObject("true"); // setting ads to be true
                oos.close();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean fileExistence(){
        File file = context.getFileStreamPath(filenameInit);
        return file.exists();
    }


    //this function returns whether or not ads are present
    public boolean checkAdState() {
        String hasAdsString = "true";
        boolean hasAds = true;
        try {
            FileInputStream fin = context.openFileInput(filenameInit);
            ObjectInputStream ois = new ObjectInputStream(fin);
            hasAdsString = (String) ois.readObject();
            ois.close();
            fin.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (hasAdsString.equals("true")) {
            hasAds = true;
        }
        else hasAds = false;
        return hasAds;
    }

    public boolean removeAds_InternalStorage() {
        String hasAdsString = "false";
        boolean success = false;
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(filenameInit, context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(hasAdsString); //write false
            oos.close();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

}
