package com.kwaou.library.firebase;

/**
 * Created by Manish on 11/22/2016.
 */

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.kwaou.library.ComplaintActivity;
import com.kwaou.library.R;
import com.kwaou.library.activities.RequestActivity;
import com.kwaou.library.activities.MainActivity;
import com.kwaou.library.helper.Config;
import com.kwaou.library.models.BookPackage;
import com.kwaou.library.models.Complaint;
import com.kwaou.library.models.Notification;
import com.kwaou.library.models.User;
import com.kwaou.library.sqlite.KeyValueDb;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private int notification_id = 0, message_id = 0;
    private String CHANNEL_ID = "borrow";
    String TYPE;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        KeyValueDb.set(getApplicationContext(), Config.USER_TOKEN,s,1);
        Log.d("Refreshed Token", s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d("onMessageReceived","Check");
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
            try {

                Log.d("Try","Just got into try");
                JSONObject json = new JSONObject(remoteMessage.getData().toString());


                parseNotificationData(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }

    }


    //this method will display the notification
    //We are passing the JSONObject that is received from
    //firebase cloud messaging
    private void parseNotificationData(JSONObject json) {
        //optionally we can display the json into log
        Log.e(TAG, "Notification JSON " + json.toString());
        try {
            //getting the json data
            JSONObject data = json.getJSONObject("data");
            JSONObject payload = data.getJSONObject("payload");

            String message = payload.getString("message");
            String type = payload.getString("type");

            String title = "";
            switch (type){
                case Config.EXCHANGE_REQUEST:
                    TYPE = Config.EXCHANGE_REQUEST;
                    title = "New book exchange request received";
                    break;
                case Config.EXCHANGE_REQUEST_ACCEPT:
                    TYPE = Config.EXCHANGE_REQUEST_ACCEPT;
                    title = "Exchange request has been accepted";
                    break;

                case Config.EXCHANGAE_REQUEST_REJECT:
                    TYPE = Config.EXCHANGAE_REQUEST_REJECT;
                    title = "Exchange request has been rejected";
                    break;
                case Config.SALE:
                    TYPE = Config.SALE;
                    title = "Sold your book";
                    break;
                case Config.COMPLAINT_REPLY:
                    TYPE = Config.COMPLAINT_REPLY;
                    title = "Your complaint has been replied.";
                    break;

            }


            showNotification(title, message);

        }
        catch (Exception e) {
            Log.e("Exception",e.getMessage());
        }
    }

    private void showNotification(String title, String message) throws JSONException {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);

        Gson gson =new Gson();
        User from = null;
        if(TYPE.equals(Config.EXCHANGE_REQUEST) ||
            TYPE.equals(Config.EXCHANGE_REQUEST_ACCEPT )||TYPE.equals(Config.EXCHANGAE_REQUEST_REJECT)) {
            JSONObject jsonObject = new JSONObject(message);
            JSONObject fromUser = jsonObject.getJSONObject("from");
             JSONObject old = jsonObject.getJSONObject("old");
             Log.d(TAG, old.toString());
             JSONObject newjson = jsonObject.getJSONObject("new");
             BookPackage oldBook = gson.fromJson(old.toString(), BookPackage.class);
             BookPackage newBook = gson.fromJson(newjson.toString(), BookPackage.class);
             from = gson.fromJson(fromUser.toString(), User.class);
             message = "I want to replace set of my " + oldBook.getBookArrayList().size() + " books "+ " with your set of " + newBook.getBookArrayList().size() + " books.";

             intent = new Intent(this, RequestActivity.class);
        }else if(TYPE.equals(Config.SALE)){
            JSONObject jsonObject = new JSONObject(message);
            JSONObject fromUser = jsonObject.getJSONObject("from");
            JSONObject old = jsonObject.getJSONObject("old");
            Log.d(TAG, old.toString());
            BookPackage oldBook = gson.fromJson(old.toString(), BookPackage.class);
            from = gson.fromJson(fromUser.toString(), User.class);
            message = "Your set of " + oldBook.getBookArrayList().size() + " books has been bought by" +
                    " " +  from.getName() + ". Please contact admin for your payment.";

        }else if(TYPE.equals(Config.COMPLAINT_REPLY)){
            Complaint complaint = gson.fromJson(message, Complaint.class);
            message = "Your complaint " + complaint.getTitle() + " has been replied";
            intent = new Intent(this, ComplaintActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String userid = KeyValueDb.get(getApplicationContext(), Config.USERID,"");
        saveNotification(title, message, userid, from);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setShowWhen(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notification_id, mBuilder.build());
    }

    private void saveNotification(String title, String message, String userid, User from) {
        DatabaseReference notRefs = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_NOTIFICATIONS);
        String id = notRefs.push().getKey();
        Notification notification = new Notification(id, title, message, getTime(), userid, from);
        notRefs.child(id).setValue(notification);
    }

    public String getTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm ", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }


}

