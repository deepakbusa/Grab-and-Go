package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class firstpage extends AppCompatActivity {

    private Button userLoginButton, orderLoginButton, shopLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstpage);

        userLoginButton = findViewById(R.id.user_login_button);
        orderLoginButton = findViewById(R.id.order_login_button);
        shopLoginButton = findViewById(R.id.shop_login_button);

        shopLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(firstpage.this, login.class);
                startActivity(intent);
            }
        });
        orderLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(firstpage.this, orderlogin.class);
                startActivity(intent);
            }
        });
        userLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(firstpage.this, .class);
                //startActivity(intent);
                Intent intent = new Intent(firstpage.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}