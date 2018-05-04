package com.tigo.skyframedemo;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.UnsupportedEncodingException;

/**
 * Author：sky on 2018/5/4 0004 13:49.
 * Email：xcode126@126.com
 * Desc：
 */

public class PrintUtils {
    /**
     * 字符串转byte数组
     */
    public static byte[] strTobytes(String str) {
        byte[] b = null, data = null;
        try {
            b = str.getBytes("utf-8");
            data = new String(b, "utf-8").getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 二值法的到的单色图
     * 灰度图,再转为单色图，的到单色图的图像信息
     *
     * @param img 位图
     * @return data返回转换好的单色位图的图像信息
     */
    public static Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高
        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        //求灰度图的的算术平均值，阈值
        double redSum = 0, greenSum = 0, blueSun = 0;
        double total = width * height;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                redSum += red;
                greenSum += green;
                blueSun += blue;
            }
        }
        int m = (int) (redSum / total);
        //二值法，转换单色图
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int alpha1 = 0xFF << 24;
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                if (red >= m) {
                    red = green = blue = 255;
                } else {
                    red = green = blue = 0;
                }
                grey = alpha1 | (red << 16) | (green << 8) | blue;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return mBitmap;
    }


    /**
     * 使用Bitmap加Matrix来缩放
     *
     * @param bitmap
     * @param w
     * @param ischecked
     * @return
     */
    public static Bitmap resizeImage(Bitmap bitmap, int w, boolean ischecked) {
        Bitmap BitmapOrg = bitmap;
        Bitmap resizedBitmap = null;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        if (width <= w) {
            return bitmap;
        }
        if (!ischecked) {
            int newWidth = w;
            int newHeight = height * w / width;
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // if you want to rotate the Bitmap
            // matrix.postRotate(45);
            resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                    height, matrix, true);
        } else {
            resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, w, height);
        }
        return resizedBitmap;
    }

    /**
     * 送纸 送n行
     *
     * @return
     */
    public static byte[] advanceLine(int line) {
        return new byte[]{0x1b, 0x64, Integer.decode("0x" + line).byteValue()};
    }

    public static byte[] getFormatBytes(String msg) {
        try {
            return msg.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 设置字体大小-正常
     *
     * @return
     */
    public static byte[] textSizeNormal() {
        return new byte[]{0x1d, 0x21, 0x0};
    }
}
