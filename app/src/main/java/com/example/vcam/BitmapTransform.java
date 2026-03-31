package com.example.vcam;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapTransform {

    public static Bitmap apply(Bitmap src, TransformSettings t) {
        if (src == null) return null;
        Matrix m = new Matrix();

        // تدوير
        if (t.rotation != 0) m.postRotate(t.rotation);

        // قلب أفقي
        if (t.flipHorizontal) m.postScale(-1f, 1f, src.getWidth() / 2f, src.getHeight() / 2f);

        // قلب عمودي
        if (t.flipVertical) m.postScale(1f, -1f, src.getWidth() / 2f, src.getHeight() / 2f);

        // تحريك
        if (t.panX != 0 || t.panY != 0) m.postTranslate(t.panX, t.panY);

        // تكبير
        if (t.scale != 1f) m.postScale(t.scale, t.scale, src.getWidth() / 2f, src.getHeight() / 2f);

        Bitmap result = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
        return result != null ? result : src;
    }
}
