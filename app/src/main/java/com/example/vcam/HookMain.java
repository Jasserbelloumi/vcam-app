package com.example.vcam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;

public class HookMain implements IXposedHookLoadPackage {
    public static String video_path = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) throws Throwable {
        hookCamera1(lp);
    }

    private void hookCamera1(XC_LoadPackage.LoadPackageParam lp) throws Throwable {
        Class<?> cam;
        try { cam = lp.classLoader.loadClass("android.hardware.Camera"); }
        catch (ClassNotFoundException e) { return; }

        for (Method m : cam.getDeclaredMethods()) {
            if (m.getName().equals("addCallbackBuffer")) {
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        replaceFrame(param);
                    }
                });
            }
        }
    }

    private void replaceFrame(XC_MethodHook.MethodHookParam param) {
        try {
            byte[] buf = (byte[]) param.args[0];
            if (buf == null || video_path == null) return;
            File f = new File(video_path);
            if (!f.exists()) return;

            Bitmap src = BitmapFactory.decodeFile(f.getAbsolutePath());
            if (src == null) return;
            src = BitmapTransform.apply(src, TransformSettings.get());

            int w = src.getWidth(), h = src.getHeight();
            int[] px = new int[w * h];
            src.getPixels(px, 0, w, 0, 0, w, h);
            byte[] nv21 = new byte[w * h * 3 / 2];
            int i = 0, uv = w * h;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int p = px[y * w + x];
                    int r = (p >> 16) & 0xff, g = (p >> 8) & 0xff, b = p & 0xff;
                    nv21[i++] = (byte)(((66*r+129*g+25*b+128)>>8)+16);
                    if (y%2==0 && x%2==0 && uv+1 < nv21.length) {
                        nv21[uv++] = (byte)(((-38*r-74*g+112*b+128)>>8)+128);
                        nv21[uv++] = (byte)(((112*r-94*g-18*b+128)>>8)+128);
                    }
                }
            }
            if (nv21.length <= buf.length)
                System.arraycopy(nv21, 0, buf, 0, nv21.length);
        } catch (Exception e) {
            XposedBridge.log("[VCAM] " + e.toString());
        }
    }
}
