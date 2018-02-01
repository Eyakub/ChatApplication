package com.example.user.chatapplication;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mEditTest;
    private Button mButton;

    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;

    //progress dialog
    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        mToolbar = (Toolbar)findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgress = new ProgressDialog(this);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userID = mCurrentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("status");

        mEditTest = (EditText)findViewById(R.id.status_et);
        mButton = (Button)findViewById(R.id.status_btn_save);
        mEditTest.setText(getIntent().getStringExtra("status"));

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgress.setTitle("saving changes");
                mProgress.setMessage("wait for a while...");
                mProgress.show();

                String status = mEditTest.getText().toString();

                mDatabase.setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();
                        }else{
                            Toast.makeText(getApplicationContext(),"There are some error",Toast.LENGTH_SHORT).show();
                            mProgress.hide();
                        }
                    }
                });


            }
        });

    }
}
