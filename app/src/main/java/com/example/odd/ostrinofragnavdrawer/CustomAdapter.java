package com.example.odd.ostrinofragnavdrawer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomAdapter extends BaseAdapter implements PlayerListener{

    private List<Ost> filteredOstList;
    private List<Ost> ostList;
    //private ItemFilter mFilter = new ItemFilter();
    private Context mContext;

    private ImageButton btnOptions;
    private QueueListener queueListener;
    private int nowPlaying = -1;

    public CustomAdapter(Context context, List<Ost> ostListin, QueueListener ql) {
        mContext = context;
        ostList = new ArrayList<>();
        ostList.addAll(ostListin);
        filteredOstList = new ArrayList<>();
        filteredOstList.addAll(ostListin);
        queueListener = ql;
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
        return position; //ostStringList.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Ost ost = ostList.get(position);
        View v = View.inflate(mContext, R.layout.custom_row, null);
        TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        TextView tvShow = (TextView) v.findViewById(R.id.tvShow);
        TextView tvTags = (TextView) v.findViewById(R.id.tvTags);
        btnOptions = (ImageButton) v.findViewById(R.id.btnOptions);

        if (nowPlaying == position) {
            System.out.println(nowPlaying + ", " + position);
            v.setBackgroundResource(R.drawable.greenrect);
        }
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queueListener.addToQueue(position);
                System.out.println("Duuuuuuuuuuuuuuude");
            }
        });


        tvTitle.setText(ost.getTitle());
        tvShow.setText((ost.getShow()));
        tvTags.setText(ost.getTags());

        v.setTag(ost.getId());
        return v;
    }

    @Override
    public void updateCurrentlyPlaying(int newId) {
        nowPlaying = newId;
    }
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        ostList.clear();
        if (charText.length() == 0) {
            ostList.addAll(filteredOstList);
        }
        else
        {
            for (Ost ost : filteredOstList) {
                if (ost.getSearchString().toLowerCase(Locale.getDefault()).contains(charText)) {
                    ostList.add(ost);
                }
            }
        }
        notifyDataSetChanged();
    }
}
