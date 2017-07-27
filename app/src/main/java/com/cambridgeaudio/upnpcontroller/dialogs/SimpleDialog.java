package com.cambridgeaudio.upnpcontroller.dialogs;

/**
 * Created by Ayo on 22/06/2017.
 */


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import static android.content.DialogInterface.BUTTON_POSITIVE;


public class SimpleDialog {

    private static AlertDialog dialog;

    public static void show(Context context, String title, String message, boolean cancelable, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if(message != null)
            builder.setMessage(message);

        if(title != null)
            builder.setTitle(title);

        builder.setCancelable(cancelable);

        builder.setPositiveButton("OK", listener != null ? listener : (dialog, id) -> dialog.cancel());

        dialog = builder.create();
        dialog.show();
    }

    public static void dismiss() {
        if (dialog != null)
            dialog.dismiss();
    }






}