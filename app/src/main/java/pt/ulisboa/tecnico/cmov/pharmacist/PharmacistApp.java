package pt.ulisboa.tecnico.cmov.pharmacist;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class PharmacistApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Firebase persistence only once here
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
