package ie.com.covidproject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private Marker homeMarker, businessMarker;
    private List<Pubs> businessList;
    private LatLng businessLocation, userLocation;
    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    // private Geocoder geocoder;
    private Pubs pubs;
    private CircleOptions circleOptions;
    private Circle mapCircle;
    private int ACCESS_COARSE_LOCATION_REQUEST_CODE = 10003;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeofenceHelper geofenceHelper;
    private Button addGeoBtn, btnRemove;
    private String GEOFENCE_ID = "UNIQUE_ID";
    private static final String TAG = "MapsActivity";
    private PendingIntent pendingIntent;
    private int km;
    private GeofencingRequest geofencingRequest;
    private Geofence geofence;
    private Integer availability, geofenceRadiusSize;
    private String businessTAG, userInput;
    private EditText userInputForGeofenceSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        geofencingClient = LocationServices.getGeofencingClient(this);
        //geocoder = new Geocoder(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geofenceHelper = new GeofenceHelper(this);
        addGeoBtn = findViewById(R.id.btnGo);
        btnRemove = findViewById(R.id.btnRemove);
        userInputForGeofenceSize = findViewById(R.id.geofenceEditSizeTxt);
        userInputForGeofenceSize.setHintTextColor(getResources().getColor(R.color.white));

        addUserGeoFence();
        removeGeofence();

        businessList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("details");
        circleOptions = new CircleOptions();
        btnRemove.setClickable(false);


    }

    /*
     Method to activate geofence
     check for permissions
    */
    public void addUserGeoFence() {

        // on button click, first check permissions to see if location services enabled
        addGeoBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= 29) {
                //We need background permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    creatingGeo();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        //We show a dialog and ask for permission
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                    }
                }

            } else {
                creatingGeo();
            }

        });
    }
   /* This Method creates a geofence based on the users current location
    User enters their geofence size from editText
    displays all available bussinesses within that geofence size
    displays geofence radius using Circle object
    */

    public void creatingGeo() {
        if (TextUtils.isEmpty(userInputForGeofenceSize.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Geofence size can not be empty", Toast.LENGTH_SHORT).show();
        } else
            // using fusedLocationProviderClient to return the best and most recent location currently available.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                return;
            }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(MapsActivity.this, location -> {
            // passing the user input from the editText field to String userInput
            userInput = userInputForGeofenceSize.getText().toString();
            Toast.makeText(getApplicationContext(), "" + userInput + "km Geofence Created", Toast.LENGTH_SHORT).show();
            // parsing userInput to Int and assigning it to Int geofenceRadiusSize
            geofenceRadiusSize = Integer.parseInt(userInput);
            // get userLocation lat and lng
            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            //  addGeofence method passing in location of user along with geofence size
            addGeofence(userLocation, geofenceRadiusSize * 1000);   // change to KM, 1000 x 1 Meter = 1KM
            //  displayBusinessesInGeoArea method to display all businesses inside geofence radius
            displayBusinessesInGeoArea();
            // enable remove geofence button to clickable
            btnRemove.setClickable(true);


        });
    }


    /*
    This Method removes the Geofence, saving battery life
    Removes circle created
    removes home marker
    restores data from firebase, displaying all business markers on map
     */
    private void removeGeo() {

        geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        pendingIntent = geofenceHelper.getPendingIntent();
        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent()).addOnSuccessListener(this, aVoid -> {
            if (geofencingClient != null) {
                removeCircle();
                homeMarker.remove();
                setUpBusinessInfoFromDB();
                Toast.makeText(getApplicationContext(), "Geofence was removed", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "No Geofence created", Toast.LENGTH_SHORT).show();

            }

        }).addOnFailureListener(this, e -> Toast.makeText(getApplicationContext(), "Failed To Remove Geofence", Toast.LENGTH_SHORT).show());

    }

    /*
    This Method is used to to remove the geofence after button click
    */

    public void removeGeofence() {

        btnRemove.setOnClickListener(view -> {
            removeGeo();
        });

    }


    /*
    Method to add Geofence
    */

    private void addGeofence(LatLng latLng, float radius) {

        geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        pendingIntent = geofenceHelper.getPendingIntent();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "Geofence was added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    String errMessage = geofenceHelper.getErrorString(e);
                    Toast.makeText(getApplicationContext(), "Geofence failed check location permissions and enable ", Toast.LENGTH_LONG).show();
                });
    }


    /*
    Method to display all markers within geofence size
    calculates the distance from two Latlng points using the SphericalUtil library
    distance is divided by 1000 to convert to KM
    if distance greater than user geofence size remove them markers from map
   */
    public void showMarkersIfInGeofenceArea(LatLng from, LatLng too) {

        Double distance = SphericalUtil.computeDistanceBetween(from, too);
        geofenceRadiusSize = Integer.parseInt(userInput);
        if (distance / 1000 > geofenceRadiusSize) {
            businessMarker.remove();
            addUserHomeMarker(userLocation);

        }


    }
    /*
     Method to set the colors of the markers on the map
     depending on the how many people are occupying the
     businesses
    */

    public void setMarkerColors(Pubs pubs) {

        if ((pubs.getOccupancy() * 100) / (pubs.getCapacity()) <= 60) {
            businessMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if ((pubs.getOccupancy() * 100) / (pubs.getCapacity()) > 60 && (pubs.getOccupancy() * 100) / (pubs.getCapacity()) <= 90) {
            businessMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        } else
            businessMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    }

    /*
        Method to display businesses if in Geofence area
        get info from firebase
        set marker colors based on occupancy
        display

      */

    public void displayBusinessesInGeoArea() {
        // get info from firebase
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear();
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    businessList.clear();
                    pubs = s.getValue(Pubs.class);
                    businessList.add(pubs);

                    for (int i = 0; i < businessList.size(); i++) {
                        businessLocation = new LatLng(pubs.getLatitude(), pubs.getLongitude());
                        availability = pubs.getCapacity() - pubs.getOccupancy();
                        businessMarker = mMap.addMarker(new MarkerOptions()
                                .position(businessLocation)
                                .title(pubs.getDescription())
                                .snippet(" Capacity " + pubs.getCapacity() + " Occupied " + pubs.getOccupancy() + " availability" + availability));
                        // set marker colors based on occupancy
                        setMarkerColors(pubs);
                        // display businesses within Geo size,remove if not
                        showMarkersIfInGeofenceArea(userLocation, businessLocation);
                        // add circle center of user location and set its size.  * 1000 is to covert to KM as its originally meters
                        addCircle(userLocation, geofenceRadiusSize * 1000);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        enableUserLocation();
    }

      /*
        Method to populate Map with data from firebase DB
      */

    public void setUpBusinessInfoFromDB() {
        // get info from firebase
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // clear map
                mMap.clear();
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    // clear arraylist
                    businessList.clear();
                    // get bar info from firebase
                    pubs = s.getValue(Pubs.class);
                    // add bar info into arrayList
                    businessList.add(pubs);

                    // looping through ArrayList with firebase info
                    for (int i = 0; i < businessList.size(); i++) {
                        // get lat and lng of businesses
                        businessLocation = new LatLng(pubs.getLatitude(), pubs.getLongitude());
                        if (mMap != null) {
                            // calculate availability
                            availability = pubs.getCapacity() - pubs.getOccupancy();
                            // add markers to map from LatLng businessLocation, including description, capacity, occupancy of each businessLocation
                            businessMarker = mMap.addMarker(new MarkerOptions()
                                    .position(businessLocation)
                                    .title(pubs.getDescription())
                                    .snippet(" Capacity " + pubs.getCapacity() + " Occupied " + pubs.getOccupancy() + " availability" + availability));
                            setMarkerColors(pubs);
                        }
                    }
                    Log.d(TAG, "ArrayList details are =>: " + businessList.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        enableUserLocation();
    }

     /*
        Display GoogleMap
        populate Map with firebase Info
      */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpBusinessInfoFromDB();

    }


    /*
      Permissions check to enable user location
     */
    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }


    }

    /*
     Permissions Check
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_REQUEST_CODE);
                    return;
                }
                mMap.setMyLocationEnabled(true);
            } else {
                //We do not have the permission..
                Toast.makeText(getApplicationContext(), "Please check background and location permissions", Toast.LENGTH_SHORT).show();

            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();
            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is necessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }

    }

     /*
        Setting a home marker based on user current location
      */

    private void addUserHomeMarker(LatLng latLng) {
        homeMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Home"));
        homeMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


    }

    /*
       Adding a circle on center of users location
       Green circle, with red outer stroke
     */
    private void addCircle(LatLng latLng, int radius) {
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 124, 252, 0));
        circleOptions.strokeWidth(4);
        mapCircle = mMap.addCircle(circleOptions);
    }

    /*
        Method to remove circle from map
     */
    private void removeCircle() {
        if (mapCircle != null) {
            mapCircle.remove();
        }
    }
}