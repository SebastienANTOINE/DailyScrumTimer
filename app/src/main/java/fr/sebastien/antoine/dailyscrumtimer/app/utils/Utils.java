package fr.sebastien.antoine.dailyscrumtimer.app.utils;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

/**
 * Utils
 *
 * Created by Sebastien on 06/03/2014.
 */
public class Utils {

    /**
     * Return size of the screen
     *
     * @param activity
     * @return
     */
    public static Point size (Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }
}