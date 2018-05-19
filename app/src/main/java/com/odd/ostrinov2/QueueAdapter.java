package com.odd.ostrinov2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.odd.ostrinov2.tools.QueueHandler;
import com.odd.ostrinov2.tools.ThumbnailHandlerKt;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewWrapper> {
    private Context mContext;

    private OnItemClickListener onItemClickListener;
    private QueueHandler queueHandler;

    QueueAdapter(Context context) {
        this.mContext = context;
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

        final Ost ost = queueHandler.getQueue().get(getItemCount() - position - 1);
        viewWrapper.getTitle().setText(ost.getTitle());
        viewWrapper.getShow().setText(ost.getShow());
        viewWrapper.getTags().setText(ost.getTags());
        ThumbnailHandlerKt.loadThumbnailInto(viewWrapper.getThumbnail(),
                ost.getVideoId(), mContext);
        viewWrapper.getBtnOptions().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queueHandler.removeFromQueue(ost);
                notifyDataSetChanged();
            }
        });
    }

    void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        if (queueHandler == null) {
            return 0;
        } else {
            return queueHandler.getQueue().size();
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
            tvTItle = base.findViewById(R.id.tvTitle);
        }
        return tvTItle;
    }

    TextView getShow() {
        if (tvShow == null) {
            tvShow = base.findViewById(R.id.tvShow);
        }
        return (tvShow);
    }

    TextView getTags() {
        if (tvTags == null) {
            tvTags = base.findViewById(R.id.tvTags);
        }
        return (tvTags);
    }

    ImageView getThumbnail() {
        if (thumbnail == null) {
            thumbnail = base.findViewById(R.id.ivThumbnail);

        }
        return thumbnail;
    }

    ImageButton getBtnOptions() {
        if (btnOptions == null) {
            btnOptions = base.findViewById(R.id.btnOptions);
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

    public void initiateQueue(QueueHandler queueHandler) {
        this.queueHandler = queueHandler;
        notifyDataSetChanged();
    }
}
