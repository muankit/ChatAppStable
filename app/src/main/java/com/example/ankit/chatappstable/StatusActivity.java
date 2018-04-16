package com.example.ankit.chatappstable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.PrivateKey;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mStatusToolbar;

    private TextInputLayout mStatus;
    private Button mSaveBtn;

    private DatabaseReference mStatusDatabse;
    private FirebaseUser mUser;

    private ProgressDialog mStatusProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mStatusToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mStatusToolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mUser.getUid();

        mStatusDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        String status_value = getIntent().getStringExtra("status_value");

        mStatus = (TextInputLayout) findViewById(R.id.status_input);
        mSaveBtn = (Button) findViewById(R.id.status_save_Btn);

        mStatus.getEditText().setText(status_value);


        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mStatusProgress = new ProgressDialog(StatusActivity.this);

                mStatusProgress.setTitle("Updating Status");
                mStatusProgress.setMessage("Please wait while we update");
                mStatusProgress.show();
                String status = mStatus.getEditText().getText().toString();

                mStatusDatabse.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mStatusProgress.dismiss();

                            Intent backIntent = new Intent(StatusActivity.this,SettingsActivity.class);
                            startActivity(backIntent);
                            finish();


                        }else{
                            Toast.makeText(StatusActivity.this, "Error in Saving ", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
