package com.example.ankit.chatappstable;

import android.app.ProgressDialog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView mProfileImage;
    private TextView mProfileCurrentStatus, mProfileDisplayName,mProfileTotalFriends;
    private Button mProfileSendReqBtn,mDeclineBtn;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private DatabaseReference mRootRef;

    private FirebaseUser mCurrent_user;

    private ProgressDialog mProgress;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notification");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mProfileImage = (CircleImageView) findViewById(R.id.profile_image);
        mProfileDisplayName = (TextView) findViewById(R.id.profile_display_name);
        mProfileCurrentStatus = (TextView) findViewById(R.id.profile_status);
        mProfileTotalFriends = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_SendReqBtn);
        mDeclineBtn = (Button) findViewById(R.id.profile_Decline_Btn);

        mCurrent_state = "not_friend";

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading User Data");
        mProgress.setMessage("Please wait while we load");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileDisplayName.setText(display_name);
                mProfileCurrentStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.profile_image).into(mProfileImage);

                // ----------Friend List / Request feature---------

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Acccept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);


                            } else if (req_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            mProgress.dismiss();
                        } else {

                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {

                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend This Person");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }
                                    mProgress.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgress.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}

                });


                mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProfileSendReqBtn.setEnabled(false);

                        // -----------First State = Not Friends -------------
                        if (mCurrent_state.equals("not_friend")) {

                            DatabaseReference newNotificationRef = mRootRef.child("notification").child(user_id).push();
                            String newNotificationId = newNotificationRef.getKey();

                            HashMap<String,String>  notificationData = new HashMap<>();
                            notificationData.put("from" , mCurrent_user.getUid());
                            notificationData.put("type", "request");

                            Map requestMap = new HashMap();
                            requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type" , "sent");
                            requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type" , "received");
                            requestMap.put("notification/" + user_id + "/" + newNotificationId , notificationData);

                            mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if(databaseError != null){
                                        Toast.makeText(ProfileActivity.this, " Error Sending Request" ,Toast.LENGTH_LONG).show();
                                    }
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "req_sent";
                                    mProfileSendReqBtn.setText("Cancel Friend Request");

                                }
                            });

                        }


                        // -----------Second State = Cancel Friend Request -------------

                        if (mCurrent_state.equals("req_sent")) {

                            mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mProfileSendReqBtn.setEnabled(true);
                                            mCurrent_state = "not_friend";
                                            mProfileSendReqBtn.setText("Send Friend Request");

                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                            mDeclineBtn.setEnabled(false);


                                        }
                                    });
                                }
                            });
                        }

                        // --------------- Req Received state ------------------

                        if (mCurrent_state.equals("req_received")) {

                            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                            Map friendsMap = new HashMap();
                            friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date" , currentDate);
                            friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date" , currentDate);

                            friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id , null);
                            friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() , null );

                            mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if(databaseError == null){

                                        mProfileSendReqBtn.setEnabled(true);
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend This Person");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }else {
                                        String error = databaseError.getMessage();
                                        Toast.makeText(ProfileActivity.this, error ,Toast.LENGTH_LONG).show();


                                    }
                                }
                            });
                        }

                        // ------------ Unfriend Person -----------

                        if(mCurrent_state.equals("friends")){

                            Map unfriendMap = new HashMap();
                            unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id , null);
                            unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() , null);

                            mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if(databaseError == null){
                                        mCurrent_state = "not_friend";
                                        mProfileSendReqBtn.setText("Send Friend Request");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }else {
                                        String error = databaseError.getMessage();
                                        Toast.makeText(ProfileActivity.this, error ,Toast.LENGTH_LONG).show();


                                    }
                                    mProfileSendReqBtn.setEnabled(true);

                                }
                            });
                        }
                    }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }
}
