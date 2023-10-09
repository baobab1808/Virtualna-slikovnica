package com.example.project.notificationsettings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.Constants;
import com.example.project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;


//Activity for enabling/disabling push notifications
public class NotificationSettings extends AppCompatActivity {

    SwitchCompat fcmSwitch;
    TextView notificationStatus;

    private static final String enabledMessage = "Notifications are enabled";
    private static final String disabledMessage = "Notifications are disabled";

    boolean isChecked = false;

    FirebaseAuth firebaseAuth;

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Project);
        setContentView(R.layout.activity_notification_settings);

        fcmSwitch = findViewById(R.id.fcmSwitch);
        notificationStatus = findViewById(R.id.notficationStatus);

        firebaseAuth = FirebaseAuth.getInstance();

        sp = getSharedPreferences("SETTINGS_SP", MODE_PRIVATE);
        isChecked = sp.getBoolean("FCM_ENABLED", false);
        fcmSwitch.setChecked(isChecked);

        if(isChecked){
            notificationStatus.setText(enabledMessage);
        }else {
            notificationStatus.setText(disabledMessage);
        }

        fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    subscribeToTopic();
                }else{
                    unSubscribeToTopic();
                }
            }
        });
    }

    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                spEditor = sp.edit();
                spEditor.putBoolean("FCM_ENABLED", true);
                spEditor.apply();

                Toast.makeText(NotificationSettings.this, ""+enabledMessage, Toast.LENGTH_SHORT).show();
                notificationStatus.setText(enabledMessage);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NotificationSettings.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unSubscribeToTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                spEditor = sp.edit();
                spEditor.putBoolean("FCM_ENABLED", false);
                spEditor.apply();

                Toast.makeText(NotificationSettings.this, ""+disabledMessage, Toast.LENGTH_SHORT).show();
                notificationStatus.setText(disabledMessage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NotificationSettings.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}