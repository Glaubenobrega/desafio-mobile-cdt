package br.com.marketpay.conductor.api;

import br.com.marketpay.conductor.util.Routes;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private final Retrofit retrofit = null;

    public static Retrofit getConnection() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Routes.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

}
