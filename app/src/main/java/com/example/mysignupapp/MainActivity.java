package com.example.mysignupapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
{
    Button button_for_login; // the button "Enter". Used to enter from login to home screen
    Button button_for_register; // the button "Sign up". Used to register a new account

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //we give our buttons ids from the activity_main.xml file
        button_for_login = findViewById(R.id.completeLogin);
        button_for_register = findViewById(R.id.register_button);

        //When we press the "Sign up" button
        button_for_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View ownerView) {

                //We enter a new Intent which takes us to the Register activity
                Intent intent_for_register = new Intent(MainActivity.this, Register.class);
                startActivity(intent_for_register);

            }
        });

        /*When we press the "Enter button"
            1.  If both textfields (username, password) are filled
            2. The Firebase database checks if the authentication has a correct form (Username and Password)
            3. If the syntax is correct, the database searches if user with given credentials exists
            4. If not, we show the proper message
            5. If credentials are correct, we enter as the given user to the HomeActivity
         */
        button_for_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View ownerView) {

                Intent intent_for_home = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent_for_home);

            }
        });
    }

    //In case we press the back button we have a choice to exit and close the app
    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }
}