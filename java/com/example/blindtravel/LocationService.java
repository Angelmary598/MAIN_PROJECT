package com.example.blindtravel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationService extends Service {

    private String BASE_URL = "http://192.168.100.15:5000";

    private String role = "Client";
    private String id;
    private String email;
    private String dest;

    Call<String> userList;

    TextToSpeech prompt;

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d("Location Service",locationResult.getLastLocation().toString());
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                Log.d("Location Service ", latitude + "," + longitude);
                setLocationApi(String.valueOf(latitude),String.valueOf(longitude));

                closestConductor(latitude,longitude);
            }
        }
    };

    private void closestConductor(double lat, double lon) {
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        if(role.equals("Client") || role.equals("client")) {
            userList = apiInterface.getConductors(dest,email);
        }
        else {
            userList = apiInterface.getClients(dest,email);
        }
        userList.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.code()==200) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body());
                        JSONArray fetched = (JSONArray) jsonObject.get("result");
                        if(!fetched.isNull(0)) {
                            Log.v("Test Debug",String.valueOf(fetched.length()));
                            for(int j=0;j< fetched.length();++j) {
                                JSONObject test = fetched.getJSONObject(j);
                                double dist = distance(lat, lon, Double.parseDouble(test.getString("latitude")), Double.parseDouble(test.getString("longitude")),0,0);
                                if(dist<1000) {
                                    speechInit(test.getString("name"));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void speechInit(String name) {
        prompt = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                prompt.speak("There's a user named "+name+" coming close to you....",TextToSpeech.QUEUE_ADD,null);
                Log.d("Home Speaker","Initialised");
            }
        });
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    private void setLocationApi(String lat, String lon) {
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        Log.d("Initiate Location Service","API Set");
        Call<String> apiInstance;
        if(role.equals("Client") || role.equals("client")) {
            apiInstance = apiInterface.updateClientLoc(email, lat, lon);
        }
        else {
            apiInstance = apiInterface.updateConductorLoc(email, lat, lon);
        }
        apiInstance.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Toast.makeText(getApplicationContext(),response.body(),Toast.LENGTH_SHORT).show();
                Log.d("Location Updated",response.body().toString());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
                Log.d("Location Updation Error",t.getMessage().toString());
            }
        });

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not Yet Implemented....");
    }

    @SuppressLint("MissingPermission")
    private void StartLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        Log.d("Initiate Location Service","Preparing");
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Helen Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running....");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        Log.d("Initiate Location Service","Ready");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Helen Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by Helen for getting live Locations....");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(30000);
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(175,builder.build());
    }

    private void StopLocationService() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        id = intent.getStringExtra("id");
        role = intent.getStringExtra("role");
        email = intent.getStringExtra("email");
        dest = intent.getStringExtra("destination");
        Log.d("Location Service Onstart",email+" : "+role+" : "+intent.toString());

        if(intent!=null) {
            String action = intent.getAction();
            if(action!=null) {
                if(action.equals("startLocationService")) {
                    Log.d("Initiate Location Service","Begin");
                    StartLocationService();
                } else if(action.equals("stopLocationService")) {
                    Log.d("Stop Location Service","Begin");
                    StopLocationService();
                }
            }
        }
        else {
            Log.d("Initiate Location Service","Failed");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
