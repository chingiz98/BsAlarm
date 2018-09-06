package com.bsaldevs.bsalarmer.Managers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.bsaldevs.bsalarmer.BroadcastActions;
import com.bsaldevs.bsalarmer.Constants;
import com.bsaldevs.bsalarmer.MyLocation;
import com.bsaldevs.bsalarmer.Point;
import com.bsaldevs.bsalarmer.Utils;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by azatiSea on 21.08.2018.
 */

public class MyLocationManager {

    private static final String TAG = Constants.TAG;

    private Context context;
    private MyLocation location;
    private PointManager pointManager;
    private LocationManager locationManager;

    public MyLocationManager(Context context) {
        location = new MyLocation();
        pointManager = new PointManager(context);
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged");
                setLocation(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "onProviderEnabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled");
            }
        });
    }

    public Point getTargetById(String id) {
        return pointManager.getPoint(id);
    }

    public void removeTarget(String id) {
        pointManager.remove(id);
    }

    public void addTarget(Point pPoint, String id) {
        pointManager.createPoint(pPoint, id);
        checkIsTargetReached(pointManager.getPoint(id));
    }

    public List<Point> getTargets() {
        return pointManager.getPoints();
    }

    public void setLocation(double latitude, double longitude) {
        location.setLocation(latitude, longitude);
        checkIsTargetsReached();
    }

    private void checkIsTargetReached(Point target) {

        if (!target.isActive()) {
            target.setAchieved(false);
            Log.d(TAG, "checkIsTargetReached target is not active");
            Log.d(TAG, "checkIsTargetReached: close notification");

            Intent notification = new Intent(Constants.NOTIFICATION_ACTION)
                    .putExtra("point", target);
            notification.putExtra("task", BroadcastActions.CLOSE_NOTIFICATION);

            context.sendBroadcast(notification);
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        boolean reached = false;

        Log.d(TAG, "MyLocationManager: checkIsTargetReached: latitude: " + latitude);
        Log.d(TAG, "MyLocationManager: checkIsTargetReached: longitude: " + longitude);
        Log.d(Constants.TAG, "MyLocationManager: checkIsTargetReached: Math.abs(latitude - target.getLatitude()) " + Math.abs(latitude - target.getLatitude()) + " ? " + location.getActionRadius());
        Log.d(Constants.TAG, "MyLocationManager: checkIsTargetReached: Math.abs(longitude - target.getLongitude()) " + Math.abs(longitude - target.getLongitude()) + " ? " + location.getActionRadius());

//        if (Math.abs(latitude - target.getLatitude()) < location.getActionRadius() && Math.abs(longitude - target.getLongitude()) < location.getActionRadius()) {

        LatLng myLocation = new LatLng(latitude, longitude);
        LatLng targetLocation = new LatLng(target.getLatitude(), target.getLongitude());

        if (Utils.CalculateDistanceBetween(myLocation, targetLocation) <= location.getActionRadius()) {
            Log.d(Constants.TAG, "MyLocationManager: checkIsTargetReached: point is achieved");
            reached = true;
        } else {
            Log.d(Constants.TAG, "MyLocationManager: checkIsTargetReached: point is un achieved");
        }

        if (reached != target.isAchieved()) {
            target.setAchieved(reached);
            if (target.isActive())
                sendChangedStateOfPoint(target, reached);
        }
    }

    private void checkIsTargetsReached() { // TODO(Rename this method)
        List<Point> points = pointManager.getPoints();
        for (Point target : points) {
            checkIsTargetReached(target);
        }
    }

    private void sendChangedStateOfPoint(Point point, boolean reached) {
        Log.d(TAG, "sendChangedStateOfPoint");

        Intent notification = new Intent(Constants.NOTIFICATION_ACTION)
                .putExtra("point", point);
        Intent alarm = new Intent(Constants.ALARM_ACTION);

        if (reached) {
            Log.d(TAG, "sendChangedStateOfPoint: create notification");
            notification.putExtra("task", BroadcastActions.CREATE_NOTIFICATION);
            alarm.putExtra("task", BroadcastActions.ALARM);
        }
        else {
            Log.d(TAG, "sendChangedStateOfPoint: close notification");
            notification.putExtra("task", BroadcastActions.CLOSE_NOTIFICATION);
            alarm.putExtra("task", BroadcastActions.STOP_ALARM);
        }
        context.sendBroadcast(notification);
        context.sendBroadcast(alarm);
    }

    public void changeTarget(Point point, String extras) {
        pointManager.changePoint(point, extras);
        checkIsTargetReached(getTargetById(point.getId()));
    }
}