package com.example.odd.ostrino;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.odd.ostrino.Listeners.PlayerListener;
import com.example.odd.ostrino.Listeners.QueueListener;
import com.example.odd.ostrinofragnavdrawer.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class CustomAdapter extends BaseAdapter implements PlayerListener {

    private List<Ost> filteredOstList, ostList;
    private Context mContext;
    private LayoutInflater mInflater;
    private QueueListener queueListener;
    private int nowPlaying = -1;

    CustomAdapter(Context context, List<Ost> ostListin, QueueListener ql) {
        mContext = context;
        ostList = new ArrayList<>();
        ostList.addAll(ostListin);
        filteredOstList = new ArrayList<>();
        filteredOstList.addAll(ostListin);
        queueListener = ql;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Ost ost = getItem(position);
        ViewHolder holder;
        if( convertView == null){
            convertView = mInflater.inflate(R.layout.custom_row, null);
            holder = new ViewHolder();
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.tvShow = (TextView) convertView.findViewById(R.id.tvShow);
            holder.tvTags = (TextView) convertView.findViewById(R.id.tvTags);
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.ivThumbnail);
            holder.btnOptions = (ImageButton) convertView.findViewById(R.id.btnOptions);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }
        File tnFile = new File(Environment.getExternalStorageDirectory()
                + "/OSTthumbnails/" + UtilMeths.urlToId(ost.getUrl()) + ".jpg");
        Picasso.with(mContext)
                .load(tnFile)
                .into(holder.thumbnail);

        if (nowPlaying == position) {
            convertView.setBackgroundResource(R.drawable.greenrect);
        } else{
            convertView.setBackgroundResource(R.drawable.white);
        }
        holder.btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queueListener.addToQueue(position);
            }
        });

        holder.tvTitle.setText(ost.getTitle());
        holder.tvShow.setText((ost.getShow()));
        holder.tvTags.setText(ost.getTags());

        return convertView;
    }

    private class ViewHolder{
        TextView tvTitle, tvShow, tvTags;
        ImageButton btnOptions;
        ImageView thumbnail;
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

    void updateList(List<Ost> updatedList){
        ostList = updatedList;
        notifyDataSetChanged();
    }

    int getNowPlaying(){
        return nowPlaying;
    }
}
