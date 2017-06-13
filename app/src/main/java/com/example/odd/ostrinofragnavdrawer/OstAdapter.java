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


public class OstAdapter extends RecyclerView.Adapter<OstAdapter.ViewWrapper> implements PlayerListener{
        private List<Ost> filteredOstList, ostList, played;
        private Context mContext;

        private QueueListener queueListener;
        private int nowPlaying = -1;
        private boolean queue;
        private OnItemClickListener onItemClickListener;

    public OstAdapter( Context context, List<Ost> ostList, QueueListener queueListener) {
            this.ostList = ostList;
            this.mContext = context;
            this.queueListener = queueListener;
            this.played = new ArrayList<>();
        }

        @Override
        public ViewWrapper onCreateViewHolder(ViewGroup viewGroup, int i) {
            // create a new view
            @SuppressLint("InflateParams") View itemLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_row, null);
            // create ViewHolder
            return new ViewWrapper(itemLayoutView);
        }

        @Override
        public void onBindViewHolder(final ViewWrapper viewWrapper, int position) {
            final int pos = viewWrapper.getAdapterPosition();
            Ost ost = ostList.get(pos);
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
                    queueListener.addToQueue(pos);
                }
            });
        }

        void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
    }
        @Override
        public int getItemCount() {
            return ostList.size();
        }

    @Override
    public void updateCurrentlyPlaying(int newId) {
            notifyItemChanged(nowPlaying);
            nowPlaying = newId;
            notifyItemChanged(nowPlaying);
    }

    @Override
    public void next() {
        played.add(ostList.remove(0));
        notifyDataSetChanged();
    }

    @Override
    public void previous() {
        ostList.add(0, played.remove(played.size() - 1));
        notifyDataSetChanged();

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
        ostList.remove(id);
        notifyDataSetChanged();
    }

    public void addToQueue(Ost ost){
        ostList.add(1, ost);
        notifyDataSetChanged();
    }
}
