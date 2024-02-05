package com.example.code_binder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends ArrayAdapter<String> {

    private Context context;
    private TextView id_tv;
    private TextView firstPart_tv;
    private TextView secondPart_tv;
    private TextView thirdPart_tv;
    private List<String> dataFromCodes;
    private CodeDataSource codeDataSource;
    private int numberOfCodes;

    public ListViewAdapter(Context context) {
        super(context, R.layout.list_item);
        this.context = context;
        this.dataFromCodes = new ArrayList<>();
        this.codeDataSource = new CodeDataSource(context);
    }

    public boolean isScanned(String code) {
        if (dataFromCodes.contains(code))
            return true;
        else
            return false;
    }

    @Override
    public int getCount() {
        return dataFromCodes.size();
    }

    @Override
    public String getItem(int position) {
        return dataFromCodes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);

        id_tv = rowView.findViewById(R.id.id_tv);
        firstPart_tv = rowView.findViewById(R.id.firstPart_tv);
        secondPart_tv = rowView.findViewById(R.id.secondPart_tv);
        thirdPart_tv = rowView.findViewById(R.id.thirdPart_tv);

        String text = dataFromCodes.get(position);
        //String[] str = dataCutter(text);

        id_tv.setText(Integer.toString(position + 1));
        firstPart_tv.setText(text);
        secondPart_tv.setText("");
        thirdPart_tv.setText("");

        return rowView;
    }

    public int getNumberOfCodes() {
        return numberOfCodes;
    }

    public void setNumberOfCodes(int numberOfCodes) {
        this.numberOfCodes = numberOfCodes;
    }

    public void addCode(String codeData) {
        dataFromCodes.add(codeData);
        notifyDataSetChanged();
    }

    public String[] dataCutter(String data) {
        String[] dataParts = new String[3];

        char[] buffer = new char[14];
        data.getChars(3, 17, buffer, 0);
        dataParts[0] = String.copyValueOf(buffer);

        buffer = new char[8];
        data.getChars(19, 27, buffer, 0);
        dataParts[1] = String.copyValueOf(buffer);

        buffer = new char[4];
        data.getChars(30, 34, buffer, 0);
        dataParts[2] = String.copyValueOf(buffer);

        return dataParts;
    }
}