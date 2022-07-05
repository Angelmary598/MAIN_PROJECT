package com.example.blindtravel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blindtravel.models.SignupUser;

import java.io.ByteArrayOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class register extends AppCompatActivity {

    EditText name,email,pass,confirmpass,mobile;
    ImageView profile;
    Button signup;
    SignupUser user = new SignupUser();

    private String BASE_URL = "http://192.168.100.15:5000";

    private String nameEntry;
    private String passEntry;
    private String mobileEntry;
    private String emailEntry;
    private String confirmPassEntry;
    private Boolean requested = false;
    private Bitmap profile_image;
    private RadioGroup role_group;
    private RadioButton role_val;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        init();
        onClickFunctions();
    }

    private void init() {
        name = findViewById(R.id.edtTextNameSignup);
        email = findViewById(R.id.edtTextEmailSignup);
        pass = findViewById(R.id.edtTextPasswordSignup);
        mobile = findViewById(R.id.edtTextMobileSignup);
        confirmpass = findViewById(R.id.edtTextConfirmPasswordSignup);
        signup = findViewById(R.id.btnSignup);
        profile = findViewById(R.id.imageView);
        role_group = findViewById(R.id.Role);
    }

    private void onClickFunctions () {
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameEntry = name.getText().toString();
                passEntry = pass.getText().toString();
                mobileEntry = mobile.getText().toString();
                emailEntry = email.getText().toString();
                confirmPassEntry = confirmpass.getText().toString();

                int selectedRole = role_group.getCheckedRadioButtonId();
                role_val = findViewById(selectedRole);
                user.setRole(role_val.getText().toString());

                if (!passEntry.equals(confirmPassEntry)) {
                    Toast.makeText(register.this, "passwords are not matching", Toast.LENGTH_SHORT).show();
                } else
                    if (profile_image == null) {
                        Toast.makeText(register.this, "No image has been selected....", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        profile_image.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        user.setPicturePath(Base64.encodeToString(byteArray, Base64.DEFAULT));
                    }
                    user.setName(nameEntry);
                    user.setEmail(emailEntry);
                    user.setMobile(mobileEntry);
                    user.setPasswrd(passEntry);
                    ApiInterface apiInterface = retrofit.create(ApiInterface.class);
                    Log.d("Signup Client", user.toString());
                    if (user.getRole().equals("Client") || user.getRole().equals("client")) {
                        Call<String> apiCall = apiInterface.signupTest(user.getName(), user.getEmail(), user.getPasswrd(), user.getMobile(), "25.252525", "14.141414", "null", user.getRole(), user.getPicturePath(), requested);
                        apiCall.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                Log.d("Signup Client", response.toString());
                                if (response.code() == 200) {
                                    Toast.makeText(register.this, response.body(), Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(register.this, login.class);
                                    startActivity(i);
                                }
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Toast.makeText(register.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        Call<String> apiCall = apiInterface.signupObj(user.getName(), user.getEmail(), user.getPasswrd(), user.getMobile(), "25.252525", "14.141414", "null", user.getRole(), user.getPicturePath(), requested);
                        apiCall.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                if (response.code() == 200) {
                                    Toast.makeText(register.this, response.body(), Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(register.this, login.class);
                                    startActivity(i);
                                }
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Toast.makeText(register.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
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
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
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