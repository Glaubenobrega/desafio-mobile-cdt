package br.com.marketpay.conductor.view;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import br.com.marketpay.conductor.R;
import br.com.marketpay.conductor.api.APIClient;
import br.com.marketpay.conductor.api.ApiEndpoint;
import br.com.marketpay.conductor.model.Profile;
import br.com.marketpay.conductor.util.CheckConnection;
import br.com.marketpay.conductor.util.Constants;
import br.com.marketpay.conductor.util.Format;
import br.com.marketpay.conductor.util.Messages;
import br.com.marketpay.conductor.util.MyPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ProfileActivity extends AppCompatActivity {

    private TextView name;
    private TextView cardNumber;
    private TextView expirationDate;
    private Profile profile = null;
    private MaterialDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getFindViewById();

        profile = MyPreferences.getProfile(this);

        if (profile == null) {

            int[] type = {ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI};
            if (!CheckConnection.isConnected(this, type)) {
                Messages.toastDefault(getResources().getString((R.string.sem_conexao)), this);
                return;
            }

            Retrofit retrofit =  APIClient.getConnection();
            ApiEndpoint apiService = retrofit.create(ApiEndpoint.class);
            Call<Profile> call = apiService.getPortador();

            dialog = new MaterialDialog.Builder(this)
                    .content(R.string.carregando)
                    .cancelable(false)
                    .progress(true, 0)
                    .show();

            call.enqueue(new Callback<Profile>() {
                @Override
                public void onResponse(Call<Profile> call, Response<Profile> response) {


                    if (response.code() != 200) {
                        Messages.toastDefault(getResources().getString((R.string.erro_portador)), ProfileActivity.this);
                    } else {

                        profile = response.body();

                        Log.i(Constants.LOG_TAG, "statuscode: " + response.code());
                        Log.i(Constants.LOG_TAG, profile.toString());

                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (profile != null) {
                                            setView(profile);
                                        }

                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            }
                        }.start();
                    }
                }
                @Override
                public void onFailure(Call<Profile> call, Throwable t) {
                    Log.e(Constants.LOG_TAG, t.toString());
                }
            });
        } else {

            if(profile != null){
                setView(profile);
            }
        }
    }

    private void getFindViewById(){
        name = findViewById(R.id.textView_profile_name);
        cardNumber = findViewById(R.id.textView_profile_cardNumber);
        expirationDate = findViewById(R.id.textView_profile_expirationDate);
    }

    private void setView(Profile profile){
        name.setText(profile.getName() != null && !("").equals(profile.getName()) ? profile.getName() : "");
        cardNumber.setText(profile.getCardNumber() != null && !("").equals(profile.getCardNumber()) ? Format.getMascaraCartao(profile.getCardNumber()) : "");
        expirationDate.setText(profile.getExpirationDate() != null && !("").equals(profile.getExpirationDate()) ? Format.converteDate(profile.getExpirationDate().toString()) : "");
    }
}
