package com.example.gpstracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_FINE_LOCATION = 101;
    private static final int PERMISSIONS_COARSE_LOCATION = 102;
    private static final int PERMISSIONS_BACKGROUND_LOCATION = 101;
    public LocationCallback locationCallBack;
    private TextToSpeech textToSpeech;

    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address;
    SwitchCompat sw_locationupdates, sw_gps;

    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setPitch(0.8f);
                    textToSpeech.setSpeechRate(0.8f);
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    }
                } else {
                    Log.e("TTS", "Language not success");
                }
            }
        });

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(30000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setNumUpdates(1)
                .setMaxWaitTime(100);

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    String speakText = "Using GPS sensor with High Accuracy";
                    tv_sensor.setText("Using GPS sensor");
                    textToSpeech.speak(speakText, TextToSpeech.QUEUE_ADD, null, null);
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    String speakText = "Using towers and wifi with balanced power accuracy";
                    tv_sensor.setText("Using Towers and Wifi");
                    textToSpeech.speak(speakText, TextToSpeech.QUEUE_ADD, null, null);
                }
            }
        });

        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_locationupdates.isChecked()) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        });

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                updateUIValues(location);
                Toast.makeText(MainActivity.this,"Location updating", Toast.LENGTH_SHORT).show();
                //super.onLocationResult(locationResult);
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        updateGPS();
    }

    private void stopLocationUpdates() {
        String text = "Stopping Tracking";
        Toast.makeText(MainActivity.this, "Stopping tracking", Toast.LENGTH_SHORT).show();
        tv_updates.setText(text);
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    private void startLocationUpdates() {
        String text = "Starting Tracking";
        tv_updates.setText(text);
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Permissions granted\nStarting tracking", Toast.LENGTH_SHORT).show();

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUIValues(location);
                }
            });
        }
        else{
            Toast.makeText(MainActivity.this, "Permissions not granted\nStarting asking for permissions", Toast.LENGTH_SHORT).show();
            updateGPS();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void updateGPS(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{"Manifest.permission.ACCESS_FINE_LOCATION"},PERMISSIONS_FINE_LOCATION);

        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                                                                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{"Manifest.permission.ACCESS_FINE_LOCATION"},PERMISSIONS_FINE_LOCATION);

        }
        /*if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{"Manifest.permission.ACCESS_COARSE_LOCATION"},PERMISSIONS_COARSE_LOCATION);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{"Manifest.permission.ACCESS_BACKGROUND_LOCATION"},PERMISSIONS_BACKGROUND_LOCATION);
        }*/
    }

    private void updateUIValues(Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));
        Toast.makeText(MainActivity.this, "Updating UI values", Toast.LENGTH_SHORT).show();
        String speakText = "Current location at Latitude "+location.getLatitude();
        speakText += " And longitude "+location.getLongitude();
        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
            speakText += " with an elevation of "+location.getAltitude();
        }
        else{
            tv_altitude.setText("Altitude Unavaiable");
        }
        speakText += " Measured with accuracy "+location.getAccuracy();
        textToSpeech.speak(speakText, TextToSpeech.QUEUE_ADD, null, null);
        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
            speakText = "Travelling with speed of "+location.getSpeed();
            textToSpeech.speak(speakText, TextToSpeech.QUEUE_ADD, null, null);
        }
        else{
            tv_speed.setText("Speed Unavailable");
        }
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e){
            tv_address.setText("Unable to get address");
        }
    }


    @Override
    protected void onDestroy() {
        if(textToSpeech!=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_FINE_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startLocationUpdates();
            else {
                Toast.makeText(MainActivity.this, "No permission", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"Manifest.permission.ACCESS_FINE_LOCATION"}, PERMISSIONS_FINE_LOCATION);
            }

        }
        /*if(requestCode == PERMISSIONS_COARSE_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startLocationUpdates();
            else
                startActivity(new Intent(MainActivity.this, MainActivity.class));
        }*/
        /*if(requestCode == PERMISSIONS_BACKGROUND_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startLocationUpdates();
            else
                startActivity(new Intent(MainActivity.this, MainActivity.class));
        }*/
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}