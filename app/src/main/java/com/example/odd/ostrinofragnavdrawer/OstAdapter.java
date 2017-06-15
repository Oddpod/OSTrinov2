package com.example.odd.ostrinofragnavdrawer;

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

import com.example.odd.ostrinofragnavdrawer.Listeners.PlayerListener;
import com.example.odd.ostrinofragnavdrawer.Listeners.QueueListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class OstAdapter extends RecyclerView.Adapter<OstAdapter.ViewWrapper> implements PlayerListener{
        private Stack<Ost> played, queue;
        private Context mContext;

        private QueueListener queueListener;
        private int nowPlaying = -1, queueAddPos;
        private OnItemClickListener onItemClickListener;
        private boolean emptyAdapter;

    public OstAdapter(){
        this.emptyAdapter = true;
    }

    public OstAdapter(Context context, List<Ost> ostList, int startIndex, QueueListener queueListener) {
            queueAddPos = 0;
            played = new Stack<>();
            queue = new Stack<>();
            for(int i = ostList.size() - 1; i >= 0; i--) {
                if(i > startIndex){
                    queue.add(ostList.get(i));
                }else{
                    played.add(ostList.get(i));
                }
            }
            this.mContext = context;
            this.queueListener = queueListener;
        }

        @Override
        public ViewWrapper onCreateViewHolder(ViewGroup viewGroup, int i) {
            // create a new view
            @SuppressLint("InflateParams") View itemLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_row, null);
            // create ViewHolder
            return new ViewWrapper(itemLayoutView);
        }

        @Override
        public void onBindViewHolder(final ViewWrapper viewWrapper, final int position) {
            final int invPos = queue.size() - (1 + position);
            final Ost ost = queue.get(invPos);
            viewWrapper.getTitle().setText(ost.getTitle());
            viewWrapper.getShow().setText(ost.getShow());
            viewWrapper.getTags().setText(ost.getTags());
            File tnFile = new File(Environment.getExternalStorageDirectory()
                    + "/OSTthumbnails/" + Util.urlToId(ost.getUrl()) + ".jpg");
            Picasso.with(mContext)
                    .load(tnFile)
                    .placeholder(R.drawable.tranquility)
                    .into(viewWrapper.getThumbnail());
            viewWrapper.getBtnOptions().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    queue.remove(invPos);
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
                return queue.size();
            }
        }

    @Override
    public void updateCurrentlyPlaying(int newId) {
            nowPlaying = newId;
    }

    @Override
    public void next() {
        played.add(queue.pop());
        notifyDataSetChanged();
        queueAddPos--;
    }

    @Override
    public void previous() {
        queue.add(played.pop());
        notifyItemInserted(0);
        queueAddPos++;

    }

    @Override
    public void shuffle(List<Ost> osts) {

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

            View getBase(){
                return base;
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
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void removeFromQueue(int id){
        queue.remove(id);
        notifyDataSetChanged();
    }

    public void addToQueue(Ost ost){
        queue.add(queue.size() - queueAddPos,ost);
        queueAddPos++;
        notifyDataSetChanged();
    }
}
