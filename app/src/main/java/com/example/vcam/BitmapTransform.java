package com.example.vcam;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapTransform {
    public static Bitmap apply(Bitmap src, TransformSettings t) {
        if (src == null) return null;
        Matrix m = new Matrix();
        if (t.rotation != 0)
            m.postRotate(t.rotation);
        if (t.flipHorizontal)
            m.postScale(-1f, 1f, src.getWidth() / 2f, src.getHeight() / 2f);
        if (t.flipVertical)
            m.postScale(1f, -1f, src.getWidth() / 2f, src.getHeight() / 2f);
        if (t.panX != 0 || t.panY != 0)
            m.postTranslate(t.panX, t.panY);
        if (t.scale != 1f)
            m.postScale(t.scale, t.scale, src.getWidth() / 2f, src.getHeight() / 2f);
        Bitmap r = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
        return r != null ? r : src;
    }
}
