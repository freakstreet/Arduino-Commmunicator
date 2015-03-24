package freakycamper.com.freaky.arduino_commmunicator.utils;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by lsa on 09/02/15.
 */
public class FreakyTools {

    public static boolean isMyServiceRunning(Class<?> serviceClass, ActivityManager manager) {
        //ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
