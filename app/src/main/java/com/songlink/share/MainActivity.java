package com.songlink.share;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.songlink.share.api.SonglinkClient;
import com.songlink.share.intent.IntentHandler;
import com.songlink.share.model.HistoryEntry;
import com.songlink.share.model.SonglinkState;
import com.songlink.share.ui.HistoryAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private FrameLayout contentFrame;
    private LayoutInflater inflater;

    private View emptyView;
    private View historyView;
    private View loadingView;
    private View successView;
    private View errorView;
    private HistoryAdapter historyAdapter;

    private SonglinkState currentState = SonglinkState.idle();
    private List<HistoryEntry> currentHistory = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentFrame = findViewById(R.id.content_frame);
        inflater = LayoutInflater.from(this);

        // Load initial history
        currentHistory = HistoryRepository.load(this);

        if (savedInstanceState == null) {
            handleIntent(getIntent());
        } else {
            render(currentState, currentHistory);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        currentHistory = HistoryRepository.load(this);

        String url = IntentHandler.extractUrl(intent);
        if (url == null) {
            if (intent != null && Intent.ACTION_SEND.equals(intent.getAction())) {
                updateState(SonglinkState.error(
                    "No URL found in the shared text. Please share a link from a music app."
                ));
            } else {
                updateState(SonglinkState.idle());
            }
            return;
        }

        fetchSonglink(url);
    }

    private void fetchSonglink(final String url) {
        updateState(SonglinkState.loading());

        executor.execute(() -> {
            try {
                final com.songlink.share.model.SonglinkResponse response = SonglinkClient.getLinks(url);
                final String pageUrl = response.getPageUrl();

                if (pageUrl == null || pageUrl.trim().isEmpty()) {
                    postState(SonglinkState.error(
                        "The Songlink API returned a response without a pageUrl. " +
                        "The track may not be available in your region."
                    ));
                    return;
                }

                // Copy to clipboard
                copyToClipboard(pageUrl);

                // Update repository
                HistoryRepository.add(MainActivity.this, new HistoryEntry(pageUrl, url));
                final List<HistoryEntry> updatedHistory = HistoryRepository.load(MainActivity.this);

                postState(SonglinkState.success(pageUrl, url), updatedHistory);

            } catch (SonglinkClient.ApiException e) {
                String message;
                switch (e.getCode()) {
                    case 400:
                        message = "The URL wasn't recognised by Songlink (HTTP 400). Make sure you're sharing a supported music link.";
                        break;
                    case 404:
                        message = "Songlink couldn't find this track (HTTP 404).";
                        break;
                    case 429:
                        message = "Too many requests — please wait a moment and try again.";
                        break;
                    default:
                        if (e.getCode() >= 500 && e.getCode() <= 599) {
                            message = "Songlink's servers are having trouble (HTTP " + e.getCode() + "). Try again later.";
                        } else {
                            message = "API error (HTTP " + e.getCode() + ")";
                        }
                        break;
                }
                postState(SonglinkState.error(message));
            } catch (java.net.UnknownHostException e) {
                postState(SonglinkState.error("No internet connection. Please check your network and try again."));
            } catch (java.net.SocketTimeoutException e) {
                postState(SonglinkState.error("The request timed out. Please try again."));
            } catch (Exception e) {
                postState(SonglinkState.error("Unexpected error: " + (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Unknown error")));
            }
        });
    }

    private void updateState(SonglinkState state) {
        currentState = state;
        render(currentState, currentHistory);
    }

    private void postState(final SonglinkState state) {
        mainHandler.post(() -> {
            currentState = state;
            render(currentState, currentHistory);
        });
    }

    private void postState(final SonglinkState state, final List<HistoryEntry> history) {
        mainHandler.post(() -> {
            currentState = state;
            currentHistory = history;
            render(currentState, currentHistory);
        });
    }

    private void render(SonglinkState state, List<HistoryEntry> history) {
        View newView;
        switch (state.getType()) {
            case LOADING:
                newView = getLoadingView();
                break;
            case SUCCESS:
                newView = getSuccessView(state);
                break;
            case ERROR:
                newView = getErrorView(state.getErrorMessage());
                break;
            case IDLE:
            default:
                if (history.isEmpty()) {
                    newView = getEmptyView();
                } else {
                    newView = getHistoryView(history);
                }
                break;
        }

        View current = contentFrame.getChildAt(0);
        if (current == newView) {
            return;
        }

        if (current != null) {
            current.animate().cancel();
            current.animate().alpha(0f).setDuration(120).withEndAction(() -> {
                contentFrame.removeAllViews();
                newView.setAlpha(0f);
                contentFrame.addView(newView);
                newView.animate().alpha(1f).setDuration(160).start();
            }).start();
        } else {
            newView.setAlpha(0f);
            contentFrame.addView(newView);
            newView.animate().alpha(1f).setDuration(200).start();
        }
    }

    private View getEmptyView() {
        if (emptyView == null) {
            emptyView = inflater.inflate(R.layout.view_empty, contentFrame, false);
        }
        return emptyView;
    }

    private View getHistoryView(List<HistoryEntry> history) {
        if (historyView == null) {
            historyView = inflater.inflate(R.layout.view_history, contentFrame, false);
            historyAdapter = new HistoryAdapter(this, url -> copyToClipboard(url));
            ListView listView = historyView.findViewById(R.id.recycler_history);
            listView.setAdapter(historyAdapter);
            
            historyView.findViewById(R.id.btn_clear_all).setOnClickListener(v -> {
                HistoryRepository.clear(MainActivity.this);
                currentHistory = new ArrayList<>();
                if (currentState.getType() == SonglinkState.Type.IDLE) {
                    render(currentState, currentHistory);
                } else {
                    if (historyAdapter != null) {
                        historyAdapter.submitList(currentHistory);
                    }
                }
            });
        }
        historyAdapter.submitList(history);
        return historyView;
    }

    private View getLoadingView() {
        if (loadingView == null) {
            loadingView = inflater.inflate(R.layout.view_loading, contentFrame, false);
        }
        return loadingView;
    }

    private View getSuccessView(final SonglinkState state) {
        boolean isNew = (successView == null);
        if (successView == null) {
            successView = inflater.inflate(R.layout.view_success, contentFrame, false);
        }
        
        TextView tvLinkUrl = successView.findViewById(R.id.tv_link_url);
        tvLinkUrl.setText(state.getPageUrl());

        if (isNew) {
            ImageView icon = successView.findViewById(R.id.iv_success_icon);
            icon.setScaleX(0f);
            icon.setScaleY(0f);
            icon.setAlpha(0f);
            icon.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator(2f))
                .setStartDelay(180)
                .start();
        }

        final Button btnCopy = successView.findViewById(R.id.btn_copy_again);
        btnCopy.setText(R.string.copy_again);
        btnCopy.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_content_copy, 0, 0, 0);
        btnCopy.setOnClickListener(v -> {
            copyToClipboard(state.getPageUrl());
            btnCopy.setText(R.string.copied);
            btnCopy.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
            mainHandler.postDelayed(() -> {
                btnCopy.setText(R.string.copy_again);
                btnCopy.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_content_copy, 0, 0, 0);
            }, 1500);
        });

        successView.findViewById(R.id.btn_open_link).setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(state.getPageUrl())));
        });

        return successView;
    }

    private View getErrorView(String message) {
        if (errorView == null) {
            errorView = inflater.inflate(R.layout.view_error, contentFrame, false);
            errorView.findViewById(R.id.btn_retry).setOnClickListener(v -> {
                updateState(SonglinkState.idle());
            });
        }
        TextView tvErrorMessage = errorView.findViewById(R.id.tv_error_message);
        tvErrorMessage.setText(message);
        return errorView;
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Songlink URL", text));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
