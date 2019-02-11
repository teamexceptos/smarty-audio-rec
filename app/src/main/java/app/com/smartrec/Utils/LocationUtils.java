package app.com.smartrec.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

/**
 * Created by Big-Nosed Developer on the Edge of Infinity.
 */
public class LocationUtils {

    private static FusedLocationProviderClient mFusedLocationClient;

    @SuppressLint("MissingPermission")
    public static Task<Location> getLastLocation(Context context) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        return mFusedLocationClient.getLastLocation();
    }
}
