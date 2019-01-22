package br.com.marketpay.conductor.util;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckConnection {

        public static boolean isConnected(Context context, int[] typeNetworks) {
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
                for (int typeNetwork : typeNetworks) {
                    NetworkInfo networkInfo = connectivityManager.getNetworkInfo(typeNetwork);
                    if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            } catch (Exception e) {
                return false;
            }
            return false;
        }

}
