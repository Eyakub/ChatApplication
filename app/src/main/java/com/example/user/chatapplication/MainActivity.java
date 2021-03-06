package com.example.user.chatapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolBar;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private TabLayout tabLayout;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        tabLayout = (TabLayout)findViewById(R.id.main_tab_layout);
        mToolBar = (Toolbar)findViewById(R.id.main_pagr_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Chat app");


        //tabs
        viewPager = (ViewPager)findViewById(R.id.main_tab_pager);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //set adapter with viewPager
        viewPager.setAdapter(sectionsPagerAdapter);

        tabLayout.setTabTextColors(ColorStateList.valueOf(Color.WHITE));

        //set up tablayout with view pager
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_btn){

            FirebaseAuth.getInstance().signOut();
            userRef.child("online").setValue(false);
            sentStartActivity();
        }else if(item.getItemId() == R.id.main_settings_btn){
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.main_all_btn){
            Intent intent = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(intent);
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
           sentStartActivity();
        }else{
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
            userRef.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
       /* final String currentDate;

        java.text.DateFormat dateFormat = new SimpleDateFormat("dd/yy  hh:mm aa");
        currentDate = dateFormat.format(Calendar.getInstance().getTime());*/

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
            userRef.child("last_online").setValue(ServerValue.TIMESTAMP);
            userRef.child("online").onDisconnect().setValue(false);
        }
    }

    private void sentStartActivity() {
        Intent intent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(intent);
        finish();

    }
}
