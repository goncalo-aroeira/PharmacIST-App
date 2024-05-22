package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.util.Log;

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



    /* =============================================================================================
                                           MEDICINE METHODS
      ============================================================================================= */

    public void addMedicine(Medicine medicine, OnChangeListener listener) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        medicinesRef.child(medicine.getId()).setValue(medicine)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

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


    public void addNewMedicineIfNotExists(Medicine medicine, OnChangeListener listener) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        Query query = medicinesRef.orderByChild("name").equalTo(medicine.getName());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Medicine does not exist, add it
                    medicinesRef.push().setValue(medicine).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onSuccess();
                        } else {
                            listener.onFailure(task.getException());
                        }
                    });
                } else {
                    // Here handle the case when the medicine already exists
                    listener.onFailure(new Exception("Medicine already exists in the list"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    public void getMedicineById(String id, OnMedicineLoadedListener listener) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        Query query = medicinesRef.orderByKey().equalTo(id);
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

    public void getAllMedicines(OnMedicinesLoadedListener listener) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        ArrayList<Medicine> medicineList = new ArrayList<>();
        medicinesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Medicine medicine = new Medicine(
                            snapshot.child("name").getValue(String.class),
                            snapshot.child("usage").getValue(String.class)
                    );
                    if (medicine != null) {
                        medicineList.add(medicine);
                    }
                }
                listener.onMedicinesLoaded(medicineList);
            } else {
                listener.onFailure(task.getException());
            }
        });
    }


    /* =============================================================================================
                                           PHARMACY METHODS
      ============================================================================================= */
    public void addPharmacy(Pharmacy pharmacy, OnChangeListener listener) {
        DatabaseReference pharmacyRef = databaseReference.child(PHARMACIES_NODE);
        pharmacyRef.child(pharmacy.getId()).setValue(pharmacy)
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
                            snapshot.child("id").getValue(String.class),
                            snapshot.child("name").getValue(String.class),
                            snapshot.child("address").getValue(String.class),
                            snapshot.child("imageBytes").getValue(String.class)
                    );

                    snapshot.child("inventory").getChildren().forEach(medicineSnapshot -> {
                        Medicine medicine = new Medicine(
                                medicineSnapshot.child("name").getValue(String.class)
                        );
                        pharmacy.addMedicine(medicine, medicineSnapshot.child("quantity").getValue(Integer.class));
                        // print medicine name
                        Log.d("FirebaseDBHandler", "Medicine: " + medicine.getName());
                        Log.d("FirebaseDBHandler", "Pharmacy: " + pharmacy.getName() + " Inventory: " + pharmacy.getInventory().get(medicine));
                    });

                    pharmacyList.add(pharmacy);
                }
                listener.onPharmaciesLoaded(pharmacyList);
            } else {
                listener.onFailure(Objects.requireNonNull(task.getException()));
            }
        });
    }

    public void getFavoritesPharmacies(String userEmail, OnGetFavoritesPharmacies listener) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        Query query = usersRef.orderByChild("email").equalTo(userEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<String> addressList = new ArrayList<String>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot pharmacySnapshot : userSnapshot.child(FAVORITES_NODE).getChildren()) {
                        addressList.add(pharmacySnapshot.getValue(String.class));

                    }
                    listener.OnPharmaciesLoadedSuccessfully(addressList);
                    listener.onFailure(new Exception("Failed to get the pharmacy"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                 listener.onFailure(databaseError.toException());
            }
        });
    }


    public void toggleFavoriteStatus(String userEmail, String pharmacyAddress, OnFavoriteToggleListener listener) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        Query query = usersRef.orderByChild("email").equalTo(userEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot favoritesSnapshot = userSnapshot.child(FAVORITES_NODE);
                    boolean isAlreadyFavorite = false;
                    String favoriteKeyToRemove = null;

                    for (DataSnapshot favorite : favoritesSnapshot.getChildren()) {
                        if (favorite.getValue(String.class).equals(pharmacyAddress)) {
                            isAlreadyFavorite = true;
                            favoriteKeyToRemove = favorite.getKey();
                            break;
                        }
                    }

                    if (isAlreadyFavorite && favoriteKeyToRemove != null) {
                        favoritesSnapshot.getRef().child(favoriteKeyToRemove).removeValue()
                                .addOnSuccessListener(aVoid -> listener.onRemovedFromFavorite())
                                .addOnFailureListener(e -> listener.onFailure(e));
                    } else if (!isAlreadyFavorite) {
                        favoritesSnapshot.getRef().push().setValue(pharmacyAddress)
                                .addOnSuccessListener(aVoid -> listener.onAddedToFavorite())
                                .addOnFailureListener(e -> listener.onFailure(e));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }


    /* =============================================================================================
                                           USER METHODS
      ============================================================================================= */
    public void performLogin(String email, String passwordInput, final PasswordCallback callback) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        Query query = usersRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    User user = new User(
                            userSnapshot.child("id").getValue(String.class),
                            userSnapshot.child("name").getValue(String.class),
                            userSnapshot.child("email").getValue(String.class),
                            userSnapshot.child("password").getValue(String.class));
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

    
    public void registerUser(User user, OnRegistrationListener listener) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);

        Query emailQuery = usersRef.orderByChild("email").equalTo(user.getEmail());
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    listener.onEmailExists();
                } else {
                    // Check for username if necessary
                    Query usernameQuery = usersRef.orderByChild("username").equalTo(user.getName());
                    usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                listener.onUsernameExists();
                            } else {
                                // Proceed with registration
                                usersRef.child(user.getId()).setValue(user)
                                        .addOnSuccessListener(aVoid -> listener.onRegistrationSuccess())
                                        .addOnFailureListener(listener::onRegistrationFailure);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            listener.onRegistrationFailure(databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onRegistrationFailure(databaseError.toException());
            }
        });
    }

    public void upgradeAccount(User user, OnChangeListener listener) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        Query query = usersRef.orderByKey().equalTo(user.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    userSnapshot.child("email").getRef().setValue(user.getEmail());
                    userSnapshot.child("password").getRef().setValue(user.getPassword());
                    if (user.getName() != null) {
                        userSnapshot.child("name").getRef().setValue(user.getName());
                    } else {
                        userSnapshot.child("name").getRef().setValue(user.getEmail());
                    }
                    listener.onSuccess();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });

    }





    /* =============================================================================================
                                           LISTENER INTERFACES
      ============================================================================================= */
    public interface PasswordCallback {
        void onUserNotFound();

        void onSuccessfulLogin(User user);

        void onWrongPassword();

    }

    public interface OnImageSavedListener extends FirebaseDBHandlerListener {
        void onImageSaved(String imageName);

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

    public interface OnGetFavoritesPharmacies extends FirebaseDBHandlerListener {
        void OnPharmaciesLoadedSuccessfully(ArrayList<String> pharmacies);
    }
    public interface OnFavoriteToggleListener {
        void onAddedToFavorite();
        void onRemovedFromFavorite();
        void onFailure(Exception e);
    }

    public interface OnRegistrationListener {
        void onRegistrationSuccess();
        void onRegistrationFailure(Exception e);
        void onEmailExists();
        void onUsernameExists();
    }

    
    public interface OnMedicinesLoadedListener extends FirebaseDBHandlerListener {
        void onMedicinesLoaded(ArrayList<Medicine> medicines);

    }

}


