package com.example.egatepass;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException; // Import specific exception
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import java.util.Hashtable;

public class QrUtil {
    public static Bitmap generateQrBitmap(String text, int size) throws WriterException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        QRCodeWriter writer = new QRCodeWriter();

        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints);

        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bmp;
    }
}