package com.example.ankit.chatappstable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;


    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;

    private Button mStatusBtn;
    private Button mImageBtn;

    private static final int GALLERY_REQUEST = 1;

    //Firebase
    private StorageReference mImageStorage;

    private ProgressDialog mImageProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.setting_display_name);
        mStatus = (TextView) findViewById(R.id.setting_status);

        mStatusBtn = (Button) findViewById(R.id.setting_status_btn);
        mImageBtn = (Button) findViewById(R.id.setting_image_Btn);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true );

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if(!image.equals("default")){

                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_account_circle_black_24dp).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.ic_account_circle_black_24dp).into(mDisplayImage);


                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_val = mStatus.getText().toString();

                Intent statusIntent = new Intent(SettingsActivity.this  , StatusActivity.class);
                statusIntent.putExtra("status_value ", status_val);
                startActivity(statusIntent);
                finish();
            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_REQUEST);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST  && resultCode == RESULT_OK){
            Uri imageUri =  data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageProgress = new ProgressDialog(SettingsActivity.this);
                mImageProgress.setTitle("Uploading Image");
                mImageProgress.setMessage("Please wait while we upload Image");
                mImageProgress.setCanceledOnTouchOutside(false);
                mImageProgress.show();

                Uri resultUri = result.getUri();

                File thumb_path = new File(resultUri.getPath());

                String current_user = mCurrentUser.getUid();

                Bitmap thumb_image = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_path);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_image.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filePath = mImageStorage.child("profile_images").child( current_user + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_user + ".jpg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            final String downlaod_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downlaod_url = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()){

                                        Map update_Hashmap = new HashMap<>();
                                        update_Hashmap.put("image",downlaod_url);
                                        update_Hashmap.put("thumb_image" , thumb_downlaod_url);

                                        mUserDatabase.updateChildren(update_Hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    mImageProgress.dismiss();
                                                    Toast.makeText(SettingsActivity.this , "Successfully Uploaded " , Toast.LENGTH_LONG).show();

                                                }
                                            }
                                        });
                                    }else{
                                        Toast.makeText(SettingsActivity.this , "Error Uploading Thumbnail" , Toast.LENGTH_LONG).show();
                                        mImageProgress.dismiss();
                                    }

                                }
                            });



                        }else{
                            Toast.makeText(SettingsActivity.this , "Error Uploading " , Toast.LENGTH_LONG).show();
                                mImageProgress.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
