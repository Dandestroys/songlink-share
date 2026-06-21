package com.songlink.share.ui

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.songlink.share.R
import com.songlink.share.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onCopy: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<HistoryEntry> = emptyList()

    fun submitList(list: List<HistoryEntry>) {
        val oldSize = items.size
        items = list
        when {
            list.isEmpty() -> notifyDataSetChanged()
            list.size > oldSize -> notifyItemRangeInserted(0, list.size - oldSize)
            else -> notifyDataSetChanged()
        }
    }

    override fun getItemCount() = items.size + 1

    override fun getItemViewType(position: Int) = if (position < items.size) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            ItemHolder(inflater.inflate(R.layout.item_history, parent, false), onCopy)
        } else {
            object : RecyclerView.ViewHolder(
                inflater.inflate(R.layout.item_attribution, parent, false)
            ) {}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder) holder.bind(items[position])
    }

    class ItemHolder(view: View, private val onCopy: (String) -> Unit) :
        RecyclerView.ViewHolder(view) {

        private val tvUrl: TextView = view.findViewById(R.id.tv_url)
        private val tvTime: TextView = view.findViewById(R.id.tv_time)
        private val btnCopy: ImageButton = view.findViewById(R.id.btn_copy)
        private val handler = Handler(Looper.getMainLooper())
        private var resetRunnable: Runnable? = null

        fun bind(entry: HistoryEntry) {
            tvUrl.text = entry.pageUrl
            tvTime.text = formatTimestamp(entry.timestamp)
            btnCopy.setImageResource(R.drawable.ic_content_copy)
            btnCopy.setOnClickListener {
                onCopy(entry.pageUrl)
                btnCopy.setImageResource(R.drawable.ic_check_circle)
                resetRunnable?.let { handler.removeCallbacks(it) }
                resetRunnable = Runnable {
                    btnCopy.setImageResource(R.drawable.ic_content_copy)
                }
                handler.postDelayed(resetRunnable!!, 1500)
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            return when {
                diff < 60_000L -> "Just now"
                diff < 3_600_000L -> "${diff / 60_000}m ago"
                diff < 86_400_000L -> "${diff / 3_600_000}h ago"
                else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }
}
