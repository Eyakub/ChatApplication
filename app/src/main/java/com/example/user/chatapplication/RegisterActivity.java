package com.example.user.chatapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName,etEmail,etPassword;
    private Button btnCreate;
    private  String name,email,password;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar)findViewById(R.id.register_toolbar);
        mProgressDialog = new ProgressDialog(this);

        //firebase realtime database
        mDatabase = FirebaseDatabase.getInstance();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Registration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binds();
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.setTitle("Registering user");
                mProgressDialog.setMessage("Please wait a while....");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                getDataFromEditText();
                if(!TextUtils.isEmpty(name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    registerUser(name, email, password);
                }else{
                    mProgressDialog.hide();
                    Toast.makeText(getApplicationContext(),"All The Field Must Be filled",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void getDataFromEditText() {

        name = etName.getText().toString();
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();

    }
private void registerUser(final String uname, String uemail, String upassword) {

   mAuth.createUserWithEmailAndPassword(uemail,upassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
       @Override
       public void onComplete(@NonNull Task<AuthResult> task) {
           if(task.isSuccessful()){

               FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
               String userID = currentUser.getUid();
               String deviceToken = FirebaseInstanceId.getInstance().getToken();

               mReference = mDatabase.getReference().child("users").child(userID);

               HashMap<String,String> userMap = new HashMap<String, String>();
               userMap.put("device_token",deviceToken);
               userMap.put("name",uname);
               userMap.put("status","Hey there ,I'm using android chat application");
               userMap.put("image","proimg");
               userMap.put("thumb_image","proimg");

               mReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {

                   @Override
                   public void onComplete(@NonNull Task<Void> task) {

                       if(task.isSuccessful()){
                           mProgressDialog.dismiss();
                           Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                           startActivity(intent);
                           finish();
                       }
                   }
               });
           }else{
               mProgressDialog.hide();
               Toast.makeText(getApplicationContext(),"You got some error",Toast.LENGTH_SHORT).show();
           }
       }
   });

    }
    private void binds() {

        etName = (EditText)findViewById(R.id.reg_display_name);
        etEmail = (EditText)findViewById(R.id.reg_email);
        etPassword = (EditText)findViewById(R.id.reg_password);
        btnCreate = (Button)findViewById(R.id.reg_create_btn);
    }

}
