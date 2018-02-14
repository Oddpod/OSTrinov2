package com.odd.ostrinov2.fragmentsLogic;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.odd.ostrinov2.Constants;
import com.odd.ostrinov2.Ost;
import com.odd.ostrinov2.R;
import com.odd.ostrinov2.listeners.PlayerListener;
import com.odd.ostrinov2.services.YTplayerService;
import com.odd.ostrinov2.tools.UtilMeths;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

class CustomAdapter extends BaseAdapter implements PlayerListener{

    private List<Ost> filteredOstList, ostList;
    private Context mContext;
    private int prevSortedMode = 0;
    private LayoutInflater mInflater;
    private int nowPlaying = -1;
    private String lastQuery = "";

    CustomAdapter(Context context, List<Ost> ostListin) {
        mContext = context;
        ostList = new ArrayList<>();
        ostList.addAll(ostListin);
        filteredOstList = new ArrayList<>();
        filteredOstList.addAll(ostListin);
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
        return ostList.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Ost ost = getItem(position);
        ViewHolder holder;
        if( convertView == null){
            convertView = mInflater.inflate(R.layout.custom_row, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }
        File tnFile = UtilMeths.INSTANCE.getThumbnailLocal(ost.getUrl());
        Picasso.with(mContext)
                .load(tnFile)
                .into(holder.thumbnail);

        if (nowPlaying == ost.getId()) {
            convertView.setBackgroundResource(R.drawable.greenrect);
        } else{
            convertView.setBackgroundResource(R.drawable.white);
        }
        holder.btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, YTplayerService.class);
                intent.putExtra("ost_extra", ost);
                intent.setAction(Constants.ADD_OST_TO_QUEUE);
                mContext.startService(intent);
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

        ViewHolder(View convertView){
            tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            tvShow = (TextView) convertView.findViewById(R.id.tvShow);
            tvTags = (TextView) convertView.findViewById(R.id.tvTags);
            thumbnail = (ImageView) convertView.findViewById(R.id.ivThumbnail);
            btnOptions = (ImageButton) convertView.findViewById(R.id.btnOptions);
        }
    }

    @Override
    public void updateCurrentlyPlaying(int newId) {
        nowPlaying = newId;
        notifyDataSetChanged();
    }

    void filter(String charText) {
        lastQuery = charText.toLowerCase(Locale.getDefault());
        ostList.clear();
        if (lastQuery.length() == 0) {
            ostList.addAll(filteredOstList);
        }
        else{
            for (Ost ost : filteredOstList) {
                if (ost.getSearchString().toLowerCase(Locale.getDefault()).contains(lastQuery)) {
                    ostList.add(ost);
                }
            }
        }
        notifyDataSetChanged();
    }

    void sort(int mode){
        if(prevSortedMode == mode){
            unSort();
            prevSortedMode = 0;
            return;
        }
        prevSortedMode = mode;
        sortInternal(mode);
    }

    private void sortInternal(int mode){
        if(mode == 0){
            return;
        }
        switch (mode){
            case 1:{
                if (ostList.size() > 0 ){
                    Collections.sort(ostList, new Comparator<Ost>() {
                        @Override
                        public int compare(final Ost ost1, final Ost ost2) {
                            return ost1.getTitle().compareTo(ost2.getTitle());
                        }
                    });
                }
                notifyDataSetChanged();
                break;
            }
            default: break;
        }
    }

    private void unSort(){
        ostList.clear();
        filter(lastQuery);
    }

    void updateList(List<Ost> updatedList){
        filteredOstList.clear();
        filteredOstList.addAll(updatedList);
        filter(lastQuery);
        sortInternal(prevSortedMode);
    }

    int getNowPlaying(){
        return nowPlaying;
    }

    List<Ost> getOstList(){
        return ostList;
    }
    public void removeOst(int pos){
        Ost item = getItem(pos);
        ostList.remove(pos);
        filteredOstList.remove(item);
    }

    Ost getNowPlayingOst(){
        return  ostList.get(nowPlaying);
    }
}
