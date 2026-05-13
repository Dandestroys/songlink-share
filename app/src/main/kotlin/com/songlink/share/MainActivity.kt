package com.songlink.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.songlink.share.databinding.ActivityMainBinding
import com.songlink.share.model.HistoryEntry
import com.songlink.share.model.SonglinkState
import com.songlink.share.ui.HistoryAdapter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ShareViewModel by viewModels()
    private val inflater by lazy { LayoutInflater.from(this) }

    private var emptyView: View? = null
    private var historyView: View? = null
    private var loadingView: View? = null
    private var successView: View? = null
    private var errorView: View? = null
    private var historyAdapter: HistoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(inflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            viewModel.handleIntent(intent, applicationContext)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.state, viewModel.history) { s, h -> s to h }
                    .collect { (state, history) -> render(state, history) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIntent(intent, applicationContext)
    }

    private fun render(state: SonglinkState, history: List<HistoryEntry>) {
        val view = when (state) {
            is SonglinkState.Idle    -> if (history.isEmpty()) emptyView() else historyView(history)
            is SonglinkState.Loading -> loadingView()
            is SonglinkState.Success -> successView(state)
            is SonglinkState.Error   -> errorView(state.message)
        }
        if (binding.contentFrame.getChildAt(0) !== view) {
            binding.contentFrame.removeAllViews()
            binding.contentFrame.addView(view)
        }
    }

    private fun emptyView(): View {
        if (emptyView == null)
            emptyView = inflater.inflate(R.layout.view_empty, binding.contentFrame, false)
        return emptyView!!
    }

    private fun historyView(history: List<HistoryEntry>): View {
        if (historyView == null) {
            historyView = inflater.inflate(R.layout.view_history, binding.contentFrame, false)
            val adapter = HistoryAdapter { url -> viewModel.copyToClipboard(applicationContext, url) }
            historyAdapter = adapter
            historyView!!.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_history).apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                this.adapter = adapter
            }
            historyView!!.findViewById<Button>(R.id.btn_clear_all).setOnClickListener {
                viewModel.clearHistory(applicationContext)
            }
        }
        historyAdapter?.submitList(history)
        return historyView!!
    }

    private fun loadingView(): View {
        if (loadingView == null)
            loadingView = inflater.inflate(R.layout.view_loading, binding.contentFrame, false)
        return loadingView!!
    }

    private fun successView(state: SonglinkState.Success): View {
        if (successView == null)
            successView = inflater.inflate(R.layout.view_success, binding.contentFrame, false)
        val view = successView!!
        view.findViewById<TextView>(R.id.tv_link_url).text = state.pageUrl

        val btnCopy = view.findViewById<Button>(R.id.btn_copy_again)
        val handler = Handler(Looper.getMainLooper())
        btnCopy.setText(R.string.copy_again)
        btnCopy.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_content_copy, 0, 0, 0)
        btnCopy.setOnClickListener {
            viewModel.copyToClipboard(applicationContext, state.pageUrl)
            btnCopy.setText(R.string.copied)
            btnCopy.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0)
            handler.postDelayed({
                btnCopy.setText(R.string.copy_again)
                btnCopy.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_content_copy, 0, 0, 0)
            }, 1500)
        }
        view.findViewById<Button>(R.id.btn_open_link).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.pageUrl)))
        }
        return view
    }

    private fun errorView(message: String): View {
        if (errorView == null) {
            errorView = inflater.inflate(R.layout.view_error, binding.contentFrame, false)
            errorView!!.findViewById<Button>(R.id.btn_retry).setOnClickListener { /* re-share to retry */ }
        }
        errorView!!.findViewById<TextView>(R.id.tv_error_message).text = message
        return errorView!!
    }
}
