package com.example.vcam;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.widget.*;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 60, 40, 40);
        setContentView(root);

        TextView title = new TextView(this);
        title.setText("VCam - كاميرا افتراضية");
        title.setTextSize(22); title.setPadding(0, 0, 0, 30);
        root.addView(title);

        Button btnOverlay = new Button(this);
        btnOverlay.setText("تشغيل أزرار التحكم (Overlay)");
        btnOverlay.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
            } else {
                startService(new Intent(this, OverlayService.class));
                Toast.makeText(this, "تم تشغيل أزرار التحكم", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(btnOverlay);

        Button btnStop = new Button(this);
        btnStop.setText("إيقاف أزرار التحكم");
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, OverlayService.class));
            Toast.makeText(this, "تم إيقاف الأزرار", Toast.LENGTH_SHORT).show();
        });
        root.addView(btnStop);

        TextView info = new TextView(this);
        info.setText("\nملاحظة: يتطلب صلاحيات Root + Xposed/LSPosed مفعّل");
        info.setTextSize(13);
        root.addView(info);
    }
}
