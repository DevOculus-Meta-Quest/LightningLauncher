package com.threethan.launcher.browser.GeckoView.Delegate;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.threethan.launcher.R;
import com.threethan.launcher.browser.BrowserActivity;
import com.threethan.launcher.browser.BrowserService;
import com.threethan.launcher.helper.Dialog;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;

public class ExtensionPromptDelegate implements WebExtensionController.PromptDelegate {
    @Nullable
    @Override
    public GeckoResult<AllowOrDeny> onInstallPrompt(@NonNull WebExtension extension) {
        return GeckoResult.fromValue(AllowOrDeny.ALLOW);
    }

    public void showList() {

        final Activity activity = Dialog.getActivityContext();
        if (activity == null) return;

        final AlertDialog dialog = Dialog.build(activity, R.layout.dialog_webextensions);
        assert dialog != null;
        dialog.hide();

        final ArrayAdapter<WebExtension> adapter =
            new ArrayAdapter<>(activity, R.layout.lv_webext) {
                private LayoutInflater mInflater;
                @NonNull
                @Override
                public View getView(final int position, View view, @NonNull final ViewGroup parent) {
                    if (mInflater == null) {
                        mInflater = LayoutInflater.from(activity);
                    }
                    WebExtension item = getItem(position);
                    assert item != null;
                    if (view == null) {
                        view =
                                mInflater.inflate(R.layout.lv_webext,
                                        parent,
                                        false);
                    }
                    final WebExtensionController extentionController = BrowserService.sRuntime.getWebExtensionController();
                    View finalView = view;

                    final boolean[] enabled = {item.metaData.disabledFlags == 0};
                    view.setOnClickListener(v -> {
                        if (enabled[0])
                            extentionController.disable(item, WebExtensionController.EnableSource.USER);
                        else
                            extentionController.enable(item, WebExtensionController.EnableSource.USER);
                        enabled[0] = !enabled[0];
                        finalView.setAlpha(enabled[0] ? 1f : 0.5f);
                    });

                    view.findViewById(R.id.uninstall).setOnClickListener(v -> {
                        extentionController.uninstall(item);
                        finalView.setVisibility(View.GONE);
                        dialog.dismiss();
                        showList();
                    });
                    view.findViewById(R.id.options).setOnClickListener(v -> {
                        ((BrowserActivity) activity).w.loadUrl(item.metaData.optionsPageUrl);
                        dialog.dismiss();
                    });
                    TextView name = view.findViewById(R.id.name);
                    name.setText(item.metaData.name);
                    TextView info = view.findViewById(R.id.info);
                    info.setText(item.metaData.description);
                    return view;
                }
            };

        BrowserService.sRuntime.getWebExtensionController().list()
                .then(value -> {
                    assert value != null;
                    Log.v("WEBEXTLIST", value.toString());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        value.removeIf(webExtension -> webExtension.id.equals("fixes@internal.ext"));
                    }
                    adapter.addAll(value);
                    dialog.show();

                    ListView lv = dialog.findViewById(R.id.listView);
                    lv.setAdapter(adapter);
                    return null;
                });



    }
}

