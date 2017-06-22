package com.cambridgeaudio.upnpcontroller.dialogs;

/**
 * Created by Ayo on 22/06/2017.
 */


import android.app.ProgressDialog;
import android.content.Context;


public class LoadingDialog {

    private static ProgressDialog dialog;

    public static void show(Context context, String title, String message, boolean cancelable) {

        dialog = new ProgressDialog(context);
        if (title != null)
            dialog.setTitle(title);
        if (message != null)
            dialog.setMessage(message);
        dialog.setCancelable(cancelable);

        dialog.show();
    }

    public static void dismiss() {
        if (dialog != null)
            dialog.dismiss();
    }


}