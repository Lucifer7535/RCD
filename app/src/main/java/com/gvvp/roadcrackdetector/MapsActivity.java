package com.gvvp.roadcrackdetector;

import static android.graphics.BitmapFactory.decodeByteArray;
import static androidx.constraintlayout.widget.Constraints.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.gvvp.roadcrackdetector.databinding.ActivityMapsBinding;
import com.gvvp.roadcrackdetector.env.Logger;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private List<Marker> markerList;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;

    private static final Logger LOGGER = new Logger();

    private BottomSheetBehavior bottomSheetBehavior;
    private View bottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SearchView searchView = findViewById(R.id.search_bar);

        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(100);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMarkers(newText);
                return true;
            }
        });
    }

    class SelectedMarkerHolder {
        Marker selectedMarker = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        markerList = new ArrayList<>();
        mMap = googleMap;
        final SelectedMarkerHolder selectedMarkerHolder = new SelectedMarkerHolder();
        final BitmapDescriptor defaultMarkerIcon = BitmapDescriptorFactory.defaultMarker();
        final BitmapDescriptor selectedMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.selected_marker);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        final String uid = currentUser.getUid();
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                lastKnownLocation = location;
                LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");

        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot userDocument : task.getResult()) {
                        DocumentReference userRef = usersRef.document(userDocument.getId());
                        CollectionReference locationsRef = userRef.collection("locations");

                        locationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot locationDocument : task.getResult()) {
                                        String label = locationDocument.getString("Label");
                                        double latitude = locationDocument.getDouble("Latitude");
                                        double longitude = locationDocument.getDouble("Longitude");

                                        String addressLine = locationDocument.getString("Address Line");
                                        double confidence = locationDocument.getDouble("Confidence");
                                        String image = locationDocument.getString("Image");
                                        String locality = locationDocument.getString("Locality");
                                        String postalCode = locationDocument.getString("Postal Code");
                                        String timeStamp = locationDocument.getString("TimeStamp");
                                        String desc = addressLine + "\n" + timeStamp;
                                        //Bitmap markerimage = decodeBase64ToBitmap(image);

                                        LatLng location = new LatLng(latitude, longitude);

                                        MarkerOptions markerOptions = new MarkerOptions()
                                                .position(location)
                                                .title(label)
                                                .snippet(desc);

                                        Marker newmarker = mMap.addMarker(markerOptions);
                                        newmarker.setTag(image);
                                        newmarker.setZIndex((float) confidence);
                                        markerList.add(newmarker);
                                    }
/*
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    for (Marker marker : markerList) {
                                        builder.include(marker.getPosition());
                                    }
                                    LatLngBounds bounds = builder.build();
                                    if(bounds==null){
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(MapsActivity.this, "Loading Locations, Try after some time", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                                    }*/
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });



        //marker click listeners
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                updateBottomSheetContent(marker);
                if (selectedMarkerHolder.selectedMarker != null) {
                    selectedMarkerHolder.selectedMarker.setIcon(defaultMarkerIcon);
                }
                // Select the clicked marker
                marker.setIcon(selectedMarkerIcon);
                selectedMarkerHolder.selectedMarker = marker;
                return true;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                if (selectedMarkerHolder.selectedMarker != null) {
                    selectedMarkerHolder.selectedMarker.setIcon(defaultMarkerIcon);
                    selectedMarkerHolder.selectedMarker = null;
                }
            }
        });

    }

    private void filterMarkers(String query) {
        for (Marker marker : markerList) {
            String title = marker.getTitle();
            if (title.toLowerCase().contains(query.toLowerCase())) {
                marker.setVisible(true);
            } else {
                marker.setVisible(false);
            }
        }
    }

    //method to set view in bottom sheet
    private void updateBottomSheetContent(Marker marker) {
        String imageString = (String) marker.getTag();
        ImageView imageView = bottomSheet.findViewById(R.id.marker_image);
        byte[] imageBytes = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap crackimage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        if (imageString != null && !imageString.isEmpty()) {
            Bitmap image = crackimage;
            if (image != null) {
                imageView.setImageBitmap(image);
            }
        }
        String title = marker.getTitle();
        TextView tvTitle = bottomSheet.findViewById(R.id.marker_title);
        if (!title.equals("")) {
            tvTitle.setText(title);
        }
        String snippet = marker.getSnippet();
        TextView tvSnippet = bottomSheet.findViewById(R.id.marker_desc);
        String subsnippet = snippet.substring(0,snippet.indexOf("2023"));
        if (!snippet.equals("")) {
            tvSnippet.setText("Location: " +subsnippet);
        }
        String timestamp = marker.getSnippet();
        TextView tvTimestamp = bottomSheet.findViewById(R.id.marker_ts);
        String subtimestamp = timestamp.substring(timestamp.indexOf("2023"));
        if (!subtimestamp.equals("")) {
            tvTimestamp.setText(subtimestamp);
        }
        Float conf = marker.getZIndex();
        String confidence = conf.toString();
        TextView tvConf = bottomSheet.findViewById(R.id.marker_conf);
        if(conf != null){
            tvConf.setText(", Confidence:"+confidence);
        }
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}