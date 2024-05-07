package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class FirebaseDBHandler {
    private static final String MEDICINES_NODE = "medicine";
    private static final String PHARMACIES_NODE = "pharmacy";
    private static final String USER_NODE = "user";
    private static final String FAVORITES_NODE = "favorites";

    private final DatabaseReference databaseReference;

    public FirebaseDBHandler() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void addMedicine(Medicine medicine, OnChangeListener listener) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        medicinesRef.push().setValue(medicine)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);

    }

    //Add medicine to pharmacy and quantity
    public void addMedicineToPharmacy(String pharmacyName, String medicineName, int quantity, OnChangeListener listener) {
        DatabaseReference pharmaciesRef = databaseReference.child(PHARMACIES_NODE);
        Query query = pharmaciesRef.orderByChild("name").equalTo(pharmacyName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot pharmacySnapshot : dataSnapshot.getChildren()) {
                    DatabaseReference medicineRef = pharmacySnapshot.child("inventory").getRef().push();
                    medicineRef.child("name").setValue(medicineName);
                    medicineRef.child("quantity").setValue(quantity);
                    listener.onSuccess();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                listener.onFailure(databaseError.toException());
            }
        });
    }

    public void getMedicineByName(String medicineName, OnMedicineLoadedListener listener) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        Query query = medicinesRef.orderByChild("name").equalTo(medicineName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Medicine medicine = snapshot.getValue(Medicine.class);
                    listener.onMedicineLoaded(medicine);
                }

                listener.onFailure(new Exception("Medicine not found"));
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }



    public void removeMedicine(String medicineId) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        medicinesRef.child(medicineId).removeValue();
    }

    public void updateMedicine(String medicineId, Medicine updatedMedicine) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        medicinesRef.child(medicineId).setValue(updatedMedicine);
    }

    public void addPharmacy(Pharmacy pharmacy, OnChangeListener listener) {
        DatabaseReference pharmacyRef = databaseReference.child(PHARMACIES_NODE);

        pharmacyRef.push().setValue(pharmacy)
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess();
                })
                .addOnFailureListener(listener::onFailure);
    };

    public void removePharmacy(String pharmacyName, OnChangeListener listener) {
        DatabaseReference pharmaciesRef = databaseReference.child(PHARMACIES_NODE);
        // Query the database to find the pharmacy with the specified name
        Query query = pharmaciesRef.orderByChild("name").equalTo(pharmacyName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                listener.onSuccess();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure(new Exception(databaseError.getMessage()));
            }
        });
    }

    public void getAllPharmacies(OnPharmaciesLoadedListener listener) {
        DatabaseReference pharmaciesRef = databaseReference.child(PHARMACIES_NODE);
        ArrayList<Pharmacy> pharmacyList = new ArrayList<Pharmacy>();
        pharmaciesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Pharmacy pharmacy = new Pharmacy(
                            snapshot.child("name").getValue(String.class),
                            snapshot.child("address").getValue(String.class)
                    );

                    snapshot.child("inventory").getChildren().forEach(medicineSnapshot -> {
                        Medicine medicine = new Medicine(
                                medicineSnapshot.child("name").getValue(String.class)
                                //print medicine
                        );
                        pharmacy.addMedicine(medicine, medicineSnapshot.child("quantity").getValue(Integer.class));
                    });

                    pharmacyList.add(pharmacy);
                }
                listener.onPharmaciesLoaded(pharmacyList);
            } else {
                listener.onFailure(Objects.requireNonNull(task.getException()));
            }
        });
    }

    public void getFavoritesPharmacies(String userEmail) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        Query query = usersRef.orderByChild("email").equalTo(userEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot pharmacySnapshot : userSnapshot.child(FAVORITES_NODE).getChildren()) {
                        String pharmacyAddress = pharmacySnapshot.getValue(String.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void addPharmacyToUserFavorite(String userEmail, String pharmacyAddress) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        Query query = usersRef.orderByChild("email").equalTo(userEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    userSnapshot.getRef().child(FAVORITES_NODE).push().setValue(pharmacyAddress);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                System.out.println("Failed to read user data: " + databaseError.toException());
            }
        });
    }

    public void removePharmacyFromFavorite(String userEmail, String pharmacyAddress) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        Query query = usersRef.orderByChild("email").equalTo(userEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot pharmacySnapshot : userSnapshot.child(FAVORITES_NODE).getChildren()) {
                        if (Objects.requireNonNull(pharmacySnapshot.getValue(String.class)).equals(pharmacyAddress)) {
                            pharmacySnapshot.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                System.out.println("Failed to read user data: " + databaseError.toException());
            }
        });
    }

    public void updatePharmacyByName(String pharmacyName, Pharmacy newPharmacyData) {
        // Get a reference to the "pharmacy" node
        DatabaseReference pharmacyRef = databaseReference.child(PHARMACIES_NODE);

        // Query the database to find the pharmacy with the specified name
        Query query = pharmacyRef.orderByChild("name").equalTo(pharmacyName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through the result set
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Update each pharmacy entry found
                    snapshot.getRef().child("name").setValue(newPharmacyData.getName());
                    snapshot.getRef().child("address").setValue(newPharmacyData.getAddress());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                System.out.println("Database Error: " + databaseError.getMessage());
            }
        });
    }






    public void addUser(User user) {
        DatabaseReference pharmaciesRef = databaseReference.child(USER_NODE);
        pharmaciesRef.push().setValue(user);
    }
    
    public void printAllUsers() {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        System.out.println("User: " + user.getName() + ", " + user.getEmail());
                        // You can print other user details as well
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                System.out.println("Failed to read user data: " + databaseError.toException());
            }
        });
    }

    // Method to get a user by email
    public void performLogin(String email, String passwordInput, final PasswordCallback callback) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        Query query = usersRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    User user = new User(userSnapshot.child("name").getValue(String.class), userSnapshot.child("email").getValue(String.class), userSnapshot.child("password").getValue(String.class));
                    String password = user.getPassword(); // Get the password
                    // Exit the loop : Wrong password
                    if (password.equals(passwordInput)) {
                        callback.onSuccessfulLogin(user);
                    } else {
                        callback.onWrongPassword();
                    }
                    return; // Exit the loop : Success
                }
                callback.onUserNotFound(); // User not found
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                System.out.println("Failed to read user data: " + databaseError.toException());
            }
        });
    }

    public void uploadImage(String base64Image, String node, OnImageSavedListener listener) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(node);
        databaseReference.push().setValue(base64Image)
                .addOnSuccessListener(aVoid -> listener.onImageSaved())
                .addOnFailureListener(aVoid -> listener.onFailure(new Exception("Failed to save image")));


    }


    public interface PasswordCallback {
        void onUserNotFound();

        void onSuccessfulLogin(User user);

        void onWrongPassword();

    }

    public interface OnImageSavedListener extends FirebaseDBHandlerListener {
        void onImageSaved();

    }

    public interface OnMedicineLoadedListener extends FirebaseDBHandlerListener {
        void onMedicineLoaded(Medicine medicine);
    }


    public interface FirebaseDBHandlerListener {
        void onFailure(Exception e);
    }

    public interface OnChangeListener extends FirebaseDBHandlerListener {
        void onSuccess();
    }

    public interface OnPharmaciesLoadedListener extends FirebaseDBHandlerListener {
        void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies);
    }

}


