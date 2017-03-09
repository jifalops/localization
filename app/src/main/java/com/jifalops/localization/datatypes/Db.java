package com.jifalops.localization.datatypes;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.jifalops.localization.App;

/**
 *
 */
public class Db {
    private static final String TAG = Db.class.getSimpleName();

    private static Db instance;
    public static Db getInstance() {
        if (instance == null) { instance = new Db(); }
        return instance;
    }
    
    private FirebaseUser user;
    private DatabaseReference db;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    
    // Lookup ranging params
    private DatabaseReference rssWifi4gSettings;
    private DatabaseReference rssWifi5gSettings;
    private DatabaseReference rssBtSettings;
    private DatabaseReference rssBtleSettings;
    private DatabaseReference tofBtHciSettings;
    private DatabaseReference tofBtJavaSettings;
    
    // Sample submission
    private DatabaseReference rssWifi4gSamples;
    private DatabaseReference rssWifi5gSamples;
    private DatabaseReference rssBtSamples;
    private DatabaseReference rssBtleSamples;
    private DatabaseReference tofBtHciSamples;
    private DatabaseReference tofBtJavaSamples;

    // Ranging results submission
    private DatabaseReference rssWifi4gRanging;
    private DatabaseReference rssWifi5gRanging;
    private DatabaseReference rssBtRanging;
    private DatabaseReference rssBtleRanging;
    private DatabaseReference tofBtHciRanging;
    private DatabaseReference tofBtJavaRanging;
    
    private Db() {
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    startListening();
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    stopListening();
                }
            }
        };

        db = FirebaseDatabase.getInstance().getReference();

        // Lookup ranging params
        rssWifi4gSettings = db.child("activeRssWifi4gSettings");
        rssWifi5gSettings = db.child("activeRssWifi5gSettings");
        rssBtSettings = db.child("activeRssBtSettings");
        rssBtleSettings = db.child("activeRssBtleSettings");
        tofBtHciSettings = db.child("activeTofBtHciSettings");
        tofBtJavaSettings = db.child("activeTofBtJavaSettings");

        // Sample submission
        rssWifi4gSamples = db.child("rssWifi4gSamples");
        rssWifi5gSamples = db.child("rssWifi5gSamples");
        rssBtSamples = db.child("rssBtSamples");
        rssBtleSamples = db.child("rssBtleSamples");
        tofBtHciSamples = db.child("tofBtHciSamples");
        tofBtJavaSamples = db.child("tofBtJavaSamples");

        // Ranging results submission
        rssWifi4gRanging = db.child("rssWifi4gRanging");
        rssWifi5gRanging = db.child("rssWifi5gRanging");
        rssBtRanging = db.child("rssBtRanging");
        rssBtleRanging = db.child("rssBtleRanging");
        tofBtHciRanging = db.child("tofBtHciRanging");
        tofBtJavaRanging = db.child("tofBtJavaRanging");
    }
    
    public void login() {
        auth.addAuthStateListener(authListener);
        auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInAnonymously", task.getException());
                    Toast.makeText(App.getInstance(), "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void removeAuthListener() {
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    private void startListening() {
        rssWifi4gSettings.addValueEventListener(settingsListener);
        rssWifi5gSettings.addValueEventListener(settingsListener);
        rssBtSettings.addValueEventListener(settingsListener);
        rssBtleSettings.addValueEventListener(settingsListener);
        tofBtHciSettings.addValueEventListener(settingsListener);
        tofBtJavaSettings.addValueEventListener(settingsListener);
    }

    private void stopListening() {
        rssWifi4gSettings.removeEventListener(settingsListener);
        rssWifi5gSettings.removeEventListener(settingsListener);
        rssBtSettings.removeEventListener(settingsListener);
        rssBtleSettings.removeEventListener(settingsListener);
        tofBtHciSettings.removeEventListener(settingsListener);
        tofBtJavaSettings.removeEventListener(settingsListener);
    }
    

    private ValueEventListener settingsListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String value = dataSnapshot.getValue().toString();
            Log.d(TAG, "SettingsListener key: " + dataSnapshot.getKey() + " value: " + value);
            
            switch (dataSnapshot.getKey()) {
                case "activeRssWifi4gSettings":
                    db.child("rssWifi4gSettings").child(value).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                dataSnapshot.getValue(DbRangingSettings.class).setType("rssWifi4gSettings");
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, "rssWifi4gSettings", databaseError.toException());
                            }
                        }
                    );
                    break;
                case "activeRssWifi5gSettings":
                    db.child("rssWifi5gSettings").child(value).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                dataSnapshot.getValue(DbRangingSettings.class).setType("rssWifi5gSettings");
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, "rssWifi5gSettings", databaseError.toException());
                            }
                        }
                    );
                    break;
                case "activeRssBtSettings":
                    db.child("rssBtSettings").child(value).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    dataSnapshot.getValue(DbRangingSettings.class).setType("rssBtSettings");
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "rssBtSettings", databaseError.toException());
                                }
                            }
                    );
                    break;
                case "activeRssBtleSettings":
                    db.child("rssBtleSettings").child(value).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    dataSnapshot.getValue(DbRangingSettings.class).setType("rssBtleSettings");
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "rssBtleSettings", databaseError.toException());
                                }
                            }
                    );
                    break;
                case "activeTofBtHciSettings":
                    db.child("tofBtHciSettings").child(value).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    dataSnapshot.getValue(DbRangingSettings.class).setType("tofBtHciSettings");
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "tofBtHciSettings", databaseError.toException());
                                }
                            }
                    );
                    break;
                case "activeTofBtJavaSettings":
                    db.child("tofBtJavaSettings").child(value).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    dataSnapshot.getValue(DbRangingSettings.class).setType("tofBtJavaSettings");
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "tofBtJavaSettings", databaseError.toException());
                                }
                            }
                    );
                break;
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "Error listening to ranging settings.", databaseError.toException());
        }
    };

    @IgnoreExtraProperties
    private static class DbRangingSettings {
        public int samples, dropHigh, dropLow, inputs, hidden, maxRange;
        public String method, weights;

        private void setType(String type) {
            
        }
    }
}
