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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.marketpay.conductor.R;
import br.com.marketpay.conductor.adpater.ExtratoRecyclerAdapter;
import br.com.marketpay.conductor.api.APIClient;
import br.com.marketpay.conductor.api.ApiEndpoint;
import br.com.marketpay.conductor.model.CardUsage;
import br.com.marketpay.conductor.model.Purchase;
import br.com.marketpay.conductor.model.Purchases;
import br.com.marketpay.conductor.util.CheckConnection;
import br.com.marketpay.conductor.util.Constants;
import br.com.marketpay.conductor.util.Format;
import br.com.marketpay.conductor.util.Messages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class GraficoFragment extends Fragment implements OnChartGestureListener{

    private MaterialDialog dialog;
    private RecyclerView mRecyclerView;
    private ExtratoRecyclerAdapter recyclerAdapter;
    private BottomNavigationView bottomNavigationView;
    private Purchases purchases;
    private BarChart mChart;
    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;
    private List<CardUsage> cardUsages = new ArrayList<>();
    private int pagina = 1;
    private String mes = "";
    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private RecyclerView.LayoutManager layout;
    List<Purchase> lista = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_grafico, container, false);

        mChart = new BarChart(getActivity());
        findViewById(view);

        Calendar calendar = Calendar.getInstance();
        mes = String.valueOf(calendar.get(Calendar.MONTH)+1);

        mRecyclerView = view.findViewById(R.id.recyclerView_grafico);
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

                if (response.code() != 200){
                    loading = false;
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(dialog != null){
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

                    Log.i(Constants.LOG_TAG,"statuscode: " + response.code());
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

                                    if (lista.size() > 0){
                                        getDados();
                                    }
                                    if(dialog != null){
                                        dialog.dismiss();
                                    }
                                    Messages.toastDefault("Mês: "+mes+" Página: "+pagina, getActivity());
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

    private void getDados() {

        Retrofit retrofit =  APIClient.getConnection();
        ApiEndpoint apiService = retrofit.create(ApiEndpoint.class);
        Call<List<CardUsage>> call = apiService.getUsoCartao();

        call.enqueue(new Callback<List<CardUsage>>() {
            @Override
            public void onResponse(Call<List<CardUsage>> call, Response<List<CardUsage>> response) {

                if (response.code() != 200){

                    Messages.toastDefault(getResources().getString((R.string.erro_compras)), getActivity());

                } else {

                    cardUsages = response.body();
                    setGrafico();
                    Log.i(Constants.LOG_TAG,"statuscode: " + response.code());
                    Log.i(Constants.LOG_TAG, cardUsages.toString());
                }
            }
            @Override
            public void onFailure(Call<List<CardUsage>> call, Throwable t) {
                Log.e(Constants.LOG_TAG, t.toString());
            }
        });

    }

    private void setGrafico() {

        ArrayList<IBarDataSet> sets = new ArrayList<IBarDataSet>();
        CardUsage cardUsage;
        for (int i = 0; i <cardUsages.size(); i++ ){
            ArrayList<BarEntry> entries = new ArrayList<>();
            cardUsage = cardUsages.get(i);
            entries.add(new BarEntry(i, Float.parseFloat(cardUsage.getValue())));
            BarDataSet barDataSet = new BarDataSet(entries, getLabel(i));
            barDataSet.setColors(getColor(i));
            sets.add(barDataSet);
        }

        BarData barData = new BarData(sets);
        mChart.setOnChartGestureListener(this);
        mChart.setData(barData);
        mChart.setFitBars(true);
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        mChart.getAxisRight().setEnabled(false);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(false);
        mChart.getDescription().setEnabled(false);
        mChart.setMaxVisibleValueCount(12);
        mChart.setPinchZoom(true);
        mChart.setDrawBarShadow(false);
        mChart.setDrawGridBackground(false);
        mChart.getAxisLeft().setDrawGridLines(true);
        mChart.animateY(2500);
        mChart.getLegend().setEnabled(true);
        mChart.getDescription().setEnabled(false);
        mChart.setFitBars(true);
        mChart.invalidate();
    }

    private void findViewById(View view) {
        mChart = view.findViewById(R.id.barChart);
    }

    private int[] colors = new int[] {
            ColorTemplate.rgb("#98FB98"),
            ColorTemplate.rgb("#F0E68C"),
            ColorTemplate.rgb("#87CEFA"),
            ColorTemplate.rgb("#FFB6C1"),
            ColorTemplate.rgb("#D3D3D3"),
            ColorTemplate.rgb("#98FB98"),
            ColorTemplate.rgb("#F0E68C"),
            ColorTemplate.rgb("#87CEFA"),
            ColorTemplate.rgb("#FFB6C1"),
            ColorTemplate.rgb("#D3D3D3"),
            ColorTemplate.rgb("#98FB98"),
            ColorTemplate.rgb("#F0E68C"),
    };

    private String[] mLabels = new String[] {
            "Oscar Calcados",
            "Steam store",
            "Brilha brilha",
            "Pernambucanas",
            "Souza Store",
            "Vinhas Enterprises",
            "Glovo",
            "Azul",
            "Odebrecht",
            "Ricardo Eletro",
            "Tim",
            "JBS"
    };

    private int getColor(int i) {
        return colors[i];
    }

    private String getLabel(int i) {
        return mLabels[i];
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) { }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        mChart.highlightValues(null);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) { }

    @Override
    public void onChartDoubleTapped(MotionEvent me) { }

    @Override
    public void onChartSingleTapped(MotionEvent me) { }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) { }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) { }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) { }
}
