package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class sucess_update extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sucesss_update);
        Button logoutbutton = findViewById(R.id.logoutbt);
        logoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to LoginActivity
                Intent intent = new Intent(sucess_update.this, login.class); // Make sure the class name is correct
                startActivity(intent);
                finish(); // Optional: close HomeActivity so the user can't return to it
            }
        });
        Button back = findViewById(R.id.backbt);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to LoginActivity
                Intent intent = new Intent(sucess_update.this, HomeActivity.class); // Make sure the class name is correct
                startActivity(intent);
            }
        });
    }

}



