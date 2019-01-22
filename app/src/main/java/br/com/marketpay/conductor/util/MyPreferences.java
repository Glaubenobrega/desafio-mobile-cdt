package br.com.marketpay.conductor.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import br.com.marketpay.conductor.model.Profile;

public class MyPreferences {

    /**
     * Salva os dados do profile nas preferências no formato JSON
     */
    public static void setProfile(Profile profile, Context context){
        SharedPreferences pref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(profile);
        prefEditor.putString(Constants.PREF_KEY_PROFILE, json);
        prefEditor.apply();
    }

    /**
     * Recupera um objeto profile das preferências
     */
    public static Profile getProfile(Context context){
        SharedPreferences pref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString(Constants.PREF_KEY_PROFILE, null);
        return gson.fromJson(json, Profile.class);
    }
}
