package pt.ulisboa.tecnico.cmov.pharmacist;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

import pt.ulisboa.tecnico.cmov.pharmacist.services.NotificationService;

public class PharmacistApp extends Application {

    private static final String TAG = "PharmacistApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Log to indicate the app has started
        Log.d(TAG, "Application onCreate: starting");

        // Enable Firebase persistence only once here
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Log.d(TAG, "Firebase persistence enabled");

        Intent serviceIntent = new Intent(this, NotificationService.class);
        this.startService(serviceIntent);

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creating notification channel");

            CharSequence name = "Pharmacist";
            String description = "Pharmacist Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Pharmacist", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Log.d(TAG, "Notification channel created with name: " + name);
        }
    }
}
