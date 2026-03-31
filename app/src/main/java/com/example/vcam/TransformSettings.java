package com.example.vcam;

public class TransformSettings {
    private static TransformSettings instance;
    public boolean flipHorizontal = false;
    public boolean flipVertical   = false;
    public int     rotation       = 0;
    public float   panX           = 0f;
    public float   panY           = 0f;
    public float   scale          = 1f;

    public static TransformSettings get() {
        if (instance == null) instance = new TransformSettings();
        return instance;
    }
    public void rotateRight() { rotation = (rotation + 90) % 360; }
    public void rotateLeft()  { rotation = (rotation + 270) % 360; }
    public void resetAll() {
        flipHorizontal = false; flipVertical = false;
        rotation = 0; panX = 0; panY = 0; scale = 1f;
    }
}
