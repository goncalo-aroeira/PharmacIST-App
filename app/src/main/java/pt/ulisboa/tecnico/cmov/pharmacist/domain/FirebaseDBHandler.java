package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class FirebaseDBHandler {
    private static final String MEDICINES_NODE = "medicine";
    private static final String PHARMACIES_NODE = "pharmacy";
    private static final String USER_NODE = "user";
    private static final String FAVORITES_NODE = "favorites";

    private static final String INVENTORY_NODE = "inventory";

    private static final String NOTIFICATIONS_NODE = "notifications";
    private static final String FLAGGED_NODE = "flagged_pharmacies";

    private final DatabaseReference databaseReference;
    private ArrayList<Pharmacy> allPharmacies;

    public FirebaseDBHandler() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        allPharmacies = new ArrayList<>();
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


    public void getMedicineById(String id, OnMedicineLoadedListener listener) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        Query query = medicinesRef.orderByKey().equalTo(id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // print
                Log.d("FirebaseDBHandler", "Medicine ID: " + id);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Medicine medicine = new Medicine(
                            snapshot.child("id").getValue(String.class),
                            snapshot.child("name").getValue(String.class),
                            snapshot.child("usage").getValue(String.class)
                    );

                    if (snapshot.child("imageBytes").exists()) {
                        medicine.setImageBytes(snapshot.child("imageBytes").getValue(String.class));
                    }

                    Log.d("FirebaseDBHandler", "Medicine: " + medicine.getName());
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
        ArrayList<Medicine> allMedicines = new ArrayList<>();
        medicinesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Medicine medicine = new Medicine(
                            snapshot.child("name").getValue(String.class),
                            snapshot.child("usage").getValue(String.class)
                    );
                    allMedicines.add(medicine);
                }
                listener.onMedicinesLoaded(allMedicines);
            } else {
                listener.onFailure(task.getException());
            }
        });
    }


    /* =============================================================================================
                                           PHARMACY METHODS
      ============================================================================================= */
    public void addPharmacy(Pharmacy pharmacy, OnChangeListener listener) {
        DatabaseReference pharmacyRef = databaseReference.child(PHARMACIES_NODE).child(pharmacy.getId());
        pharmacyRef.child("id").setValue(pharmacy.getId());
        pharmacyRef.child("name").setValue(pharmacy.getName());
        pharmacyRef.child("address").setValue(pharmacy.getAddress());
        pharmacyRef.child("imageBytes").setValue(pharmacy.getImageBytes())
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess();
                })
                .addOnFailureListener(listener::onFailure);
    };


    public void loadPharmacies(String userId, OnPharmaciesLoadedListener listener) {
        DatabaseReference userRef = databaseReference.child(USER_NODE).child(userId);
        DatabaseReference pharmaciesRef = databaseReference.child(PHARMACIES_NODE);

        ArrayList<String> favoritePharmacies = new ArrayList<>();
        ArrayList<String> flaggedPharmacies = new ArrayList<>();

        Log.d("loadPharmacies", "Loading user data for user ID: " + userId);

        userRef.get().addOnCompleteListener(userTask -> {
            if (userTask.isSuccessful()) {
                DataSnapshot userSnapshot = userTask.getResult();

                if (userSnapshot.hasChild("favorite_pharmacies")) {
                    for (DataSnapshot favoriteSnapshot : userSnapshot.child("favorite_pharmacies").getChildren()) {
                        favoritePharmacies.add(favoriteSnapshot.getKey());
                        Log.d("loadPharmacies", "Loaded favorite pharmacy ID: " + favoriteSnapshot.getKey());
                    }
                } else {
                    Log.d("loadPharmacies", "No favorite pharmacies found");
                }

                if (userSnapshot.hasChild("flagged_pharmacies")) {
                    for (DataSnapshot flaggedSnapshot : userSnapshot.child("flagged_pharmacies").getChildren()) {
                        flaggedPharmacies.add(flaggedSnapshot.getKey());
                        Log.d("loadPharmacies", "Loaded flagged pharmacy ID: " + flaggedSnapshot.getKey());
                    }
                } else {
                    Log.d("loadPharmacies", "No flagged pharmacies found");
                }

                pharmaciesRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Pharmacy> allPharmacies = new ArrayList<>();
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            Pharmacy newPharmacy = new Pharmacy(
                                    snapshot.child("id").getValue(String.class),
                                    snapshot.child("name").getValue(String.class),
                                    snapshot.child("address").getValue(String.class),
                                    snapshot.child("imageBytes").getValue(String.class)
                            );
                            if (favoritePharmacies.contains(newPharmacy.getId())) {
                                newPharmacy.setFavorite(true);
                                Log.d("loadPharmacies", "Pharmacy " + newPharmacy.getName() + " is set as favorite.");
                            }

                            if (flaggedPharmacies.contains(newPharmacy.getId())) {
                                newPharmacy.setFlagged(true);
                                Log.d("loadPharmacies", "Pharmacy " + newPharmacy.getName() + " is flagged and hidden.");
                            }

                            allPharmacies.add(newPharmacy);
                        }

                        Log.d("loadPharmacies", "Pharmacies loaded successfully. Count: " + allPharmacies.size());
                        listener.onPharmaciesLoaded(allPharmacies);

                    } else {
                        Log.e("loadPharmacies", "Error loading pharmacies", task.getException());
                        listener.onFailure(Objects.requireNonNull(task.getException()));
                    }
                });
            } else {
                Log.e("loadPharmacies", "Error loading user data", userTask.getException());
                listener.onFailure(Objects.requireNonNull(userTask.getException()));
            }
        });
    }


    public ArrayList<Pharmacy> getPharmacies() {
        return allPharmacies;
    }

    public Pharmacy getPharmacyById(String id) {
        for (Pharmacy pharmacy : allPharmacies) {
            if (pharmacy.getId().equals(id)) {
                return pharmacy;
            }
        }
        return null;
    }



    public void toggleFavoriteStatus(String userId, String pharmacyAddress, OnFavoriteToggleListener listener) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        Query query = usersRef.orderByKey().equalTo(userId);

        Log.d("toggleFavoriteStatus", "Toggling favorite status for user: " + userId + ", pharmacy: " + pharmacyAddress);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.w("toggleFavoriteStatus", "User not found in database.");
                    return; // Exit if the user doesn't exist
                }
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.child(FAVORITES_NODE).hasChild(pharmacyAddress)) {
                        Log.d("toggleFavoriteStatus", "Removing pharmacy from favorites.");
                        userSnapshot.child(FAVORITES_NODE).child(pharmacyAddress).getRef().removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("toggleFavoriteStatus", "Pharmacy removed from favorites successfully.");
                                        listener.onRemovedFromFavorite();
                                    } else {
                                        Log.e("toggleFavoriteStatus", "Failed to remove pharmacy from favorites.", task.getException());
                                        listener.onFailure(task.getException());
                                    }
                                });
                    } else {
                        Log.d("toggleFavoriteStatus", "Adding pharmacy to favorites.");
                        userSnapshot.child(FAVORITES_NODE).child(pharmacyAddress).getRef().setValue(true)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("toggleFavoriteStatus", "Pharmacy added to favorites successfully.");
                                        listener.onAddedToFavorite();
                                    } else {
                                        Log.e("toggleFavoriteStatus", "Failed to add pharmacy to favorites.", task.getException());
                                        listener.onFailure(task.getException());
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("toggleFavoriteStatus", "Database access cancelled.", databaseError.toException());
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

        if (user.getId() == null) {
            // Handle null ID (generate new ID, return error, etc.)
            user.generateId();
        }

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
                                           INVENTORY METHODS
      ============================================================================================= */
    public void addMedicineToPharmacyInventory(String pharmacyId, String medicineId, int quantity, OnChangeListener listener) {
        DatabaseReference inventoryRef = databaseReference.child(INVENTORY_NODE).push();
        inventoryRef.child("pharmacyId").setValue(pharmacyId);
        inventoryRef.child("medicineId").setValue(medicineId);
        inventoryRef.child("quantity").setValue(quantity)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public void removeMedicineFromPharmacyInventory(String pharmacyId, String medicineId, OnChangeListener listener) {
        DatabaseReference inventoryRef = databaseReference.child(INVENTORY_NODE);
        Query query = inventoryRef.orderByChild("pharmacyId").equalTo(pharmacyId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("medicineId").getValue(String.class).equals(medicineId)) {
                        snapshot.getRef().removeValue().addOnCompleteListener(task -> listener.onSuccess());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    // Get the inventory of a pharmacy
    public void getInventoryForPharmacy(String pharmacyId, OnGetInventory listener) {
        DatabaseReference inventoryRef = databaseReference.child(INVENTORY_NODE);
        Query query = inventoryRef.orderByChild("pharmacyId").equalTo(pharmacyId);
        HashMap<String, Integer> inventory = new HashMap<>();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String medicineId = snapshot.child("medicineId").getValue(String.class);
                    int quantity = snapshot.child("quantity").getValue(Integer.class);
                    inventory.put(medicineId, quantity);
                }
                listener.onInventoryLoaded(inventory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    // Get the inventory of a medicine across all pharmacies
    public void getInventoryForMedicine(String medicineId, OnGetInventory listener) {
        DatabaseReference inventoryRef = databaseReference.child(INVENTORY_NODE);
        Query query = inventoryRef.orderByChild("medicineId").equalTo(medicineId);
        HashMap<String, Integer> inventory = new HashMap<>();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String pharmacyId = snapshot.child("pharmacyId").getValue(String.class);
                    int quantity = snapshot.child("quantity").getValue(Integer.class);
                    inventory.put(pharmacyId, quantity);
                }
                listener.onInventoryLoaded(inventory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

     /* ============================================================================================
                                                NOTIFICATIONS
      ============================================================================================= */

    public void addNotification(String medicineId, String pharmacyId, String userId, OnChangeListener listener) {
        DatabaseReference notificationsRef = databaseReference.child(NOTIFICATIONS_NODE).push();
        notificationsRef.child("medicineId").setValue(medicineId);
        notificationsRef.child("pharmacyId").setValue(pharmacyId);
        notificationsRef.child("userId").setValue(userId)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public void getNotificationsForUser(String userId, OnChangeListener listener) {
        DatabaseReference notificationsRef = databaseReference.child(NOTIFICATIONS_NODE);
        Query query = notificationsRef.orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String medicineId = snapshot.child("medicineId").getValue(String.class);
                    String pharmacyId = snapshot.child("pharmacyId").getValue(String.class);
                    listener.onSuccess();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    public void removeNotification(String medicineId, String pharmacyId, String userId, OnChangeListener listener) {
        DatabaseReference notificationsRef = databaseReference.child(NOTIFICATIONS_NODE);
        Query query = notificationsRef.orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("medicineId").getValue(String.class).equals(medicineId) &&
                            snapshot.child("pharmacyId").getValue(String.class).equals(pharmacyId)) {
                        snapshot.getRef().removeValue().addOnCompleteListener(task -> listener.onSuccess());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    public void getNotificationByPharmacyAndMedicine(String medicineId, String pharmacyId, String userId, OnChangeListener listener) {
        DatabaseReference notificationsRef = databaseReference.child(NOTIFICATIONS_NODE);
        Query query = notificationsRef.orderByChild("medicineId").equalTo(medicineId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("medicineId").getValue(String.class).equals(medicineId) &&
                            snapshot.child("pharmacyId").getValue(String.class).equals(pharmacyId)) {
                        listener.onSuccess();
                    }
                }
                listener.onFailure(new Exception("Notification not found"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }





    /* ============================================================================================
                                           META-DATA CONTROL
      ============================================================================================= */
    public void flagPharmacy(String userId, String pharmacyId, OnFlaggedPharmacy listener) {
        // On user side
        DatabaseReference usersRef = databaseReference.child(USER_NODE).child(userId);
        usersRef.child("flagged_pharmacies").child(pharmacyId).setValue(pharmacyId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long flaggedCount = dataSnapshot.child("flagged_pharmacies").getChildrenCount();
                        if (flaggedCount > 5) {
                            usersRef.child("suspended").setValue(true);
                            listener.onAccountSuspended();
                        }
                        listener.onSuccess();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailure(databaseError.toException());
                    }
                });
            } else {
                listener.onFailure(task.getException());
            }
        });

        // On pharmacy side
        DatabaseReference pharmaciesRef = databaseReference.child(PHARMACIES_NODE);
        pharmaciesRef.child("flagged_by").child(userId).setValue(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pharmaciesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long flaggedByCount = dataSnapshot.child("flagged_by").getChildrenCount();
                        if (flaggedByCount > 3) {
                            pharmaciesRef.child("suspended").setValue(true);
                        }
                        listener.onSuccess();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailure(databaseError.toException());
                    }
                });
            } else {
                listener.onFailure(task.getException());
            }
        });

    }


    /* ============================================================================================
                                           SEARCH DYNAMICALLY METHODS
      ============================================================================================= */
    public void searchPharmaciesWithMedicine(String searchQuery, OnPharmaciesWithMedicineListener listener) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        Query query = medicinesRef.orderByChild("name").startAt(searchQuery).endAt(searchQuery + "\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot medicinesSnapshot) {
                ArrayList<String> medicineIds = new ArrayList<>();
                for (DataSnapshot snapshot : medicinesSnapshot.getChildren()) {
                    String medicineId = snapshot.getKey();
                    medicineIds.add(medicineId);
                }

                if (medicineIds.isEmpty()) {
                    listener.onPharmaciesFound(new ArrayList<>());
                    return;
                }

                fetchPharmaciesByMedicineIds(medicineIds, listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    private void fetchPharmaciesByMedicineIds(ArrayList<String> medicineIds, OnPharmaciesWithMedicineListener listener) {
        DatabaseReference inventoryRef = databaseReference.child(INVENTORY_NODE);
        ArrayList<String> pharmacyIds = new ArrayList<>();

        for (String medicineId : medicineIds) {
            Query query = inventoryRef.orderByChild("medicine_id").equalTo(medicineId);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot inventorySnapshot) {
                    for (DataSnapshot snapshot : inventorySnapshot.getChildren()) {
                        String pharmacyId = snapshot.child("pharmacy_id").getValue(String.class);
                        if (!pharmacyIds.contains(pharmacyId)) {
                            pharmacyIds.add(pharmacyId);
                        }
                    }

                    if (pharmacyIds.isEmpty()) {
                        listener.onPharmaciesFound(new ArrayList<>());
                    } else {
                        fetchPharmacies(pharmacyIds, listener);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    listener.onFailure(databaseError.toException());
                }
            });
        }
    }

    private void fetchPharmacies(ArrayList<String> pharmacyIds, OnPharmaciesWithMedicineListener listener) {
        DatabaseReference pharmaciesRef = databaseReference.child(PHARMACIES_NODE);
        ArrayList<Pharmacy> pharmacies = new ArrayList<>();

        for (String pharmacyId : pharmacyIds) {
            pharmaciesRef.child(pharmacyId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot pharmacySnapshot) {
                    Pharmacy pharmacy = pharmacySnapshot.getValue(Pharmacy.class);
                    if (pharmacy != null) {
                        pharmacies.add(pharmacy);
                    }

                    if (pharmacies.size() == pharmacyIds.size()) {
                        listener.onPharmaciesFound(pharmacies);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    listener.onFailure(databaseError.toException());
                }
            });
        }
    }

    /* ============================================================================================
                                           LISTENER INTERFACES
      ============================================================================================= */
    public interface OnPharmaciesWithMedicineListener {
        void onPharmaciesFound(ArrayList<Pharmacy> pharmacies);
        void onFailure(Exception e);
    }

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

    public interface OnGetInventory extends FirebaseDBHandlerListener {
        void onInventoryLoaded(HashMap<String, Integer> inventory);

    }

    public interface OnFlaggedPharmacy extends FirebaseDBHandlerListener {
        void onSuccess();

        void onAccountSuspended();
    }

}


