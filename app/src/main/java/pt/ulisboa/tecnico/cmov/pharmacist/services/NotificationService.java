package pt.ulisboa.tecnico.cmov.pharmacist.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;

public class NotificationService extends Service {

    private static final String TAG = "NotificationService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "PharmacIST";

    private FirebaseDBHandler dbHandler;
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");

        // Create a notification channel if Android version is Oreo or above
        createNotificationChannel();
        dbHandler = new FirebaseDBHandler();

        UserLocalStore userLocalStore = new UserLocalStore(this);
        String userId = userLocalStore.getLoggedInId();

        startService(new Intent(this, NotificationService.class));

        // Initialize handler and runnable
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                dbHandler.checkNotifications(userId, new FirebaseDBHandler.OnNotificationCheckListener() {

                    @Override
                    public void onNotificationAvailable(String medicineName, String pharmacyName, int quantity) {
                        Notification notification = createNotification(medicineName, pharmacyName);
                        NotificationManager manager = getSystemService(NotificationManager.class);
                        manager.notify(NOTIFICATION_ID, notification);
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Log.e(TAG, "Failed to check notifications", exception);
                    }
                });

                // Schedule the next execution after 1 second
                handler.postDelayed(this, 1000);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");

        // Start the task runnable
        handler.post(runnable);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service onDestroy");

        // Remove the callback to stop the task
        handler.removeCallbacks(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification(String medicineName, String pharmacyName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Medicine Available")
                .setContentText("Medicine " + medicineName + " is now available at " + pharmacyName + "!")
                .setSmallIcon(R.mipmap.ic_launcher);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
