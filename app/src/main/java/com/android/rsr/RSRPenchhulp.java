package com.android.rsr;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.rsr.utils.GPSTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RSRPenchhulp extends AppCompatActivity implements OnMapReadyCallback {

    private double latitude;
    private double longitude;
    private static final int REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rsrpenchhulp);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final Button RSRPEnchhulp = findViewById(R.id.RSRP_main_btn);
        ImageView clock = findViewById(R.id.clockTimer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_arrow);
        String[] Permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (!hasPermissions(this, Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, REQUEST);
        }
        if (!isTablet()) {
            RSRPEnchhulp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RSRPenchhulp.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.custom_layout, null);
                    // Set the custom layout as alert dialog view
                    builder.setView(dialogView);
                    // Create the alert dialog
                    final AlertDialog dialog = builder.create();
                    //Make dialog show at bottom
                    Window window = dialog.getWindow();
                    WindowManager.LayoutParams wlp = window.getAttributes();
                    wlp.gravity = Gravity.BOTTOM;
                    wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                    //Set background color transparent
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    window.setAttributes(wlp);
                    // Get the custom alert dialog view widgets reference
                    Button btn_call = dialogView.findViewById(R.id.call_btn);
                    TextView btn_close = dialogView.findViewById(R.id.close_btn);
                    // Set positive/yes button click listener
                    btn_call.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:+319007788990"));
                            if (ContextCompat.checkSelfPermission(RSRPenchhulp.this,
                                    Manifest.permission.CALL_PHONE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(RSRPenchhulp.this,
                                        new String[]{Manifest.permission.CALL_PHONE},
                                        REQUEST);
                            } else {
                                //You already have permission
                                try {
                                    startActivity(callIntent);
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    dialog.show();
                    dialog.setCancelable(false);
                    btn_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                }
            });
        }

        if (isGPSConnected(RSRPenchhulp.this)) {
            GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
            Location mLocation = gpsTracker.getLocation();
            if (mLocation != null) {
                latitude = mLocation.getLatitude();
                longitude = mLocation.getLongitude();
            } else {
                clock.setVisibility(View.VISIBLE);
            }
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        clock.setVisibility(View.INVISIBLE);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:+319007788990"));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(callIntent);
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        String fullAddress = null;
        BitmapDescriptor marker = BitmapDescriptorFactory.fromResource(R.drawable.map_marker);
        Geocoder geocoder;
        List<Address> addresses;

        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses.size() != 0) {
                String streetName = addresses.get(0).getThoroughfare();
                String houseNumber = addresses.get(0).getSubThoroughfare();
                String postalCode = addresses.get(0).getPostalCode();
                String city = addresses.get(0).getLocality();
                fullAddress = streetName + " " + houseNumber + ", " + postalCode + ", " + city;
            }

            LatLng loc = new LatLng(latitude, longitude);
            // Add a marker in our Location and move the camera
            Marker myMarker = googleMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .title("Uw Locatie:")
                    .snippet("\n\n" + fullAddress + "\n\n" + "Onthoud deze locatie voor het telefoongesprek.")
                    .icon(marker));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    LayoutInflater inflater;
                    inflater = (LayoutInflater)
                            getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    //Creating custom layout for snippet
                    View v = inflater.inflate(R.layout.custom_snippet, null);
                    TextView snippetText = v.findViewById(R.id.snippetText);
                    TextView snippetTitle = v.findViewById(R.id.snippetTitle);
                    snippetText.setTextColor(Color.WHITE);
                    snippetText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snippetTitle.setTextColor(Color.WHITE);
                    snippetTitle.setTypeface(null, Typeface.BOLD);
                    snippetTitle.setTextSize(20);
                    snippetText.setText(arg0.getSnippet());
                    snippetTitle.setText(arg0.getTitle());
                    return v;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    return null;
                }
            });
            myMarker.showInfoWindow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //Checking if internet connection is enabled (Wifi or Data)
    private boolean isInternetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return (mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting());
        } else
            return false;
    }

    private boolean isGPSConnected(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //Alert dialog for internet connection
    private void alertNetStatus(final Context c) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Geen internetverbiding")
                //builder.setView(R.layout.custom_alert);
                .setMessage("Er is geen verbinding mogelijk met het internet. Hierdoor kunnen" +
                        "uw locatiegegevens niet worden opgehaald.")
                .setPositiveButton("Probeer opnieuw", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isInternetConnected(c))
                            builder.show();
                    }
                })
                .setNegativeButton("Annuleren", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    //Alert dialog for GPS status
    private void alertGPSstatus(final Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Locatieservices uitgeschakeld")
                .setMessage("RSR penchhulp heeft toegang tot uw locatie nodig. " +
                        "Schakel locatietoegang in")
                .setPositiveButton("Annuleren", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("Ga nar instellingen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Checking if device is Tablet
    private boolean isTablet() {
        boolean tablet;
        View v = findViewById(R.id.rsrp_layout_large);
        tablet = v != null;
        return tablet;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGPSConnected(RSRPenchhulp.this))
            alertGPSstatus(RSRPenchhulp.this);
        if (!isInternetConnected(RSRPenchhulp.this))
            alertNetStatus(RSRPenchhulp.this);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return true;
    }
}
