package com.odd.ostrinov2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.odd.ostrinov2.Listeners.PlayerListener;
import com.odd.ostrinov2.Listeners.QueueListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewWrapper> implements PlayerListener {
        private Stack<Ost> played, preQueued, queued;
        private Context mContext;

        private QueueListener queueListener;
        private int queueAddPos;
        private OnItemClickListener onItemClickListener;
        private boolean emptyAdapter;
        private int currPlayingIndex;

    public QueueAdapter(){
        this.emptyAdapter = true;
    }

    QueueAdapter(Context context, QueueListener queueListener) {
            played = new Stack<>();
            preQueued = new Stack<>();
            queued = new Stack<>();
            this.mContext = context;
            this.queueListener = queueListener;
        }

        @Override
        public ViewWrapper onCreateViewHolder(ViewGroup viewGroup, int i) {
            // create a new view
            @SuppressLint("InflateParams") View itemLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.queue_row, null);
            // create ViewHolder
            return new ViewWrapper(itemLayoutView);
        }

        @Override
        public void onBindViewHolder(final ViewWrapper viewWrapper, final int position) {
            int queuedSize = queued.size();
            final int queuedInvPos = queuedSize - (1 + position);
            final int invPos = preQueued.size() - (1 + position - queuedSize);
            final Ost ost;
            if(position < queuedSize){
                ost = queued.get(queuedInvPos);
            } else{
                ost = preQueued.get(invPos);
            }
            viewWrapper.getTitle().setText(ost.getTitle());
            viewWrapper.getShow().setText(ost.getShow());
            viewWrapper.getTags().setText(ost.getTags());
            File tnFile = new File(Environment.getExternalStorageDirectory()
                    + "/OSTthumbnails/" + UtilMeths.INSTANCE.urlToId(ost.getUrl()) + ".jpg");
            Picasso.with(mContext)
                    .load(tnFile)
                    .into(viewWrapper.getThumbnail());
            viewWrapper.getBtnOptions().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ost.equals(queued.get(queuedInvPos))){
                        queued.remove(queuedInvPos);
                    } else{
                        preQueued.remove(invPos);
                    }
                    queueListener.removeFromQueue(ost.getUrl());
                    notifyDataSetChanged();
                }
            });
        }

        void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
    }
        @Override
        public int getItemCount() {
            if(emptyAdapter){
                return 0;
            }else{
                return preQueued.size();
            }
        }

    @Override
    public void updateCurrentlyPlaying(int newId) {
        currPlayingIndex = newId;
    }

    @Override
    public void next() {
        if(!queued.isEmpty()){
            played.add(queued.pop());
            notifyDataSetChanged();
        }
        else if(!preQueued.isEmpty()){
            played.add(preQueued.pop());
            notifyDataSetChanged();
        }
        if(queueAddPos != 0){
            queueAddPos--;
        }
    }

    @Override
    public void previous() {
        if(!played.isEmpty()){
            preQueued.add(played.pop());
            notifyItemInserted(0);
            queueAddPos++;
        }

    }

    @Override
    public void shuffle(long seed) {
        Collections.shuffle(preQueued, new Random(seed));
        notifyDataSetChanged();
    }

    @Override
    public void unShuffle(List<Ost> unShuffledList) {
        preQueued = new Stack<>();
        for(int i = unShuffledList.size() - 1; i >= 0; i--) {
            if(i > currPlayingIndex){
                preQueued.add(unShuffledList.get(i));
            }else{
                played.add(unShuffledList.get(i));
            }
        }
    }

    class ViewWrapper extends RecyclerView.ViewHolder implements View.OnClickListener {
            final View base;
            TextView tvShow, tvTItle, tvTags;
            ImageView thumbnail;
            ImageButton btnOptions;

            ViewWrapper(View itemView) {
                super(itemView);
                base = itemView;
                base.setOnClickListener(this);
            }

            TextView getTitle() {
                if (tvTItle == null) {
                    tvTItle = (TextView) base.findViewById(R.id.tvTitle);
                }
                return tvTItle;
            }

            TextView getShow() {
                if (tvShow == null) {
                    tvShow = (TextView) base.findViewById(R.id.tvShow);
                }
                return (tvShow);
            }

            TextView getTags() {
                if (tvTags == null) {
                    tvTags = (TextView) base.findViewById(R.id.tvTags);
                }
                return (tvTags);
            }

            ImageView getThumbnail() {
                if (thumbnail == null) {
                    thumbnail = (ImageView) base.findViewById(R.id.ivThumbnail);

                }
                return thumbnail;
            }

            ImageButton getBtnOptions() {
                if (btnOptions == null) {
                    btnOptions = (ImageButton) base.findViewById(R.id.btnOptions);
                }
                return btnOptions;
            }

            @Override
            public void onClick(View v) {
                try {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, getAdapterPosition());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    void addToQueue(Ost ost){
        //preQueued.add(preQueued.size() - queueAddPos,ost);
        queued.add(queued.size() - queueAddPos, ost);
        queueAddPos++;
        notifyDataSetChanged();
    }

    void initiateQueue(List<Ost> ostList, int startId){
        queueAddPos = 0;
        played = new Stack<>();
        preQueued = new Stack<>();
        for(int i = ostList.size() - 1; i >= 0; i--) {
            if(i > startId){
                preQueued.add(ostList.get(i));
            }else{
                played.add(ostList.get(i));
            }
        }
        notifyDataSetChanged();
    }
}
