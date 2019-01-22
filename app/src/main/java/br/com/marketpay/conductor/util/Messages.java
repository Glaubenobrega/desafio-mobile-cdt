package br.com.marketpay.conductor.util;

import android.content.Context;
import android.widget.Toast;

public class Messages {

    public static void toastDefault(String message, Context context){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
