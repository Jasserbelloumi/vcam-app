package com.example.vcam;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 60, 40, 40);
        setContentView(root);

        TextView t = new TextView(this);
        t.setText("VCam Controls");
        t.setTextSize(22);
        t.setPadding(0,0,0,30);
        root.addView(t);

        Button on = new Button(this);
        on.setText("Start Overlay");
        on.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
            } else {
                startService(new Intent(this, OverlayService.class));
                Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(on);

        Button off = new Button(this);
        off.setText("Stop Overlay");
        off.setOnClickListener(v -> {
            stopService(new Intent(this, OverlayService.class));
            Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
        });
        root.addView(off);

        TextView info = new TextView(this);
        info.setText("\nRequires Root + LSPosed");
        root.addView(info);
    }
}
