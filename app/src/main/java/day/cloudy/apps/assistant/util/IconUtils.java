package day.cloudy.apps.assistant.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Gaelan Bolger on 12/19/2016.
 */
public class IconUtils {

    private static int mAppIconSize = -1;

    public static Drawable normalize(Drawable icon) {
        if (mAppIconSize == -1) {
            mAppIconSize = Resources.getSystem().getDimensionPixelSize(android.R.dimen.app_icon_size);
        }
        icon = new ScaleDrawable(icon, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight()).getDrawable();
        icon.setBounds(0, 0, mAppIconSize, mAppIconSize);
        return icon;
    }

    public static Bitmap normalize(Bitmap icon) {
        if (mAppIconSize == -1) {
            mAppIconSize = Resources.getSystem().getDimensionPixelSize(android.R.dimen.app_icon_size);
        }
        icon = Bitmap.createScaledBitmap(icon, mAppIconSize, mAppIconSize, true);
        return icon;
    }

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

    public static Drawable cropCircle(Drawable icon) {
        Bitmap src = getBitmap(icon);
        int width = src.getWidth();
        int height = src.getHeight();
        final Path path = new Path();
        path.addCircle((float) (width / 2), (float) (height / 2),
                (float) Math.min(width, (height / 2)), Path.Direction.CCW);
        Bitmap des = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(des);
        canvas.clipPath(path);
        canvas.drawBitmap(src, 0, 0, null);
        return new BitmapDrawable(des);
    }
}
