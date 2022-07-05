package com.example.blindtravel;

import static android.util.Config.DEBUG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blindtravel.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class home extends AppCompatActivity {

    private String BASE_URL = "http://192.168.100.15:5000";

    private SwipeRefreshLayout swipe_layout;
    private RecyclerView clientList;
    private EditText destination;
    private Button profile;

    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private static final int REQUEST_CODE_SPEECH_INPUT_VIVA = 4;
    private static final int REQUEST_CODE_LOCATION_SERVICE = 2;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_FINE_PERMISSION_CODE = 102;
    private static final int MY_COARSE_PERMISSION_CODE = 104;
    int PERMISSION_ID = 44;

    String id = null;
    String role = "Client";
    String email_glob = "";
    Call<String> apiInstance;
    ApiInterface apiInterface;
    JSONArray fetched = null;
    private int mBindFlag;

    TextToSpeech prompt;
    private TextToSpeech myTTS;

    private View popupView;
    int width;
    int height;
    private boolean focusable;
    PopupWindow popupWindow;
    private TextView chat_user;
    private TextView chat_system;
    private AudioManager audioManager;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setTitle("Home Page");

        requestPermission();

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    home.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    2
            );
        }
        else {
            startLocationService();
        }

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        role = intent.getStringExtra("role");
        email_glob = intent.getStringExtra("email");
        Log.d("Home Page Intent Vals",id+" : "+role+" : "+email_glob);
        if(id==null) {
            Log.d("Test Shared Prefs Home",id+" : "+role+" : "+email_glob);
        }

        Intent service = new Intent(home.this,VoiceCommandService.class);
        service.putExtra("id",id);
        service.putExtra("role",role);
        service.putExtra("email",email_glob);
        getApplicationContext().startService(service);
        mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;



        init();
        onClickFunctions();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==REQUEST_CODE_LOCATION_SERVICE && grantResults.length>0) {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            }
            else {
                Toast.makeText(home.this,"Permission Denied....",Toast.LENGTH_SHORT).show();
            }
        }
       /* if(requestCode==5){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(home.this,"Phone permission granted...", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(home.this," permission denied...", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode==6){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(home.this," permission granted...", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(home.this," permission denied...", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode==7){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(home.this," permission granted...", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(home.this," permission denied...", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode==8){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(home.this," permission granted...", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(home.this," permission denied...", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode==9){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(home.this," permission granted...", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(home.this," permission denied...", Toast.LENGTH_SHORT).show();
            }
        }*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopLocationService();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && (role.equals("Client") || role.equals("client"))){
            initializeTextToSpeech();
            promptSpeechInput();
            View view = LayoutInflater.from(home.this).inflate(R.layout.popup_window,null);
            initPopupWindow();

            show_popup(view);
            chat_system.setText(" ");
            chat_user.setText(" ");
//            myTTS.stop();
            Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vb.vibrate(200);
        }
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP && (role.equals("Client") || role.equals("client"))) {
            if(role.equals("Client") || role.equals("client")) {
                speechToText();
                //initializeTextToSpeech();
            }
        }
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},1);
      // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},6);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},7);
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_FINE_PERMISSION_CODE);
        }
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_COARSE_PERMISSION_CODE);
        }
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},8);
       // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH},9);
    }

    public void initializeTextToSpeech() {
        myTTS = new TextToSpeech(this, i -> {
            if(myTTS.getEngines().size()==0){
                Toast.makeText(home.this,"Helen engine not on this device",Toast.LENGTH_LONG).show();
                finish();
            } else {
                myTTS.setLanguage(Locale.UK);

                Calendar c = Calendar.getInstance();
                int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

                if(timeOfDay >= 0 && timeOfDay < 12){
                    speak("Good Morning.     ");
                    chat_system.setText("Good Morning");
                }else if(timeOfDay >= 12 && timeOfDay < 16){
                    speak("Good Afternoon.      ");
                    chat_system.setText("Good Afternoon");
                }else if(timeOfDay >= 16 && timeOfDay < 24){
                    speak("Good Evening.      ");
                    chat_system.setText("Good Evening");
                }
            }
        });
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager!=null) {
            for(ActivityManager.RunningServiceInfo service:activityManager.getRunningServices(Integer.MAX_VALUE)){
                if(LocationService.class.getName().equals(service.service.getClassName())) {
                    if(service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService() {
        if(!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.putExtra("role",role);
            intent.putExtra("email",email_glob);
            intent.putExtra("id",id);

            intent.setAction("startLocationService");
            startService(intent);
            Toast.makeText(this,"Location Service Started....",Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService() {
        if(isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.setAction("stopLocationService");
            intent.putExtra("id",id);
            intent.putExtra("role",role);
            startService(intent);
            Toast.makeText(this,"Location Service Stopped....",Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        clientList = findViewById(R.id.user_list_home);
        destination = findViewById(R.id.destination_entry_home);
        profile = findViewById(R.id.profile_home);
        swipe_layout = findViewById(R.id.home_layout);
    }

    private void initPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_window, null);
        width = LinearLayout.LayoutParams.MATCH_PARENT;
        height = LinearLayout.LayoutParams.WRAP_CONTENT;
        focusable = true;
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.setFocusable(focusable);

        chat_system = popupView.findViewById(R.id.chat_system);
        chat_user = popupView.findViewById(R.id.chat_user);

        chat_system.setVisibility(View.VISIBLE);
        chat_user.setVisibility(View.VISIBLE);
        Toast.makeText(home.this,"Test toast",Toast.LENGTH_SHORT).show();
        //findViewById(R.id.imageView3).setVisibility(View.VISIBLE);
        //findViewById(R.id.imageView4).setVisibility(View.VISIBLE);
        Toast.makeText(home.this,"Test toast",Toast.LENGTH_SHORT).show();
    }

    private void show_popup(View view) {
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                chat_system.setVisibility(View.GONE);
                chat_user.setVisibility(View.GONE);
                findViewById(R.id.imageView3).setVisibility(View.GONE);
                findViewById(R.id.imageView4).setVisibility(View.GONE);
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private void setUserList() {
        UserListAdapter adaptor = new UserListAdapter(fetched,getApplicationContext(),role);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(home.this);
        clientList.setLayoutManager(layoutManager);
        clientList.setAdapter(adaptor);
    }

    private void speechToText() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        prompt = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS) {
                    Toast.makeText(home.this,"Speak your Destination ",Toast.LENGTH_SHORT).show();
                    prompt.setLanguage(Locale.UK);
                    prompt.speak("Speak your destination :    ",TextToSpeech.QUEUE_ADD,null);
                }
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your destination");
            }
        });

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }
        catch (Exception e) {
            Toast.makeText(home.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String test = result.get(0);
                if (test.length() > 0) {
                    String[] myArray = test.split("destination");
                    int size = myArray.length;
                    //Toast.makeText(home.this, myArray[size - 1].toString(), Toast.LENGTH_SHORT).show();
                    if (size > 0) {
                        destination.setText(myArray[size - 1]);
                        Toast.makeText(home.this, myArray[size - 1].toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        if(requestCode == REQUEST_CODE_SPEECH_INPUT_VIVA) {
            audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            myTTS.setLanguage(Locale.UK);

            ArrayList<String> arrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            chat_user.setText(arrayList.get(0));
            String text = arrayList.get(0).toLowerCase();

            if (text.indexOf("time") != -1 ) {
                Date now = new Date();
                String time = DateUtils.formatDateTime(this, now.getTime(),
                        DateUtils.FORMAT_SHOW_TIME);
                speak("Its " + time);
                chat_system.setText("Its " + time);
            } else

            if (text.indexOf("date") != -1 ) {
                Calendar calendar = Calendar.getInstance();
                String date = DateFormat.getDateInstance().format(calendar.getTime());
                speak("Its " + date);
                chat_system.setText("Its " + date);
            } else if (text.indexOf("battery") != -1 ) {

                BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
                int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
                if(isCharging) {
                    speak("Battery is at " + percentage + " percentage and Charging");
                    chat_system.setText("Battery is at " + percentage + " percentage and Charging");
                }
                else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    speak("Battery is at " + percentage + " percentage and Not Charging");
                    chat_system.setText("Battery is at " + percentage + " percentage and Not Charging");
                }
            } else if (text.indexOf("set") != -1) {
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                if (text.indexOf("vibrate") != -1) {
                    am.setRingerMode(1);
                    chat_system.setText("Phone is on Vibrate");
                    speak("Phone is on Vibrate");
                }
                else if (text.indexOf("ring") != -1) {
                    am.setRingerMode(2);
                    chat_system.setText("Phone is on Ring");
                    speak("Phone is on Ring");
                }
                else {
                    Toast.makeText(getApplicationContext(), "I don't recognise, Plz try again", Toast.LENGTH_SHORT).show();
                    speak("I don't recognise, Please try again");
                }
            } else if (text.indexOf("how") != -1 ) {
                if (text.contains("old are you")) {
                    speak("I'm a new born baby...");
                    chat_system.setText("I'm a new born baby");
                } else
                if (text.contains("are you")) {
                    speak("I'm doing great, thanks for asking. Anything I can help with");
                    chat_system.setText("I'm doing great, thanks for asking. Anything I can help with");
                } else {
                    Toast.makeText(getApplicationContext(), "I don't understand, Plz try again", Toast.LENGTH_SHORT).show();
                    speak("I don't recognise, Please try again");
                }
            } else if (text.indexOf("what") != -1 ) {
                if (text.contains("your name") ) {
                    speak("My Name is Viva");
                    chat_system.setText("My Name is Viva");
                } else
                if (text.contains("meaning of viva") || text.contains("meaning of weaver") || text.contains("meaning of vivah") ) {
                    speak("Viva stands for Visually Impaired's Voice Assistant");
                    chat_system.setText("Viva stands for Visually Impaired's Voice Assistant");
                } else
                if (text.contains("you do") || text.contains("you doing")) {
                    speak("I am your Personal Assistant, I'm here to help you");
                    chat_system.setText("I am your Personal Assistant");
                } else {
                    Toast.makeText(getApplicationContext(), "I don't understand, Plz try again", Toast.LENGTH_SHORT).show();
                    speak("I don't recognise, Please try again");
                }
            } else if (text.indexOf("on") != -1) {
                if (text.contains("bluetooth")) {
                    Intent bluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(bluetooth, 1);
                    Toast.makeText(getApplicationContext(),"Bluetooth Turned ON",Toast.LENGTH_SHORT).show();
                    speak("Bluetooth turned on");
                }
            } else if(text.indexOf("off") != -1){
                if(text.contains("bluetooth") ){
                    BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                    bluetooth.disable();
                    Toast.makeText(getApplicationContext(),"Bluetooth Turned OFF",Toast.LENGTH_SHORT).show();
                    speak("Bluetooth turned off");
                }
            } else if(text.indexOf("where am i") != -1 || text.indexOf("current location") != -1){
                getLastLocation();
                Toast.makeText(getApplicationContext(), "wait a sec...", Toast.LENGTH_LONG).show();
            }
            else if(text.indexOf("volume") != -1  ){
                if(text.contains("up")){
                    audioManager.adjustVolume(AudioManager.ADJUST_RAISE,  AudioManager.STREAM_MUSIC);
                    speak("Volume increased");
                    chat_system.setText("Volume increased");
                } else
                if(text.contains("down")){
                    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.STREAM_MUSIC);
                    speak("Volume Decreased");
                    chat_system.setText("Volume Decreased");
                }
            }
            else if(text.indexOf("check network") != -1 ){
                boolean wifiConnected;
                boolean mobileConnected;
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
                if (activeInfo != null && activeInfo.isConnected()) { //connected with either mobile or wifi
                    wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
                    mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
                    if (wifiConnected) { //wifi connected
                        chat_system.setText("Connected with Wifi");
                        speak("Connected with Wifi");
                    } else if (mobileConnected) { //mobile data connected
                        chat_system.setText("Connected with Mobile Data Connection");
                        speak("Connected with Mobile Data Connection");
                    }
                } else { //no internet connection
                    chat_system.setText("No internet connection");
                    speak("No internet connection");
                }
            }
            else if (text.indexOf("call") != -1) {
                String Num = text.replaceAll("[^0-9]", "");
                Intent c = new Intent(Intent.ACTION_CALL);
                if (Num.trim().isEmpty()) {
                    Toast.makeText(this, "Plz tell the correct number", Toast.LENGTH_SHORT).show();
                } else {
                    c.setData(Uri.parse("tel:" + Num));
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Please grant the permission to call", Toast.LENGTH_SHORT).show();
                    speak("Please grant the permission to call");
                    requestPermission();
                } else {
                    startActivity(c);
                    speak("Calling "+Num);
                }
                chat_user.setText("Calling " + Num);
            }
            else if (text.indexOf("thank you") != -1 || text.indexOf("thanks") != -1) {
                speak("You're Always Welcome");
                chat_system.setText("You're Always Welcome...");
            }
            else if (text.indexOf("see you later") != -1 || text.indexOf("exit") != -1) {
                speak("Bye");
                super.onBackPressed();
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addCategory(Intent.CATEGORY_HOME);
                startActivity(i);
                finish();
                System.exit(0);
            }
            else {
                Toast.makeText(getApplicationContext(), "I don't understand, Plz try again", Toast.LENGTH_SHORT).show();
                speak("I don't recognise, Please try again");
            }

        }

    }

    private void userListSpeak() {
        Toast.makeText(home.this,"test toast",Toast.LENGTH_SHORT).show();
        prompt = new TextToSpeech(home.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS && (role.equals("Client") || role.equals("client"))) {
                    // To Choose language of speech

                    prompt.setLanguage(Locale.UK);
                    prompt.speak("Users to the given destination are : ",TextToSpeech.QUEUE_ADD,null);
                    Log.d("Home Speaker","Initialised");

                    if(fetched.length()==0) {

                        prompt.speak("No conductors found to the given destination",TextToSpeech.QUEUE_ADD,null);

                    }

                    for(int i2=0;i2< fetched.length();++i2) {
                        try {
                            Log.d("User List Iteration",fetched.getJSONObject(i2).getString("name"));
                            JSONObject test = fetched.getJSONObject(i2);
                            String text = test.getString("name");
                            prompt.speak(text,TextToSpeech.QUEUE_ADD,null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    Log.d("Home Speaker","Error! Cannot be initialised.");
                }
            }
        });

        //Iterate and speak user list
        Log.d("Home Speaker","Debug Point 1");
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.VIBRATE,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            //add = add + "\n" + obj.getCountryName();
            // add = add + "\n" + obj.getCountryCode();
            // add = add + "\n" + obj.getAdminArea();
            // add = add + "\n" + obj.getPostalCode();
            // add = add + "\n" + obj.getSubAdminArea();
            // add = add + "\n" + obj.getLocality();
            // add = add + "\n" + obj.getSubThoroughfare();

            // Log.v("IGA", "Address" + add);
            //Toast.makeText(this, "Address=>" + add,Toast.LENGTH_SHORT).show();
            chat_system.setText(add);
            speak("My Location is "+add);


            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {

                            getAddress(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    private void ApiCall() {
        //Api Call instance
        apiInterface = retrofit.create(ApiInterface.class);

        if(role.equals("Client") || role.equals("client")) {
            apiInstance = apiInterface.getConductors(destination.getText().toString().toLowerCase(Locale.ROOT), email_glob);
            Log.d("Test Get Conductors List Params",destination.getText().toString());
        }
        else {
            apiInstance = apiInterface.getClients(destination.getText().toString().toLowerCase(Locale.ROOT), email_glob);
            Log.d("Test Get Clients List Params",destination.getText().toString());
        }
        apiInstance.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.code()==200) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body());
                        fetched = (JSONArray) jsonObject.get("result");
                        userListSpeak();
                        if(!fetched.isNull(0)) {
                            setUserList();

                            Log.d("JSON Test",fetched.get(0).toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(home.this,t.getMessage(),Toast.LENGTH_SHORT).show();
                Log.e("Api Error Test",t.getMessage());
            }
        });
    }

    private void speak(String message) {
        if(Build.VERSION.SDK_INT >= 21) {

            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH,null,null);

        } else {
            Toast.makeText(home.this,"Viva engine not on this device",Toast.LENGTH_LONG).show();
        }

    }

    private void onClickFunctions() {

        swipe_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                popupWindow.dismiss();
                Log.d("Home Page","Refresh Initiated");
                swipe_layout.setRefreshing(false);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profile = new Intent(home.this,conductor_home.class);
                profile.putExtra("id",id);
                profile.putExtra("role",role);
                profile.putExtra("email",email_glob);
                startActivity(profile);
            }
        });

        destination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ApiCall();
            }
        });
    }

    public void promptSpeechInput() {
        Intent voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        voice.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now...");
        try {
            startActivityForResult(voice, REQUEST_CODE_SPEECH_INPUT_VIVA);
        } catch (ActivityNotFoundException a) {
            speak("Sorry!Your device does not support speech input.");
            Toast.makeText(getApplicationContext(),"Sorry!Your device does not support speech input.",Toast.LENGTH_SHORT).show();
        }
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            getAddress(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
    };

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}