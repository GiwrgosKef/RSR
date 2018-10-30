package com.android.rsr;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST= 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (isTablet()) {
            Button OverRSR = findViewById(R.id.btnOverRSR);
            OverRSR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), OverRSR.class));
                }
            });
        }

        Button RSRPEnchhulp = findViewById(R.id.btnRSR);
        //Check if Internet is enabled and show alert if not
        if (!isInternetConnected(MainActivity.this)) alertNetStatus(MainActivity.this);

        RSRPEnchhulp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] Permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            Permissions,
                            REQUEST);
                } else{
                    try {
                        startActivity(new Intent(getApplicationContext(), RSRPenchhulp.class));
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] Permissions, @NonNull int[] Permissions_all) {
        super.onRequestPermissionsResult(requestCode, Permissions, Permissions_all);
        switch (requestCode) {
            case REQUEST: {
                if (Permissions_all.length > 0 && Permissions_all[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), RSRPenchhulp.class));
                }
            }
        }
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
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isGPSEnabled;
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
                        startActivityForResult(new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                    }
                })
                .setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Checking if device is Tablet
    private boolean isTablet() {
        boolean tablet;
        View v = findViewById(R.id.layout_large);
        tablet = v != null;
        return tablet;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGPSConnected(MainActivity.this))
            alertGPSstatus(MainActivity.this);
        if (!isInternetConnected(MainActivity.this))
            alertNetStatus(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isTablet())
            getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item1) {
            Intent homeIntent = new Intent(this, OverRSR.class);
            startActivity(homeIntent);
        }
        return super.onOptionsItemSelected(item);
    }
}