package com.gvvp.roadcrackdetector;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gvvp.roadcrackdetector.env.Logger;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RelativeLayout detectcrackbtn, showmapbtn, viewstatsbtn, editprofilebtn;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.25f;
    public static final int PRIORITY_HIGH_ACCURACY = 100;

    private ImageView menuProfile;
    private TextView menuName, menuEmail;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    public static double latitude;
    public static double longitude;
    private static final Logger LOGGER = new Logger();
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        detectcrackbtn = findViewById(R.id.roadcrackbutton);
        showmapbtn = findViewById(R.id.mapcrackbutton);
        viewstatsbtn = findViewById(R.id.crackstatsbutton);
        editprofilebtn = findViewById(R.id.editprofilebutton);

        mAuth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        navigationView.bringToFront();
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        MenuItem logout_btn = menu.findItem(R.id.nav_logout);
        MenuItem menu_ep_btn = menu.findItem(R.id.nav_editprofile);
        MenuItem menu_dc_btn = menu.findItem(R.id.nav_detectcracks);
        MenuItem menu_sm_btn = menu.findItem(R.id.nav_showmaps);
        MenuItem menu_cs_btn = menu.findItem(R.id.nav_crackstats);
        MenuItem menu_share_btn = menu.findItem(R.id.nav_share);
        MenuItem menu_about_btn = menu.findItem(R.id.nav_about);

        menuProfile = headerView.findViewById(R.id.nav_profile_image);
        menuName = headerView.findViewById(R.id.nav_fullname);
        menuEmail = headerView.findViewById(R.id.nav_email);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Users").document(uid);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("fullname");
                    String email = documentSnapshot.getString("email");
                    String imageUrl = documentSnapshot.getString("image");
                    //do nothing
                    if(imageUrl==null) {
                        Toast.makeText(MainActivity.this, "No Profile Picture Found",Toast.LENGTH_SHORT).show();
                        //do nothing
                    }
                    else{
                        Glide.with(MainActivity.this)
                                .load(imageUrl)
                                .into(menuProfile);
                    }
                    // Set the retrieved data in EditText fields
                    menuName.setText(name);
                    menuEmail.setText(email);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
        menu_ep_btn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(intent);
                return true;
            }
        });

        menu_dc_btn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                startActivity(intent);
                return true;
            }
        });

        menu_sm_btn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
                return true;
            }
        });

        menu_cs_btn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                Intent intent = new Intent(getApplicationContext(), StatsActivity.class);
                startActivity(intent);
                return true;
            }
        });

        menu_about_btn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                openDialog();
                return true;
            }

            private void openDialog() {
                AboutUsDialog aboutUsDialog = new AboutUsDialog();
                aboutUsDialog.show(getSupportFragmentManager(), "About Us Dialog");
            }
        });

        menu_share_btn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Share Link","https://github.com/Lucifer7535/Road-Crack-Detector");
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(MainActivity.this, "Link Copied to Clipboard", Toast.LENGTH_LONG).show();
                return true;
            }
        });
        logout_btn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                mAuth.signOut();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                Toast.makeText(MainActivity.this, "Successfully logged out!", Toast.LENGTH_LONG).show();
                return true;
            }
        });
        editprofilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });
        viewstatsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StatsActivity.class);
                startActivity(intent);
            }
        });
        showmapbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });
        detectcrackbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                startActivity(intent);
            }
        });
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                MainActivity.this
        );
        this.getLoc();
    }

    public void getLoc() {
        if(ActivityCompat.checkSelfPermission(MainActivity.this
                , android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this
                , android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this
                    , new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
                            , android.Manifest.permission.ACCESS_COARSE_LOCATION}
                    , 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            getCurrentLocation();
        } else {
            Toast.makeText(getApplicationContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

//    public void getCurrentLocation() {
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // Request location permission
//                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
//                return;
//            }
//
//            fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null).addOnCompleteListener(new OnCompleteListener<Location>() {
//                @Override
//                public void onComplete(@NonNull Task<Location> task) {
//                    Location location = task.getResult();
//
//                    if (location != null) {
//                        LOGGER.i("*************" + location.getLatitude());
//                        LOGGER.i("*************" + location.getLongitude());
//                        longitude = location.getLongitude();
//                        latitude = location.getLatitude();
//                    } else {
//                        LocationRequest locationRequest = new LocationRequest()
//                                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
//                                .setInterval(100)
//                                .setFastestInterval(10)
//                                .setNumUpdates(3);
//
//                        LocationCallback locationCallback = new LocationCallback() {
//                            @Override
//                            public void onLocationResult(@NonNull LocationResult locationResult) {
//                                Location location1 = locationResult.getLastLocation();
//                                LOGGER.i("*************" + location1.getLatitude());
//                                LOGGER.i("*************" + location1.getLongitude());
//                                longitude = location1.getLongitude();
//                                latitude = location1.getLatitude();
//                            }
//                        };
//
//                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                                && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                            return;
//                        }
//                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
//                    }
//                }
//            });
//        } else {
//            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//        }
//    }

    public void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request location permission
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }

            fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null).addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();

                    if (location != null) {
                        LOGGER.i("*************" + location.getLatitude());
                        LOGGER.i("*************" + location.getLongitude());
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    } else {
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                                .setInterval(100)
                                .setFastestInterval(10)
                                .setNumUpdates(3);

                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                Location location1 = locationResult.getLastLocation();
                                LOGGER.i("*************" + location1.getLatitude());
                                LOGGER.i("*************" + location1.getLongitude());
                                longitude = location1.getLongitude();
                                latitude = location1.getLatitude();
                            }
                        };

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                }
            });
        } else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }
}