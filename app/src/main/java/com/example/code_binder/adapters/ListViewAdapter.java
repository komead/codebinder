package com.example.code_binder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.code_binder.Product;
import com.example.code_binder.R;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends ArrayAdapter<String> {

    private Context context;
    private TextView info_tv;
    private TextView gtin_tv;
    private List<Product> products;

    public ListViewAdapter(Context context) {
        super(context, R.layout.list_item);
        this.context = context;
        this.products = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public String getItem(int position) {
        //return products.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);

        info_tv = rowView.findViewById(R.id.tv_info);
        gtin_tv = rowView.findViewById(R.id.tv_gtin);

        info_tv.setText(products.get(position).getTitle());
        gtin_tv.setText(products.get(position).getGtin());

        return rowView;
    }

    public void addAllProducts(List<Product> list) {
        if (!list.isEmpty())
            products.addAll(list);
    }
}