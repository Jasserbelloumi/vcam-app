package com.example.vcam;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class OverlayService extends Service {
    private WindowManager wm;
    private View panel;
    private float lastX, lastY;

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        buildPanel();
        return START_STICKY;
    }

    private void buildPanel() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.argb(180, 0, 0, 0));
        layout.setPadding(8, 8, 8, 8);
        panel = layout;

        addBtn(layout, "qalb ofqi", v -> {
            TransformSettings.get().flipHorizontal = !TransformSettings.get().flipHorizontal;
            toast("flip H: " + TransformSettings.get().flipHorizontal);
        });
        addBtn(layout, "qalb 3amudi", v -> {
            TransformSettings.get().flipVertical = !TransformSettings.get().flipVertical;
            toast("flip V: " + TransformSettings.get().flipVertical);
        });
        addBtn(layout, "tadwir +90", v -> {
            TransformSettings.get().rotateRight();
            toast("rot: " + TransformSettings.get().rotation);
        });
        addBtn(layout, "tadwir -90", v -> {
            TransformSettings.get().rotateLeft();
            toast("rot: " + TransformSettings.get().rotation);
        });
        addBtn(layout, "<- yasar",  v -> { TransformSettings.get().panX -= 50; toast("X: " + TransformSettings.get().panX); });
        addBtn(layout, "-> yamin",  v -> { TransformSettings.get().panX += 50; toast("X: " + TransformSettings.get().panX); });
        addBtn(layout, "^ a3la",    v -> { TransformSettings.get().panY -= 50; toast("Y: " + TransformSettings.get().panY); });
        addBtn(layout, "v asfal",   v -> { TransformSettings.get().panY += 50; toast("Y: " + TransformSettings.get().panY); });
        addBtn(layout, "+ zoom in",  v -> { TransformSettings.get().scale = Math.min(3f, TransformSettings.get().scale + 0.1f); toast("zoom: " + TransformSettings.get().scale); });
        addBtn(layout, "- zoom out", v -> { TransformSettings.get().scale = Math.max(0.5f, TransformSettings.get().scale - 0.1f); toast("zoom: " + TransformSettings.get().scale); });
        addBtn(layout, "reset", v -> { TransformSettings.get().resetAll(); toast("reset"); });
        addBtn(layout, "X close", v -> stopSelf());

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0; params.y = 100;

        panel.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                lastX = e.getRawX(); lastY = e.getRawY();
            } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                params.x += (int)(e.getRawX() - lastX);
                params.y += (int)(e.getRawY() - lastY);
                lastX = e.getRawX(); lastY = e.getRawY();
                wm.updateViewLayout(panel, params);
            }
            return false;
        });
        wm.addView(panel, params);
    }

    private void addBtn(LinearLayout p, String label, View.OnClickListener c) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(11f);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.argb(200, 30, 80, 160));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 2, 0, 2);
        b.setLayoutParams(lp);
        b.setOnClickListener(c);
        p.addView(b);
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }

    @Override public void onDestroy() { if (panel != null) wm.removeView(panel); }
    @Override public IBinder onBind(Intent i) { return null; }
}
