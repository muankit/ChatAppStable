package com.example.ankit.chatappstable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;

    private Button mLoginBtn;
    private ProgressDialog mProgress;
    private Button mNewRegisterBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login Account");

        mLoginEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mLoginPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mLoginBtn = (Button) findViewById(R.id.login_Btn);
        mNewRegisterBtn = (Button) findViewById(R.id.login_new_register_btn);

        mProgress = new ProgressDialog(this );

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();


        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mLoginEmail.getEditText().getText().toString();
                String password = mLoginPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                    mProgress.setTitle("Logging In");
                    mProgress.setMessage("Please wait while we check your credentials");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();
                    loginUser(email,password);
                }

            }
        });

        mNewRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RegisterIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(RegisterIntent);
                finish();
            }
        });

    }

    private void loginUser(String email, String password) {

         mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
             @Override
             public void onComplete(@NonNull Task<AuthResult> task) {
                 if(task.isSuccessful()){
                     mProgress.dismiss();

                     if(mAuth.getCurrentUser()!=null) {

                         String current_user_id = mAuth.getCurrentUser().getUid();
                         String deviceToken = FirebaseInstanceId.getInstance().getToken();

                         mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid) {

                                 Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                                 loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                 startActivity(loginIntent);
                                 finish();

                             }
                         });
                     }
                 }else{
                        mProgress.hide();
                     Toast.makeText(LoginActivity.this , "Error logging In " , Toast.LENGTH_LONG).show();
                 }
             }
         });
    }
}
