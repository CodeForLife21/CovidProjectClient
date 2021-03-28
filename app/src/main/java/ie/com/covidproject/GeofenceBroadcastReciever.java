package ie.com.covidproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

// triggered when geofence is triggered
// to listen for geofence transitions
public class GeofenceBroadcastReciever extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiv";

     

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        // make a toast when triggered

        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofencing reciever");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }
//        Location location = geofencingEvent.getTriggeringLocation();
        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, "Entered Your Travel Restriction Area", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Entered Your Travel Restriction Area", "", MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "Inside Your Travel Restriction Area", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Inside Your Travel Restriction Area", "", MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "You Are Outside Of Your Travel Restriction Area", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("You Are Outside Of Your Travel Restriction Area", "", MapsActivity.class);
                break;
        }

    }
}