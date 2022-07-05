package com.example.blindtravel;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.blindtravel.models.SignupUser;
import com.example.blindtravel.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class conductor_home extends AppCompatActivity {

    private String BASE_URL = "http://192.168.100.15:5000";

    private EditText name;
    private EditText email;
    private EditText mobile;
    private EditText passwrd;
    private ImageView profile;
    private Bitmap profile_image;

    private Button update;

    private JSONObject jsonObject;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private String id = "";
    private String role = "Client";
    private String email_glob = "";

    private User user = null;
    private SignupUser updateUser = new SignupUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor_home);
        getSupportActionBar().setTitle("User Profile");

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        role = intent.getStringExtra("role");
        email_glob = intent.getStringExtra("email");
        Log.d("Test Shared Prefs Profile",id+" : "+role+":"+email_glob);

        init();
        apiCallGet();
        onClickFunctions();
    }

    private void setVals() {
        try {
            name.setText(jsonObject.getString("name"));
            email.setText(jsonObject.getString("email"));
            mobile.setText(jsonObject.getString("mobile"));
            passwrd.setText(jsonObject.getString("password"));

            String imageDataBytes = jsonObject.getString("profilePicturePath");
            InputStream stream = new ByteArrayInputStream(Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT));
            profile_image = BitmapFactory.decodeStream(stream);
            if (imageDataBytes.length() > 0) {
                profile.setImageBitmap(profile_image);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void apiCallGet() {
        //Api Call
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<String> apiCall;
        String resVal;
        if(role.equals("Client") || role.equals("client")) {
            apiCall = apiInterface.getClient(email_glob);
        }
        else {
            apiCall = apiInterface.getConductor(email_glob);
        }
        Log.d("Profile Page Api params",role+id);
        apiCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Toast.makeText(conductor_home.this,response.toString(),Toast.LENGTH_SHORT).show();
                Log.d("Profile Page Api Response",response.toString());
                try {
                    jsonObject = new JSONObject(response.body());
                    jsonObject = jsonObject.getJSONObject("result");
                    Log.d("Profile JSON Test",jsonObject.getString("email"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(jsonObject != null) {
                    setVals();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(conductor_home.this,t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getVals() {
        updateUser.setRole(role);
        updateUser.setName(name.getText().toString());
        updateUser.setEmail(email.getText().toString());
        updateUser.setMobile(mobile.getText().toString());
        updateUser.setPasswrd(passwrd.getText().toString());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        profile_image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        updateUser.setPicturePath(Base64.encodeToString(byteArray, Base64.DEFAULT));

        Toast.makeText(conductor_home.this,"Profile Image: "+user.getPicturePath(),Toast.LENGTH_SHORT).show();
    }

    private void apiCallUpdate() {
        //Api Call
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<User> apiCall;
        Boolean test = false;
        getVals();
        if(role=="Client") {
            apiCall = apiInterface.updateClient(updateUser.getName(),updateUser.getEmail(),updateUser.getPasswrd(),updateUser.getMobile(),updateUser.getCurrentLat(),updateUser.getCurrentLong(),updateUser.getDestination(),updateUser.getRole(),updateUser.getPicturePath(),true);
            test = true;
        }
        else {
            apiCall = apiInterface.updateConductor(updateUser.getName(),updateUser.getEmail(),updateUser.getPasswrd(),updateUser.getMobile(),updateUser.getCurrentLat(),updateUser.getCurrentLong(),updateUser.getDestination(),updateUser.getRole(),updateUser.getPicturePath(),true);
            test = true;
        }
        if(test) {
            apiCall.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    Toast.makeText(conductor_home.this,"Profile Updated Succesfully",Toast.LENGTH_SHORT).show();
                    apiCallGet();
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(conductor_home.this,"Profile Could Not be Updated",Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(conductor_home.this,"Some Error has occured....",Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        name = findViewById(R.id.name_profile);
        email = findViewById(R.id.email_profile);
        mobile = findViewById(R.id.mobile_profile);
        passwrd = findViewById(R.id.password_profile);
        profile = findViewById(R.id.picture_profile);

        update = findViewById(R.id.update_profile);
    }

    private void onClickFunctions () {
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(conductor_home.this,"Update Profile Clicked",Toast.LENGTH_SHORT).show();
                apiCallUpdate();

            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v)
            {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_SHORT).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            profile_image = (Bitmap) data.getExtras().get("data");
            profile.setImageBitmap(profile_image);
        }
    }

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}