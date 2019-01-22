package br.com.marketpay.conductor.adpater;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.marketpay.conductor.R;
import br.com.marketpay.conductor.model.Purchase;
import br.com.marketpay.conductor.util.Format;

public class ExtratoRecyclerAdapter extends RecyclerView.Adapter<ExtratoRecyclerAdapter.ViewHolder> {

    private List<Purchase> purchases;
    private Context context;

    public ExtratoRecyclerAdapter(Context context, List<Purchase> purchases) {
        this.purchases = new ArrayList<>(purchases);
        this.context = context;
    }

    @Override
    public ExtratoRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_purchases, parent, false);
        return new ExtratoRecyclerAdapter.ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ExtratoRecyclerAdapter.ViewHolder holder, final int position) {
        Purchase purchase = purchases.get(position);
        holder.date.setText(Format.formatDate(purchase.getDate()));
        holder.store.setText(purchase.getStore());
        holder.description.setText(purchase.getDescription());
        holder.value.setText(Format.formatMoeda(new BigDecimal(purchase.getValue())));

        holder.description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return purchases.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView store;
        TextView description;
        TextView value;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            date = itemLayoutView.findViewById(R.id.textView_date);
            store = itemLayoutView.findViewById(R.id.textView_strore);
            description = itemLayoutView.findViewById(R.id.textView_description);
            value = itemLayoutView.findViewById(R.id.textView_value);
        }
    }

}
