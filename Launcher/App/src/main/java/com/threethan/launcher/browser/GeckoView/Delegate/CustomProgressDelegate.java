package com.threethan.launcher.browser.GeckoView.Delegate;

import androidx.annotation.NonNull;

import com.threethan.launcher.browser.BrowserActivity;

import org.mozilla.geckoview.GeckoSession;

public class CustomProgressDelegate implements GeckoSession.ProgressDelegate {
    private final BrowserActivity mActivity;
    public CustomProgressDelegate(BrowserActivity activity) {
        super();
        this.mActivity = activity;
    }
    @Override
    public void onPageStart(@NonNull GeckoSession session, @NonNull String url) {
        GeckoSession.ProgressDelegate.super.onPageStart(session, url);
        if (mActivity !=null) mActivity.startLoading();
    }
    @Override
    public void onPageStop(@NonNull GeckoSession session, boolean success) {
        GeckoSession.ProgressDelegate.super.onPageStop(session, success);
        if (mActivity != null) mActivity.stopLoading();
    }
}