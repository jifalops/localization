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
import com.jifalops.localization.util.FileBackedArrayList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Db {
    private static final String TAG = Db.class.getSimpleName();

    private static Db instance;

    public static Db getInstance() {
        if (instance == null) {
            instance = new Db();
        }
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
            final String value = dataSnapshot.getValue().toString();
            Log.d(TAG, "SettingsListener key: " + dataSnapshot.getKey() + " value: " + value);

            switch (dataSnapshot.getKey()) {
                case "activeRssWifi4gSettings":
                    db.child("rssWifi4gSettings").child(value).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    dataSnapshot.getValue(DbRangingSettings.class).setType("rssWifi4gSettings", value);
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
                                    dataSnapshot.getValue(DbRangingSettings.class).setType("rssWifi5gSettings", value);
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
                                    dataSnapshot.getValue(DbRangingSettings.class).setType("rssBtSettings", value);
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
                                    dataSnapshot.getValue(DbRangingSettings.class).setType("rssBtleSettings", value);
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
                                    dataSnapshot.getValue(DbRangingSettings.class).setType("tofBtHciSettings", value);
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
                                    dataSnapshot.getValue(DbRangingSettings.class).setType("tofBtJavaSettings", value);
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
    public static class DbRangingSettings {
        public int samples, dropHigh, dropLow, inputs, hidden, maxRange;
        public String method, weights;

        private void setType(String type, String key) {
            double[] w = null;
            App app = App.getInstance();

            try {
                String[] parts = weights.split(",");
                w = new double[parts.length];
                for (int i = 0; i < parts.length; ++i) {
                    w[i] = Double.valueOf(parts[i]);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse weights.", e);
            }

            RefiningParams refiningParams = new RefiningParams(samples, dropHigh, dropLow, method);
            RangingParams rangingParams = new RangingParams(inputs, hidden, maxRange, w);

            switch (type) {
                case "rssWifi4gSettings":
                    app.rssWifi4gRefiningParams = refiningParams;
                    app.rssWifi4gRangingParams = rangingParams;
                    app.rssWifi4gRanging = new FileBackedArrayList(new File(
                            app.getExternalFilesDir(null), App.FILE_RSS_WIFI4G_RANGING + key + ".csv"), null);
                    app.rssWifi4gRangingKey = key;
                    break;
                case "rssWifi5gSettings":
                    app.rssWifi5gRefiningParams = refiningParams;
                    app.rssWifi5gRangingParams = rangingParams;
                    app.rssWifi5gRanging = new FileBackedArrayList(new File(
                            app.getExternalFilesDir(null), App.FILE_RSS_WIFI5G_RANGING + key + ".csv"), null);
                    app.rssWifi5gRangingKey = key;
                    break;
                case "rssBtSettings":
                    app.rssBtRefiningParams = refiningParams;
                    app.rssBtRangingParams = rangingParams;
                    app.rssBtRanging = new FileBackedArrayList(new File(
                            app.getExternalFilesDir(null), App.FILE_RSS_BT_RANGING + key + ".csv"), null);
                    app.rssBtRangingKey = key;
                    break;
                case "rssBtleSettings":
                    app.rssBtleRefiningParams = refiningParams;
                    app.rssBtleRangingParams = rangingParams;
                    app.rssBtleRanging = new FileBackedArrayList(new File(
                            app.getExternalFilesDir(null), App.FILE_RSS_BTLE_RANGING + key + ".csv"), null);
                    app.rssBtleRangingKey = key;
                    break;
                case "tofBtHciSettings":
                    app.tofBtHciRefiningParams = refiningParams;
                    app.tofBtHciRangingParams = rangingParams;
                    app.tofBtHciRanging = new FileBackedArrayList(new File(
                            app.getExternalFilesDir(null), App.FILE_TOF_BT_HCI_RANGING + key + ".csv"), null);
                    app.tofBtHciRangingKey = key;
                    break;
                case "tofBtJavaSettings":
                    app.tofBtJavaRefiningParams = refiningParams;
                    app.tofBtJavaRangingParams = rangingParams;
                    app.tofBtJavaRanging = new FileBackedArrayList(new File(
                            app.getExternalFilesDir(null), App.FILE_TOF_BT_JAVA_RANGING + key + ".csv"), null);
                    app.tofBtJavaRangingKey = key;
                    break;
            }
        }
    }


    public void submitSamples(final DatabaseReference.CompletionListener onComplete) {
        Map<String, Object> updates = new HashMap<>();
        String key;
        final App app = App.getInstance();
        RssWifi wifi;
        RssBtle btle;
        Rss btRss;
        Tof btHci, btJava;
        RssWifiRanging wifiRanging;
        RssBtleRanging btleRanging;
        RssRanging btRssRanging;
        TofRanging btHciRanging, btJavaRanging;

        //
        // Samples
        //
        
        for (String sample : app.rssWifi4gSamples) {
            wifi = new RssWifi(sample.split(","));
            key = db.child("rssWifi4gSamples").push().getKey();
            updates.put("/rssWifi4gSamples/" + key, wifi);
        }

        for (String sample : app.rssWifi5gSamples) {
            wifi = new RssWifi(sample.split(","));
            key = db.child("rssWifi5gSamples").push().getKey();
            updates.put("/rssWifi5gSamples/" + key, wifi);
        }

        for (String sample : app.rssBtSamples) {
            btRss = new Rss(sample.split(","));
            key = db.child("rssBtSamples").push().getKey();
            updates.put("/rssBtSamples/" + key, btRss);
        }

        for (String sample : app.rssBtleSamples) {
            btle = new RssBtle(sample.split(","));
            key = db.child("rssBtleSamples").push().getKey();
            updates.put("/rssBtleSamples/" + key, btle);
        }

        for (String sample : app.tofBtHciSamples) {
            btHci = new Tof(sample.split(","));
            key = db.child("tofBtHciSamples").push().getKey();
            updates.put("/tofBtHciSamples/" + key, btHci);
        }

        for (String sample : app.tofBtJavaSamples) {
            btJava = new Tof(sample.split(","));
            key = db.child("tofBtJavaSamples").push().getKey();
            updates.put("/tofBtJavaSamples/" + key, btJava);
        }

        //
        // Ranging
        //

        for (String range : app.rssWifi4gRanging) {
            wifiRanging = new RssWifiRanging(range.split(","));
            key = db.child("rssWifi4gRanging").child(app.rssWifi4gRangingKey).push().getKey();
            updates.put("/rssWifi4gRanging/" + app.rssWifi4gRangingKey + "/" + key, wifiRanging);
        }

        for (String range : app.rssWifi5gRanging) {
            wifiRanging = new RssWifiRanging(range.split(","));
            key = db.child("rssWifi5gRanging").child(app.rssWifi5gRangingKey).push().getKey();
            updates.put("/rssWifi5gRanging/" + app.rssWifi5gRangingKey + "/" + key, wifiRanging);
        }

        for (String range : app.rssBtRanging) {
            btRssRanging = new RssRanging(range.split(","));
            key = db.child("rssBtRanging").child(app.rssBtRangingKey).push().getKey();
            updates.put("/rssBtRanging/" + app.rssBtRangingKey + "/" + key, btRssRanging);
        }

        for (String range : app.rssBtleRanging) {
            btleRanging = new RssBtleRanging(range.split(","));
            key = db.child("rssBtleRanging").child(app.rssBtleRangingKey).push().getKey();
            updates.put("/rssBtleRanging/" + app.rssBtleRangingKey + "/" + key, btleRanging);
        }

        for (String range : app.tofBtHciRanging) {
            btHciRanging = new TofRanging(range.split(","));
            key = db.child("tofBtHciRanging").child(app.tofBtHciRangingKey).push().getKey();
            updates.put("/tofBtHciRanging/" + app.tofBtHciRangingKey + "/" + key, btHciRanging);
        }

        for (String range : app.tofBtJavaRanging) {
            btJavaRanging = new TofRanging(range.split(","));
            key = db.child("tofBtJavaRanging").child(app.tofBtJavaRangingKey).push().getKey();
            updates.put("/tofBtJavaRanging/" + app.tofBtJavaRangingKey + "/" + key, btJavaRanging);
        }

        addOldRangingSampleUpdates(updates);

        Log.d(TAG, "Submitting updates: " + updates);

        db.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.i(TAG, "Submitted samples successfully");
                    Toast.makeText(app, "Samples submitted.", Toast.LENGTH_SHORT).show();
                    app.rssWifi4gSamples.clear();
                    app.rssWifi5gSamples.clear();
                    app.rssBtSamples.clear();
                    app.rssBtleSamples.clear();
                    app.tofBtHciSamples.clear();
                    app.tofBtJavaSamples.clear();

                    app.rssWifi4gRanging.clear();
                    app.rssWifi5gRanging.clear();
                    app.rssBtRanging.clear();
                    app.rssBtleRanging.clear();
                    app.tofBtHciRanging.clear();
                    app.tofBtJavaRanging.clear();
                } else {
                    Log.e(TAG, "Failed to submit samples: " + databaseError.getMessage(), databaseError.toException());
                    Toast.makeText(app, "Error submitting samples.", Toast.LENGTH_LONG).show();
                }
                if (onComplete != null) onComplete.onComplete(databaseError, databaseReference);
            }
        });
    }

    private void addOldRangingSampleUpdates(Map<String, Object> updates) {
        
    }
}
