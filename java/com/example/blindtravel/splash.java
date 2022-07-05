package com.example.blindtravel;

import android.content.Intent;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;



public class splash extends AppCompatActivity {
    long delay=1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        Timer t=new Timer();

        TimerTask tt=new TimerTask() {
            @Override
            public void run() {
                finish();
                Intent i= new Intent(splash.this, login.class);
                startActivity(i);

            }
        };
        t.schedule(tt,delay);
    }
}
