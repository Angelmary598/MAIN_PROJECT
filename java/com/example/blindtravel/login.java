package com.example.blindtravel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blindtravel.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class login extends AppCompatActivity {

    private String BASE_URL = "http://192.168.100.15:5000";

    private EditText email,passwrd;
    private TextView signup;
    private Button login;

    private String emailVal;
    private String passVal;
    private String role;

    private RadioGroup role_group;
    private RadioButton role_val;

    private Boolean logged_in = false;
    private String id;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");

        getSharedPrefs();

        Log.d("SharedPrefsLoggedIn",logged_in.toString());
        Log.d("SharedPrefsId",id);
        Log.d("SharedPrefsEmail",emailVal);

        if(logged_in) {
            Intent i = new Intent(login.this, home.class);
            i.putExtra("id",id);
            i.putExtra("role",role);
            i.putExtra("email",emailVal);
            startActivity(i);
        }
        else {
            init();
            onClickFunctions();
        }
    }

    private void init() {
        email = findViewById(R.id.edtTextEmailLogin);
        passwrd = findViewById(R.id.edtTextPasswordLogin);
        login = findViewById(R.id.btnLogin);
        signup = findViewById(R.id.txtViewSignupLogin);
        role_group = findViewById(R.id.login_role);
    }

    private void onClickFunctions() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getValues();
                ApiCall();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(login.this,register.class);
                startActivity(i);
            }
        });
    }

    private void getValues() {
        emailVal = email.getText().toString();
        passVal = passwrd.getText().toString();
        int selectedRole = role_group.getCheckedRadioButtonId();
        role_val = findViewById(selectedRole);
        role = role_val.getText().toString();
    }

    private void ApiCall() {
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Log.v("Api Call Test",role.toString());
        if(role.equals("Client") || role.equals("client")) {
            Call<String> user = apiInterface.loginClient(emailVal,passVal);
            Log.v("Login Api Test",emailVal);
            user.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if(response.code()!=200) {
                        Log.e("Login Page","Error "+response.code()+": "+response.body()+".");
                        Toast.makeText(login.this,"Invalid Credentials",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //On Api Call Success
                        Log.d("Login Page",response.body().toString());
                        Intent intent = new Intent(login.this, home.class);
                        if(response.body()!=null && response.body()!="") {
                            intent.putExtra("id",response.body());
                            intent.putExtra("role","Client");
                            intent.putExtra("email",emailVal);
                            setSharedPrefs(response.body(),"client");
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(login.this,"Invalid Credentials",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    //On Api Call Failure
                    Log.e("Login Page",t.getMessage().toString());
                }
            });
        }
        else {
            Call<String> user = apiInterface.login(emailVal,passVal);
            user.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if(response.code()!=200) {
                        Log.e("Login Page","Error "+response.code()+": "+response.body()+".");
                        Toast.makeText(login.this,"Invalid Credentials",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //On Api Call Success
                        Log.d("Login Page",response.body().toString());
                        Intent intent = new Intent(login.this, home.class);
                        if(response.body()!=null && response.body()!="") {
                            intent.putExtra("id", response.body());
                            intent.putExtra("role", "Conductor");
                            intent.putExtra("email",emailVal);
                            setSharedPrefs(response.body(),"conductor");
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(login.this,"Invalid Credentials",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    //On Api Call Failure
                    Log.e("Login Page",t.getMessage().toString());
                }
            });
        }
    }

    private void getSharedPrefs() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        logged_in = pref.getBoolean("logged_in",false);
        emailVal = pref.getString("email","null");
        id = pref.getString("id","null");
        role = pref.getString("role","client");
    }

    private void setSharedPrefs(String id,String pvt_role) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("id",id);
        editor.putString("email",emailVal);
        editor.putString("role",pvt_role);
        editor.putBoolean("logged_in",true);
        editor.commit();
        Log.d("Login Page","Shared Preferences Set.");
    }

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}