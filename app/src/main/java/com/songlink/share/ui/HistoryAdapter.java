package com.songlink.share.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import com.songlink.share.R;
import com.songlink.share.model.HistoryEntry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends BaseAdapter {

    public interface OnCopyListener {
        void onCopy(String url);
    }

    private final Context context;
    private final OnCopyListener onCopyListener;
    private List<HistoryEntry> items = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public HistoryAdapter(Context context, OnCopyListener onCopyListener) {
        this.context = context;
        this.onCopyListener = onCopyListener;
    }

    public void submitList(List<HistoryEntry> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position < items.size() ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        
        if (type == 0) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
                holder = new ViewHolder();
                holder.tvUrl = convertView.findViewById(R.id.tv_url);
                holder.tvTime = convertView.findViewById(R.id.tv_time);
                holder.btnCopy = convertView.findViewById(R.id.btn_copy);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            HistoryEntry entry = items.get(position);
            holder.tvUrl.setText(entry.getPageUrl());
            holder.tvTime.setText(formatTimestamp(entry.getTimestamp()));
            holder.btnCopy.setImageResource(R.drawable.ic_content_copy);
            
            holder.btnCopy.setOnClickListener(v -> {
                onCopyListener.onCopy(entry.getPageUrl());
                holder.btnCopy.setImageResource(R.drawable.ic_check_circle);
                
                if (holder.resetRunnable != null) {
                    handler.removeCallbacks(holder.resetRunnable);
                }
                holder.resetRunnable = () -> holder.btnCopy.setImageResource(R.drawable.ic_content_copy);
                handler.postDelayed(holder.resetRunnable, 1500);
            });
        } else {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_attribution, parent, false);
            }
        }
        
        return convertView;
    }

    private String formatTimestamp(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 60000L) {
            return "Just now";
        } else if (diff < 3600000L) {
            return (diff / 60000) + "m ago";
        } else if (diff < 86400000L) {
            return (diff / 3600000) + "h ago";
        } else {
            return new SimpleDateFormat("MMM d", Locale.getDefault()).format(new Date(timestamp));
        }
    }

    private static class ViewHolder {
        TextView tvUrl;
        TextView tvTime;
        ImageButton btnCopy;
        Runnable resetRunnable;
    }
}
