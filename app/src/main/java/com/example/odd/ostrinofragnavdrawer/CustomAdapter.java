package com.example.odd.ostrinofragnavdrawer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CustomAdapter extends BaseAdapter{

    private Context mContext;
    private ImageButton btnOptions;
    private List<Ost> ostList;
    private int clickedPos;

    public CustomAdapter(Context context, List<Ost> ostList){
        mContext = context;
        this.ostList = ostList;
    }

    @Override
    public int getCount() {
        return ostList.size();
    }

    @Override
    public Object getItem(int position) {
        return ostList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position; //ostList.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Ost ost = ostList.get(position);
        View v = View.inflate(mContext, R.layout.custom_row, null);
        TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        TextView tvShow = (TextView) v.findViewById(R.id.tvShow);
        TextView tvTags = (TextView) v.findViewById(R.id.tvTags);
        btnOptions = (ImageButton) v.findViewById(R.id.btnOptions);

        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedPos = position;
                System.out.println("Duuuuuuuuuuuuuuude");
            }
        });

        tvTitle.setText(ost.getTitle());
        tvShow.setText((ost.getShow()));
        tvTags.setText(ost.getTags());

        v.setTag(ost.getId());
        return v;
    }

    public int getClickedPos() {
        return clickedPos;
    }
}
