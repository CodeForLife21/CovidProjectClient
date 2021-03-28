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
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
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
    private Geocoder geocoder;
    private Pubs pubs;
    private CircleOptions circleOptions;
    private Circle mapCircle;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeofenceHelper geofenceHelper;
    private Button add5kmBtn, btnRemove, add10kmBtn;
    private final int GEOFENCE5KM = 5000;
    private final int GEOFENCE10KM = 10000;
    private String GEOFENCE_ID = "UNIQUE_ID";
    private static final String TAG = "MapsActivity";
    private PendingIntent pendingIntent;
    private int km;
    private GeofencingRequest geofencingRequest;
    private Geofence geofence;
    private Integer availability, geofenceRadiusSize;
    private String businessTAG, userInput;
    private EditText userInputForGeofenceSize;
    private String hello;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geocoder = new Geocoder(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geofenceHelper = new GeofenceHelper(this);
        add5kmBtn = findViewById(R.id.btn5km);
        add10kmBtn = findViewById(R.id.btn10km);
        btnRemove = findViewById(R.id.btnRemove);
        userInputForGeofenceSize = findViewById(R.id.geofenceEditSizeTxt);
        userInputForGeofenceSize.setHintTextColor(getResources().getColor(R.color.white));


        add5kmGeofence();
        removeGeofence();
        add10kmGeofence();


        businessList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("details");
        circleOptions = new CircleOptions();

        btnRemove.setClickable(false);


    }


    public void add5kmGeofence() {

        add5kmBtn.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Need to enable location permissions", Toast.LENGTH_SHORT).show();
                return;
            }
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    mMap.clear();
                    userInput = userInputForGeofenceSize.getText().toString();
                    Toast.makeText(getApplicationContext(), "" + userInput +"km Geofence Created", Toast.LENGTH_SHORT).show();
                    geofenceRadiusSize = Integer.parseInt(userInput);
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    addGeofence(userLocation, geofenceRadiusSize * 1000);
                    display5kmBusiness();
                    btnRemove.setClickable(true);

                    // permissions check
                    if (Build.VERSION.SDK_INT >= 29) {
                        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                            } else {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                            }
                        }

                    } else {

                    }

                }
            });
//            Toast.makeText(getApplicationContext(), "" + userInput +"km Geofence Created", Toast.LENGTH_SHORT).show();
        });
    }

    public int setGeofenceRadiusCircle(int radius) {
        radius = geofenceRadiusSize;
        return radius;
    }


    public void add10kmGeofence() {

        add10kmBtn.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Need to enable location permissions", Toast.LENGTH_SHORT).show();
                return;
            }
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    mMap.clear();
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    addGeofence(userLocation, GEOFENCE10KM);
                    display10kmBusiness();

                    // permissions check
                    if (Build.VERSION.SDK_INT >= 29) {
                        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                            } else {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                            }
                        }

                    }

                }
            });
            Toast.makeText(getApplicationContext(), "10km Geofence Created", Toast.LENGTH_SHORT).show();
