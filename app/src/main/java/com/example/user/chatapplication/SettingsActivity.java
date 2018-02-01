package com.example.user.chatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mReference;
    private FirebaseUser mCurrentUser;
    private TextView profileName,profileStatus;
    private Button imgChangebtn,statusChangebtn;
    private CircleImageView imageView;
    private static final int GELLARY_PICK = 1;

    //firebase storage
    private StorageReference mImageStorage;
    private String userID;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        statusChangebtn = (Button)findViewById(R.id.change_status_btn);
        imgChangebtn = (Button)findViewById(R.id.change_p_btn);
        imageView = (CircleImageView)findViewById(R.id.profile_image);
        profileName = (TextView)findViewById(R.id.profile_name);
        profileStatus = (TextView)findViewById(R.id.profile_status);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = mCurrentUser.getUid();

        //firebase storage
        mImageStorage = FirebaseStorage.getInstance().getReference();

        //firebase database
        mReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        mReference.keepSynced(true);

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);

                if(!image.equals("proimg")) {

                    //Picasso.with(SettingsActivity.this).load(thumb_image).placeholder(R.drawable.proimg).into(imageView);
                    Picasso.with(SettingsActivity.this).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.proimg).into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(SettingsActivity.this).load(thumb_image).placeholder(R.drawable.proimg).into(imageView);

                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        statusChangebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status = profileStatus.getText().toString();

                Intent intent = new Intent(SettingsActivity.this,StatusActivity.class);
                intent.putExtra("status",status);
                startActivity(intent);
            }
        });

        imgChangebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gellaryIntent = new Intent();
                gellaryIntent.setType("image/*");
                gellaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gellaryIntent,"select image"),GELLARY_PICK);

              /* CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);*/// start picker to get image for cropping and then use the image in cropping activity



            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GELLARY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(300,300)
                    .start(this);// start cropping activity for pre-acquired image saved on the device

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgress = new ProgressDialog(this);
                mProgress.setTitle("uploading...");
                mProgress.setMessage("please wait a while");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();



                Uri resultUri = result.getUri();

                //get the file
                File thumb_file = new File(resultUri.getPath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                //set the file into compress to bitmap and decreased the quality of that bitmap file
                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(150)
                            .setMaxHeight(150)
                            .setQuality(50)
                            .compressToBitmap(thumb_file);

                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                final byte[] byte_data = baos.toByteArray();

                StorageReference filepath = mImageStorage.child("profile_images").child(userID + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumb").child(userID + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(byte_data);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    if(thumb_task.isSuccessful()){

                                        String thumb_url = thumb_task.getResult().getDownloadUrl().toString();

                                        Map map = new HashMap();
                                        map.put("image",downloadUrl);
                                        map.put("thumb_image",thumb_url);

                                        mReference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                mProgress.dismiss();
                                                Toast.makeText(getApplicationContext(),"profile updated!",Toast.LENGTH_SHORT).show();
                                            }
                                        });


                                    }else{

                                        Toast.makeText(getApplicationContext(),"not working",Toast.LENGTH_SHORT).show();
                                        mProgress.hide();
                                    }

                                }
                            });

                        }else{
                            Toast.makeText(getApplicationContext(),"not working",Toast.LENGTH_SHORT).show();
                            mProgress.hide();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }


}
