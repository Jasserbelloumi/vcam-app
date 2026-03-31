package com.example.vcam;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class OverlayService extends Service {

    private WindowManager wm;
    private View          panel;
    private float         lastX, lastY;

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        buildPanel();
        return START_STICKY;
    }

    private void buildPanel() {
        panel = new LinearLayout(this);
        ((LinearLayout) panel).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) panel).setBackgroundColor(Color.argb(180, 0, 0, 0));
        ((LinearLayout) panel).setPadding(8, 8, 8, 8);

        addBtn("↔ قلب أفقي", v -> {
            TransformSettings.get().flipHorizontal = !TransformSettings.get().flipHorizontal;
            toast("قلب أفقي: " + (TransformSettings.get().flipHorizontal ? "تشغيل" : "إيقاف"));
        });
        addBtn("↕ قلب عمودي", v -> {
            TransformSettings.get().flipVertical = !TransformSettings.get().flipVertical;
            toast("قلب عمودي: " + (TransformSettings.get().flipVertical ? "تشغيل" : "إيقاف"));
        });
        addBtn("↻ تدوير +90°", v -> {
            TransformSettings.get().rotateRight();
            toast("تدوير: " + TransformSettings.get().rotation + "°");
        });
        addBtn("↺ تدوير -90°", v -> {
            TransformSettings.get().rotateLeft();
            toast("تدوير: " + TransformSettings.get().rotation + "°");
        });
        addBtn("← تحريك يسار", v -> {
            TransformSettings.get().panX -= 50;
            toast("إزاحة X: " + TransformSettings.get().panX);
        });
        addBtn("→ تحريك يمين", v -> {
            TransformSettings.get().panX += 50;
            toast("إزاحة X: " + TransformSettings.get().panX);
        });
        addBtn("↑ تحريك أعلى", v -> {
            TransformSettings.get().panY -= 50;
            toast("إزاحة Y: " + TransformSettings.get().panY);
        });
        addBtn("↓ تحريك أسفل", v -> {
            TransformSettings.get().panY += 50;
            toast("إزاحة Y: " + TransformSettings.get().panY);
        });
        addBtn("+ تكبير", v -> {
            TransformSettings.get().scale = Math.min(3f, TransformSettings.get().scale + 0.1f);
            toast("تكبير: " + String.format("%.1f", TransformSettings.get().scale) + "x");
        });
        addBtn("- تصغير", v -> {
            TransformSettings.get().scale = Math.max(0.5f, TransformSettings.get().scale - 0.1f);
            toast("تصغير: " + String.format("%.1f", TransformSettings.get().scale) + "x");
        });
        addBtn("↺ إعادة ضبط", v -> {
            TransformSettings.get().resetAll();
            toast("تم إعادة الضبط");
        });
        addBtn("✕ إغلاق", v -> stopSelf());

        // إعداد نافذة عائمة قابلة للسحب
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
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN: lastX = e.getRawX(); lastY = e.getRawY(); break;
                case MotionEvent.ACTION_MOVE:
                    params.x += (int)(e.getRawX() - lastX);
                    params.y += (int)(e.getRawY() - lastY);
                    lastX = e.getRawX(); lastY = e.getRawY();
                    wm.updateViewLayout(panel, params);
                    break;
            }
            return false;
        });

        wm.addView(panel, params);
    }

    private void addBtn(String label, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(11f);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.argb(200, 30, 80, 160));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 2, 0, 2);
        b.setLayoutParams(lp);
        b.setOnClickListener(click);
        ((LinearLayout) panel).addView(b);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        if (panel != null) wm.removeView(panel);
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
