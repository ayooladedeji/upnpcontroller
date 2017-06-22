package com.cambridgeaudio.upnpcontroller.dialogs;

/**
 * Created by Ayo on 22/06/2017.
 */


import android.app.ProgressDialog;
import android.content.Context;


public class LoadingDialog {

    public static ProgressDialog create(Context context, String title, String message, boolean cancelable) {

        ProgressDialog loadingDialog = new ProgressDialog(context);
        if (title != null)
            loadingDialog.setTitle(title);
        if (message != null)
            loadingDialog.setMessage(message);
        loadingDialog.setCancelable(cancelable);

        return loadingDialog;
    }



}