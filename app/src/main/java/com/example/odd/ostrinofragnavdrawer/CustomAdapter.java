package com.example.odd.ostrinofragnavdrawer;

import android.content.Context;
import android.media.Image;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.odd.ostrinofragnavdrawer.Listeners.PlayerListener;
import com.example.odd.ostrinofragnavdrawer.Listeners.QueueListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomAdapter extends BaseAdapter implements PlayerListener {

    private List<Ost> filteredOstList, ostList;
    private Context mContext;
    private LayoutInflater mInflater;
    private QueueListener queueListener;
    private int nowPlaying = -1;
    private File[] tnFiles;

    public CustomAdapter(Context context, List<Ost> ostListin, QueueListener ql) {
        mContext = context;
        ostList = new ArrayList<>();
        ostList.addAll(ostListin);
        filteredOstList = new ArrayList<>();
        filteredOstList.addAll(ostListin);
        queueListener = ql;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tnFiles = new File[ostListin.size()];
        for (int i = 0; i < ostList.size(); i++) {
            File tnFile = new File(Environment.getExternalStorageDirectory()
                    + "/OSTthumbnails/" + Util.urlToId(ostList.get(i).getUrl()) + ".jpg");
            tnFiles[i] = tnFile;
        }
    }

    @Override
    public int getCount() {
        return ostList.size();
    }

    @Override
    public Ost getItem(int position) {
        return ostList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position; //ostStringList.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Ost ost = ostList.get(position);
        View v = mInflater.inflate(R.layout.custom_row, null);
        ImageView thumbnail = (ImageView) v.findViewById(R.id.ivThumbnail);
        Picasso.with(mContext).load(tnFiles[position]).into(thumbnail);
        /*File tnFile = new File(Environment.getExternalStorageDirectory()
                + "/OSTthumbnails/" + Util.urlToId(ost.getUrl()) + ".jpg");
        Picasso.with(mContext)
                .load(tnFile)
                .into(thumbnail);*/
        TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        TextView tvShow = (TextView) v.findViewById(R.id.tvShow);
        TextView tvTags = (TextView) v.findViewById(R.id.tvTags);
        ImageButton btnOptions = (ImageButton) v.findViewById(R.id.btnOptions);

        if (nowPlaying == position) {
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

    private class ViewHolder{

    }

    @Override
    public void updateCurrentlyPlaying(int newId) {
        nowPlaying = newId;
    }

    @Override
    public void next() {
    }

    @Override
    public void previous() {
    }

    @Override
    public void shuffle(List<Ost> updatedList) {
    }

    void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        ostList.clear();
        if (charText.length() == 0) {
            ostList.addAll(filteredOstList);
        }
        else{
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

    public int getNowPlaying(){
        return nowPlaying;
    }
}