//            add10kmBtn.setVisibility(view.INVISIBLE);
//            add5kmBtn.setVisibility(view.INVISIBLE);
        });
    }


    private void removeGeo() {

        geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        pendingIntent = geofenceHelper.getPendingIntent();


        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent()).addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (geofencingClient != null){
                    removeCircle();
                    homeMarker.remove();
                    setUpBusinessInfoFromDB();
                    Toast.makeText(getApplicationContext(), "Geofence waa removed", Toast.LENGTH_SHORT).show();


                }
                else{
                    Toast.makeText(getApplicationContext(), "No Geofence created", Toast.LENGTH_SHORT).show();

                }



            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed To Remove Geofence", Toast.LENGTH_SHORT).show();
            }
        });
        ;

    }

    public void removeGeofence() {

        btnRemove.setOnClickListener(view -> {
            removeGeo();
//            add10kmBtn.setVisibility(view.VISIBLE);
//            add5kmBtn.setVisibility(view.VISIBLE);
        });

    }

    private void addGeofence(LatLng latLng, float radius) {

        geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        pendingIntent = geofenceHelper.getPendingIntent();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Geofence was added", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errMessage = geofenceHelper.getErrorString(e);
                        Toast.makeText(getApplicationContext(), "Geofence failed", Toast.LENGTH_SHORT).show();


                    }
                });
    }


    public double distanceBetweenUsingSphericalUtil(LatLng from, LatLng too) {
        Double distance = SphericalUtil.computeDistanceBetween(from, too);
        return distance.shortValue();
    }


    public boolean showMarkersIfIn10KmArea(LatLng from, LatLng too) {
        Double distance = SphericalUtil.computeDistanceBetween(from, too);
        String userInput = userInputForGeofenceSize.getText().toString();
        geofenceRadiusSize = Integer.parseInt(userInput);
        int geo =  setGeofenceRadiusCircle(geofenceRadiusSize);
        if (distance.shortValue() / 1000 > 10) {
            // Toast.makeText(getApplicationContext(), "distance is == " + distance / 1000 + "km", Toast.LENGTH_SHORT).show();
            businessMarker.remove();
            addUserHomeMarker(userLocation);
            addCircle(userLocation, geofenceRadiusSize);
            return true;
        } else {
            return false;
        }

    }

    public boolean showMarkersIfIn5KmArea(LatLng from, LatLng too) {

        Double distance = SphericalUtil.computeDistanceBetween(from, too);
        geofenceRadiusSize = Integer.parseInt(userInput);
        if (distance.shortValue() / 1000 > 5) {
            // Toast.makeText(getApplicationContext(), "distance is == " + distance / 1000 + "km", Toast.LENGTH_SHORT).show();
            businessMarker.remove();
            addUserHomeMarker(userLocation);
            addCircle(userLocation, geofenceRadiusSize * 1000);
            return true;
        } else {
            return false;
        }
    }

    public void setMarkerColors(Pubs pubs) {

        if ((pubs.getOccupancy() * 100) / (pubs.getCapacity()) <= 60) {
            businessMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if ((pubs.getOccupancy() * 100) / (pubs.getCapacity()) > 60 && (pubs.getOccupancy() * 100) / (pubs.getCapacity()) <= 90) {
            businessMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        } else
            businessMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    }

    public void display5kmBusiness() {
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
                        showMarkersIfIn5KmArea(userLocation, businessLocation);

                        if (mMap != null) {
                            availability = pubs.getCapacity() - pubs.getOccupancy();
                            businessMarker = mMap.addMarker(new MarkerOptions()
                                    .position(businessLocation)
                                    .title(pubs.getDescription())
                                    .snippet(" Capacity " + pubs.getCapacity() + " Occupied " + pubs.getOccupancy() + " availability" + availability));
                            setMarkerColors(pubs);
                        }
                        showMarkersIfIn5KmArea(userLocation, businessLocation);
                    }
                }
                Log.d(TAG, "ArrayList details are =>: " + businessList.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        enableUserLocation();
    }


    public void display10kmBusiness() {

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
                        showMarkersIfIn10KmArea(userLocation, businessLocation);

                        if (mMap != null) {
                            businessMarker = mMap.addMarker(new MarkerOptions()
                                    .position(businessLocation)
                                    .title(pubs.getDescription())
                                    .snippet(" Capacity " + pubs.getCapacity() + " Occupied " + pubs.getOccupancy()));
                            setMarkerColors(pubs);
                        }
                        showMarkersIfIn10KmArea(userLocation, businessLocation);

                    }
                }
                Log.d(TAG, "ArrayList details are =>: " + businessList.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        enableUserLocation();


    }


    public void setUpBusinessInfoFromDB() {

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
                        if (mMap != null) {
                            availability = pubs.getCapacity() - pubs.getOccupancy();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpBusinessInfoFromDB();

    }


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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
            } else {
                //We do not have the permission..

            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();
            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is neccessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void addUserHomeMarker(LatLng latLng) {
        homeMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Home"));
        homeMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


    }


    private void addCircle(LatLng latLng, int radius) {
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mapCircle = mMap.addCircle(circleOptions);
    }

    private void removeCircle() {
        if (mapCircle != null) {
            mapCircle.remove();
        }
    }
}