package br.com.marketpay.conductor.view;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import br.com.marketpay.conductor.R;
import br.com.marketpay.conductor.api.APIClient;
import br.com.marketpay.conductor.api.ApiEndpoint;
import br.com.marketpay.conductor.model.Profile;
import br.com.marketpay.conductor.util.CheckConnection;
import br.com.marketpay.conductor.util.Constants;
import br.com.marketpay.conductor.util.Messages;
import br.com.marketpay.conductor.util.MyPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SplashScreenActivity extends AppCompatActivity {

    private Profile profile = null;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = findViewById(R.id.progressBar);
        StartAnimations();
    }

    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        LinearLayout layout = findViewById(R.id.layout);
        if (layout != null) {
            layout.clearAnimation();
            layout.startAnimation(anim);
        }
        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageView imageView = findViewById(R.id.iconApp);
        if (imageView != null) {
            imageView.clearAnimation();
            imageView.startAnimation(anim);
        }

        int SPLASH_DISPLAY_LENGTH = 3500;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                progressBar.setVisibility(View.VISIBLE);

                int[] type = {ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI};
                if (!CheckConnection.isConnected(SplashScreenActivity.this, type)) {
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    SplashScreenActivity.this.finish();
                }

                Retrofit retrofit =  APIClient.getConnection();
                ApiEndpoint apiService = retrofit.create(ApiEndpoint.class);
                Call<Profile> call = apiService.getPortador();

                call.enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {

                        if (response.code() != 200) {

                            progressBar.setVisibility(View.GONE);
                            Messages.toastDefault(getResources().getString((R.string.erro_portador)), SplashScreenActivity.this);
                            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                            SplashScreenActivity.this.finish();

                        } else {

                            profile = response.body();

                            if (profile != null) {
                                MyPreferences.setProfile(profile, SplashScreenActivity.this);
                            }

                            Log.i(Constants.LOG_TAG, "statuscode: " + response.code());
                            Log.i(Constants.LOG_TAG, profile.toString());

                            progressBar.setVisibility(View.GONE);

                            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                            SplashScreenActivity.this.finish();
                        }
                    }
                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        Log.e(Constants.LOG_TAG, t.toString());
                    }
                });


            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
