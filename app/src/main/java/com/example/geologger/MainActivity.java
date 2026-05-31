package com.example.geologger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOC = 100;

    private TextView tvLatDisplay, tvLonDisplay, tvStatus;
    private RequestQueue requestQueue;
    private LocationManager locationManager;

    private final String insertUrl = "http://10.0.2.2/localisation/createPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLatDisplay = findViewById(R.id.tv_lat_display);
        tvLonDisplay = findViewById(R.id.tv_lon_display);
        tvStatus = findViewById(R.id.tv_status);
        Button btnViewMap = findViewById(R.id.btn_view_map);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnViewMap.setOnClickListener(v ->
                startActivity(new Intent(this, MapsActivity.class)));

        askLocationPermissionAndStart();
    }

    private void askLocationPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOC);
        } else {
            startGpsUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startGpsUpdates() {
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                60000,
                150,
                new LocationListener() {

                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        double alt = location.getAltitude();
                        float acc = location.getAccuracy();

                        tvLatDisplay.setText("Latitude : " + lat);
                        tvLonDisplay.setText("Longitude : " + lon);
                        tvStatus.setText("Dernière mise à jour : " +
                                new SimpleDateFormat("HH:mm:ss",
                                        Locale.getDefault()).format(new Date()));

                        String msg = getString(R.string.new_location,
                                lat, lon, alt, acc);
                        Toast.makeText(getApplicationContext(),
                                msg, Toast.LENGTH_LONG).show();

                        sendPosition(lat, lon);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        String newStatus;
                        switch (status) {
                            case LocationProvider.OUT_OF_SERVICE:
                                newStatus = "OUT_OF_SERVICE"; break;
                            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                                newStatus = "TEMPORARILY_UNAVAILABLE"; break;
                            case LocationProvider.AVAILABLE:
                                newStatus = "AVAILABLE"; break;
                            default: newStatus = "UNKNOWN";
                        }
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.provider_new_status, provider, newStatus),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderEnabled(@NonNull String provider) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.provider_enabled, provider),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.provider_disabled, provider),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void sendPosition(final double lat, final double lon) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                insertUrl,
                response -> tvStatus.setText("Position envoyée ✓"),
                error -> tvStatus.setText("Erreur réseau")
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date", sdf.format(new Date()));
                params.put("imei", getDeviceIdentifier());
                return params;
            }
        };
        requestQueue.add(request);
    }

    private String getDeviceIdentifier() {
        String androidId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null && !androidId.trim().isEmpty()) return androidId;

        try {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tm = (TelephonyManager)
                        getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    String id = tm.getDeviceId();
                    if (id != null && !id.trim().isEmpty()) return id;
                }
            }
        } catch (Exception ignored) {}

        return "UNKNOWN_DEVICE";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGpsUpdates();
        } else {
            Toast.makeText(this,
                    getString(R.string.permission_denied),
                    Toast.LENGTH_LONG).show();
        }
    }
}