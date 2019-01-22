package br.com.marketpay.conductor.api;

import java.util.List;

import br.com.marketpay.conductor.model.CardUsage;
import br.com.marketpay.conductor.model.Profile;
import br.com.marketpay.conductor.model.Purchases;
import br.com.marketpay.conductor.model.Resume;
import br.com.marketpay.conductor.util.Routes;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiEndpoint {

    @GET(Routes.PROFILE)
    @Headers("Content-Type: application/json")
    Call<Profile> getPortador();

    @GET(Routes.RESUMO)
    @Headers("Content-Type: application/json")
    Call<Resume> getSaldo();

    @GET(Routes.EXTRATO)
    @Headers("Content-Type: application/json")
    Call<Purchases> getExtrato(
            @Query("month") String month,
            @Query("year") String year,
            @Query("page") int page);

    @GET(Routes.USO_CARTAO)
    @Headers("Content-Type: application/json")
    Call<List<CardUsage>> getUsoCartao();

}
