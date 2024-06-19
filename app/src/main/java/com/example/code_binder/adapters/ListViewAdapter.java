package com.example.code_binder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.code_binder.Product;
import com.example.code_binder.R;

import java.util.List;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> {
    private List<Product> productList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView info_tv;
        public TextView counter_tv;

        public ViewHolder(View view) {
            super(view);
            info_tv = view.findViewById(R.id.tv_info);
            counter_tv = view.findViewById(R.id.tv_counter);
        }
    }

    public ListViewAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Product item = productList.get(position);
        holder.info_tv.setText(item.getTitle());

        int alreadyScanned = item.getAlreadyScanned();
        holder.counter_tv.setText(alreadyScanned + "/" + item.getCount());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}