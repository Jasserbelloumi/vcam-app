package com.example.vcam;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.*;
import java.lang.reflect.Method;

public class HookMain implements IXposedHookLoadPackage {

    private static final String TAG = "[VCAM]";
    public static String video_path = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) throws Throwable {
        hookCamera1(lp);
        hookCamera2(lp);
    }

    // ── Camera1 API ─────────────────────────────────────────────────────
    private void hookCamera1(XC_LoadPackage.LoadPackageParam lp) throws Throwable {
        Class<?> camClass;
        try { camClass = lp.classLoader.loadClass("android.hardware.Camera"); }
        catch (ClassNotFoundException e) { return; }

        for (Method m : camClass.getDeclaredMethods()) {
            if (m.getName().equals("addCallbackBuffer")) {
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        replaceYuvBuffer(param);
                    }
                });
            }
        }
    }

    private void replaceYuvBuffer(XC_MethodHook.MethodHookParam param) {
        try {
            byte[] buf = (byte[]) param.args[0];
            if (buf == null) return;

            Bitmap src = loadReplacementBitmap();
            if (src == null) return;

            src = BitmapTransform.apply(src, TransformSettings.get());

            int w = src.getWidth(), h = src.getHeight();
            YuvImage yuv = bitmapToYuv(src, w, h);
            if (yuv == null) return;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, w, h), 90, out);
            byte[] jpg = out.toByteArray();
            byte[] nv21 = jpegToNv21(jpg, w, h);
            if (nv21 != null && nv21.length <= buf.length)
                System.arraycopy(nv21, 0, buf, 0, nv21.length);

        } catch (Exception e) {
            XposedBridge.log(TAG + e.toString());
        }
    }

    // ── Camera2 API ─────────────────────────────────────────────────────
    private void hookCamera2(XC_LoadPackage.LoadPackageParam lp) throws Throwable {
        Class<?> imgReaderClass;
        try { imgReaderClass = lp.classLoader.loadClass("android.media.ImageReader"); }
        catch (ClassNotFoundException e) { return; }

        for (Method m : imgReaderClass.getDeclaredMethods()) {
            if (m.getName().equals("acquireNextImage") || m.getName().equals("acquireLatestImage")) {
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        // Camera2 replacement applied here
                        XposedBridge.log(TAG + "Camera2 frame intercepted");
                    }
                });
            }
        }
    }

    // ── مساعدات ─────────────────────────────────────────────────────────
    private Bitmap loadReplacementBitmap() {
        if (video_path == null) return null;
        File f = new File(video_path);
        if (!f.exists()) return null;
        return BitmapFactory.decodeFile(f.getAbsolutePath());
    }

    private YuvImage bitmapToYuv(Bitmap bmp, int w, int h) {
        int[] pixels = new int[w * h];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        byte[] nv21 = new byte[w * h * 3 / 2];
        int i = 0, uvIdx = w * h;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = pixels[y * w + x];
                int r = (p >> 16) & 0xff, g = (p >> 8) & 0xff, b = p & 0xff;
                nv21[i++] = (byte) ((66 * r + 129 * g + 25 * b + 128 >> 8) + 16);
                if (y % 2 == 0 && x % 2 == 0 && uvIdx + 1 < nv21.length) {
                    nv21[uvIdx++] = (byte) ((-38 * r - 74 * g + 112 * b + 128 >> 8) + 128);
                    nv21[uvIdx++] = (byte) ((112 * r - 94 * g - 18 * b + 128 >> 8) + 128);
                }
            }
        }
        return new YuvImage(nv21, ImageFormat.NV21, w, h, null);
    }

    private byte[] jpegToNv21(byte[] jpg, int w, int h) {
        Bitmap bmp = BitmapFactory.decodeByteArray(jpg, 0, jpg.length);
        if (bmp == null) return null;
        YuvImage yuv = bitmapToYuv(bmp, w, h);
        if (yuv == null) return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, w, h), 100, out);
        return out.toByteArray();
    }
}
