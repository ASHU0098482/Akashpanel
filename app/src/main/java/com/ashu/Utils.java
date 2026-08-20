package com.ashu;

import android.content.Context;
import android.util.TypedValue;

public class Utils {

    Context context;

    public Utils(Context globContext) {
        context = globContext;
    }

    public int FixDP(int i) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) i, context.getResources().getDisplayMetrics());
    }

    public static android.graphics.Bitmap makeBlackTransparent(android.graphics.Bitmap src) {
        if (src == null) return null;
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            // Make black background transparent
            if (r < 25 && g < 25 && b < 25) {
                pixels[i] = 0x00000000;
            }
        }
        android.graphics.Bitmap result = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

}
