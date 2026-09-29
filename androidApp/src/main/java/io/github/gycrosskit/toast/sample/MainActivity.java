package io.github.gycrosskit.toast.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;

import io.github.gycrosskit.toast.AndroidMessagePlatform;
import io.github.gycrosskit.toast.AppMessageDuration;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button button = new Button(this);
        button.setText(R.string.show_toast);
        button.setOnClickListener(view -> AndroidMessagePlatform.Companion
                .get(getApplicationContext())
                .show(getString(R.string.toast_message), AppMessageDuration.SHORT));

        FrameLayout root = new FrameLayout(this);
        root.addView(button, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));
        setContentView(root);
    }
}
