package br.com.marketpay.conductor.fragment;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.marketpay.conductor.R;
import br.com.marketpay.conductor.adpater.ExtratoRecyclerAdapter;
import br.com.marketpay.conductor.api.APIClient;
import br.com.marketpay.conductor.api.ApiEndpoint;
import br.com.marketpay.conductor.model.Profile;
import br.com.marketpay.conductor.model.Purchase;
import br.com.marketpay.conductor.model.Purchases;
import br.com.marketpay.conductor.model.Resume;
import br.com.marketpay.conductor.util.CheckConnection;
import br.com.marketpay.conductor.util.Constants;
import br.com.marketpay.conductor.util.Format;
import br.com.marketpay.conductor.util.Messages;
import br.com.marketpay.conductor.util.MyPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class ExtratoFragment extends Fragment {

    private MaterialDialog dialog;
    private RecyclerView mRecyclerView;
    private ExtratoRecyclerAdapter recyclerAdapter;
    private BottomNavigationView bottomNavigationView;
    private Purchases purchases;
    private Resume saldo;
    private Profile profile = null;
    private TextView textViewCartao;
    private TextView textViewSaldoDisponivel;
    private int pagina = 1;
    private String mes = "";
    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private RecyclerView.LayoutManager layout;
    List<Purchase> lista = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_extrato, container, false);

        findViewById(view);

        profile = MyPreferences.getProfile(getActivity());

        Calendar calendar = Calendar.getInstance();
        mes = String.valueOf(calendar.get(Calendar.MONTH)+1);

        if (profile != null){
            getSaldo();
        }

        mRecyclerView = view.findViewById(R.id.recyclerView_extrato);
        layout = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        setHasOptionsMenu(true);

        bottomNavigationView = getActivity().findViewById(R.id.navigation);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && bottomNavigationView.isShown()) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else if (dy < 0 ) {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        getPurchases(mes, pagina);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if(dy > 0){
                    visibleItemCount = layout.getChildCount();
                    totalItemCount = layout.getItemCount();
                    pastVisiblesItems = ((LinearLayoutManager)mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                    if (loading) {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            Log.i(Constants.LOG_TAG,"Chamada para nova página");
                            pagina = pagina+1;
                            getPurchases(mes, pagina);
                        }
                    }
                }
            }
        });

        return view;
    }

    private void getConnection() {
        int[] type = {ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI};
        if (!CheckConnection.isConnected(getActivity(), type)) {

            new Thread() {
                @Override
                public void run() {
                    super.run();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(dialog != null){
                                dialog.dismiss();
                                Messages.toastDefault(getResources().getString((R.string.sem_conexao)), getActivity());
                            }
                        }
                    });
                }
            }.start();

            return;
        }
    }

    private void getPurchases(final String mes, final int pagina) {

        getConnection();

        Log.i(Constants.LOG_TAG,"Mes: "+mes +" Página: "+pagina);

        Retrofit retrofit =  APIClient.getConnection();
        ApiEndpoint apiService = retrofit.create(ApiEndpoint.class);
        Call<Purchases> call = apiService.getExtrato(mes, Format.getAnoAtual(), pagina);

        dialog = new MaterialDialog.Builder(getActivity())
                .content(R.string.carregando)
                .cancelable(false)
                .progress(true, 0)
                .show();

        call.enqueue(new Callback<Purchases>() {
            @Override
            public void onResponse(Call<Purchases> call, Response<Purchases> response) {

                if (response.code() != 200) {
                    loading = false;
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                    Messages.toastDefault(getResources().getString((R.string.pagina_nao_encotrada)), getActivity());
                                }
                            });
                        }
                    }.start();
                } else {

                    loading = true;
                    purchases = response.body();
                    lista.addAll(purchases.getPurchases());

                    Log.i(Constants.LOG_TAG, "statuscode: " + response.code());
                    Log.i(Constants.LOG_TAG, purchases.toString());

                    recyclerAdapter = new ExtratoRecyclerAdapter(getActivity(), lista);
                    mRecyclerView.setAdapter(recyclerAdapter);

                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                    Messages.toastDefault("Mês: " + mes + " Página: " + pagina, getActivity());
                                }
                            });
                        }
                    }.start();
                }
            }
            @Override
            public void onFailure(Call<Purchases> call, Throwable t) {
                Log.e(Constants.LOG_TAG, t.toString());
            }
        });
    }

    private void getSaldo(){

        Retrofit retrofit =  APIClient.getConnection();
        ApiEndpoint apiService = retrofit.create(ApiEndpoint.class);
        Call<Resume> call = apiService.getSaldo();

        call.enqueue(new Callback<Resume>() {
            @Override
            public void onResponse(Call<Resume> call, Response<Resume> response) {

                if (response.code() != 200) {
                    Messages.toastDefault(getResources().getString((R.string.erro_saldo)), getActivity());
                } else {
                    saldo = response.body();
                    Log.i(Constants.LOG_TAG,"statuscode: " + response.code());
                    Log.i(Constants.LOG_TAG, saldo.toString());

                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(saldo != null){
                                        setView();
                                    }
                                }
                            });
                        }
                    }.start();
                }
            }
            @Override
            public void onFailure(Call<Resume> call, Throwable t) {
                Log.e(Constants.LOG_TAG, t.toString());
            }
        });
    }

    private void findViewById(View view) {
        textViewCartao = view.findViewById(R.id.textView_cartao);
        textViewSaldoDisponivel = view.findViewById(R.id.textView_valor_disponivel);
    }

    private void setView (){
        textViewCartao.setText(profile.getCardNumber() != null ? Format.getMascaraCartao(profile.getCardNumber()) : "");
        textViewSaldoDisponivel.setText(saldo.getBalance() != null ? Format.formatMoedaSimbolo(new BigDecimal(saldo.getBalance())) : "");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.nomeMes);
        item.setTitle(Format.getMes());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nomeMes:
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.mes)
                            .items(R.array.items)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    lista.clear();
                                    item.setTitle(text);
                                    loading = true;
                                    mes = String.valueOf(which+1);
                                    pagina = 1;
                                    getPurchases(mes, pagina);
                                }
                            })
                            .show();
                    return true;
                default:
                    return false;
        }
    }
}
