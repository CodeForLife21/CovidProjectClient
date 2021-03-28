package ie.com.covidproject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

public class GeofenceHelper  extends ContextWrapper {

    private static final String TAG = "GeofenceHelper";
    PendingIntent pendingIntent;

    public GeofenceHelper(Context base) {
        super(base);
    }


    public GeofencingRequest getGeofencingRequest(Geofence geofence){

        return new GeofencingRequest.Builder()
                .addGeofence(geofence)// if want to add a list of geofences using method that takes list parameters
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .build();


    }

    public Geofence getGeofence(String ID, LatLng latLng, float radius, int transitionTypes){

        return new Geofence.Builder().setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setRequestId(ID)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000) // used for Dwell to update user while inside geofence
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();


    }

    public PendingIntent getPendingIntent(){
        if (pendingIntent != null){
            return pendingIntent;

        }

        Intent intent = new Intent(this, GeofenceBroadcastReciever.class);
        pendingIntent = PendingIntent.getBroadcast(this, 2607, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }



    public String getErrorString(Exception e ){

        if(e instanceof ApiException){
            ApiException apiException = (ApiException) e;

            switch (apiException.getStatusCode()){

                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE_IS_NOT_AVAILABLE";

                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return "TOO_MANY_GEOFENCES";

                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "TOO_MANY_GEOFENCE_PENDING_INTENTS";



            }
        }

        return e.getLocalizedMessage();


    }
}
