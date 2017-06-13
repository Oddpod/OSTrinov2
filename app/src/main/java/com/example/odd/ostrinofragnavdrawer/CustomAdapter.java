package com.example.odd.ostrinofragnavdrawer;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.odd.ostrinofragnavdrawer.Listeners.PlayerListener;
import com.example.odd.ostrinofragnavdrawer.Listeners.QueueListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;

public class CustomAdapter extends BaseAdapter implements PlayerListener {

    private List<Ost> filteredOstList, ostList;
    private Stack<Ost> played;
    private Context mContext;

    private ImageButton btnOptions;
    private QueueListener queueListener;
    private int nowPlaying = -1;
    private boolean queue;

    public CustomAdapter(Context context, List<Ost> ostListin, QueueListener ql, boolean queue) {
        mContext = context;
        ostList = new ArrayList<>();
        ostList.addAll(ostListin);
        filteredOstList = new ArrayList<>();
        filteredOstList.addAll(ostListin);
        queueListener = ql;
        if(queue){
            played = new Stack<>();
            this.queue = queue;
        }
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
        ImageView thumbnail = (ImageView) v.findViewById(R.id.ivThumbnail);
        File tnFile = new File(Environment.getExternalStorageDirectory()
                + "/OSTthumbnails/" + Util.urlToId(ost.getUrl()) + ".jpg");
        Picasso.with(mContext)
                .load(tnFile)
                .placeholder(R.drawable.tranquility)
                .into(thumbnail);
        TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        TextView tvShow = (TextView) v.findViewById(R.id.tvShow);
        TextView tvTags = (TextView) v.findViewById(R.id.tvTags);
        btnOptions = (ImageButton) v.findViewById(R.id.btnOptions);

        if (nowPlaying == position) {
            System.out.println(nowPlaying + ", " + position);
            v.setBackgroundResource(R.drawable.greenrect);
        }
        if(queue && position == 0){
            v.setBackgroundResource(R.drawable.greenrect);
        }
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queueListener.addToQueue(position);
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
        if(!queue){
            nowPlaying = newId;
        }
    }

    @Override
    public void next() {
        played.add(ostList.remove(0));
        notifyDataSetChanged();
    }

    @Override
    public void previous() {
        ostList.add(0, played.pop());
        notifyDataSetChanged();
    }

    @Override
    public void shuffle(List<Ost> updatedList) {
        ostList = updatedList;
        notifyDataSetChanged();
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

    public void updateList(List<Ost> updatedList){
        ostList = updatedList;
        notifyDataSetChanged();
        System.out.println(ostList.toString());
    }

    public void removeFromQueue(int id){
        ostList.remove(id);
        notifyDataSetChanged();
    }

    public void addToQueue(Ost ost){
        ostList.add(1, ost);
        notifyDataSetChanged();
    }
}
