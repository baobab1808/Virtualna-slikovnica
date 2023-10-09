package com.example.project.firebasemessaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.project.R;
import com.example.project.admin.PendingPicturebooks;
import com.example.project.archive.MyArchive;
import com.example.project.explore.Explore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

//firebase cloud messaging
public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    //receive data and prepare notification data to be shown
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        String notificationType = message.getData().get("notificationType");

        if(notificationType.equals("NewPicturebook")){
            String authorId = message.getData().get("userId");
            String adminId = message.getData().get("adminId");
            String picturebookId = message.getData().get("picturebookId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationMessage = message.getData().get("notificationMessage");

            if(firebaseUser != null && firebaseAuth.getUid().equals(adminId)){
                showNotification(picturebookId,adminId,authorId,notificationTitle,notificationMessage, notificationType);
            }

        }

        if(notificationType.equals("PicturebookStatusChanged")){
            String authorId = message.getData().get("userId");
            String adminId = message.getData().get("adminId");
            String picturebookId = message.getData().get("picturebookId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationMessage = message.getData().get("notificationMessage");

            if(firebaseUser != null && firebaseAuth.getUid().equals(authorId)){
                showNotification(picturebookId,adminId,authorId,notificationTitle,notificationMessage, notificationType);

            }
        }

        if(notificationType.equals("NewFollow")){
            String userId = message.getData().get("userId");
            String authorId = message.getData().get("authorId");
            String picturebookId = message.getData().get("picturebookId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationMessage = message.getData().get("notificationMessage");

            if(firebaseUser != null && firebaseAuth.getUid().equals(authorId)){
                showNotification(picturebookId,authorId,userId,notificationTitle,notificationMessage, notificationType);

            }
        }

    }

    //depending on type of notification show it and on click open different activity
    private void showNotification(String picturebookId, String adminId, String authorId, String notificationTitle, String notificationMessage, String notificationType){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = new Random().nextInt(3000);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setupNotificationChannel(notificationManager);
        }

        Intent intent = null;
        if(notificationType.equals("NewPicturebook")){
            intent = new Intent(this, PendingPicturebooks.class);
            intent.putExtra("picturebookId", picturebookId);
            intent.putExtra("userId", authorId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);


        }else if(notificationType.equals("PicturebookStatusChanged")){
            intent = new Intent(this, MyArchive.class);
            intent.putExtra("picturebookId", picturebookId);
            intent.putExtra("userId", authorId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        }else if(notificationType.equals("NewFollow")){
            intent = new Intent(this, Explore.class);
            intent.putExtra("userId", adminId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //look of notification
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.picturebook_not);
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.icona)
                .setLargeIcon(largeIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setSound(notificationSoundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    //setting up notification channel and options
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "Some Sample Text";
        String channelDescription = "Channel Description here";
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);

        if(notificationManager != null){
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
