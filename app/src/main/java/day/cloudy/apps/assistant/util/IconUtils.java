package day.cloudy.apps.assistant.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Gaelan Bolger on 12/19/2016.
 */
public class IconUtils {

    public static Bitmap getBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable)
            return ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return bitmap;
    }

    public static String encodeToBase64(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] ba = bos.toByteArray();
        return Base64.encodeToString(ba, Base64.DEFAULT);
    }

    public static Bitmap decodeFromBase64(String input) {
        byte[] ba = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(ba, 0, ba.length);
    }
}
