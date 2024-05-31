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
import java.util.Map;
import java.util.Objects;


public class FirebaseDBHandler {
    private static final String MEDICINES_NODE = "medicine";
    private static final String PHARMACIES_NODE = "pharmacy";
    private static final String USER_NODE = "user";
    private static final String FAVORITES_NODE = "favorite_pharmacies";
    private static final String INVENTORY_NODE = "inventory";
    private static final String NOTIFICATIONS_NODE = "notifications";
    private static final String FLAGGED_NODE = "flagged_pharmacies";

    private static final String FLAGGED_BY_NODE = "flagged_by";

    private final String TAG = "FirebaseDBHandler";

    private final DatabaseReference databaseReference;

    public FirebaseDBHandler() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);
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
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Medicine medicine = new Medicine(
                            snapshot.child("id").getValue(String.class),
                            snapshot.child("name").getValue(String.class),
                            snapshot.child("usage").getValue(String.class)
                    );

                    if (snapshot.child("imageBytes").exists()) {
                        medicine.setImageBytes(snapshot.child("imageBytes").getValue(String.class));
                    }


                    Log.d(TAG, "Listener Medicine: " + medicine.getName());
                    listener.onMedicineLoaded(medicine);
                }
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
                            snapshot.child("id").getValue(String.class),
                            snapshot.child("name").getValue(String.class),
                            snapshot.child("usage").getValue(String.class)
                    );
                    if (snapshot.child("imageBytes").exists()) {
                        medicine.setImageBytes(snapshot.child("imageBytes").getValue(String.class));
                    }
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

                if (userSnapshot.hasChild(FAVORITES_NODE)) {
                    for (DataSnapshot favoriteSnapshot : userSnapshot.child(FAVORITES_NODE).getChildren()) {

                        favoritePharmacies.add(favoriteSnapshot.getKey());
                        Log.d("loadPharmacies", "Loaded favorite pharmacy ID: " + favoriteSnapshot.getKey());
                    }
                } else {
                    Log.d("loadPharmacies", "No favorite pharmacies found");
                }

                if (userSnapshot.hasChild(FLAGGED_NODE)) {
                    for (DataSnapshot flaggedSnapshot : userSnapshot.child(FLAGGED_NODE).getChildren()) {
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
                                //newPharmacy.setFlagged(true);
                                Log.d("loadPharmacies", "Pharmacy " + newPharmacy.getName() + " is flagged and hidden.");
                            } else {
                                allPharmacies.add(newPharmacy);
                            }
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

    public void getPharmacyById(String id, OnPharmacyLoadedListener listener) {
        DatabaseReference pharmacyRef = databaseReference.child(PHARMACIES_NODE).child(id);
        pharmacyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Pharmacy pharmacy = new Pharmacy(
                        dataSnapshot.child("id").getValue(String.class),
                        dataSnapshot.child("name").getValue(String.class),
                        dataSnapshot.child("address").getValue(String.class),
                        dataSnapshot.child("imageBytes").getValue(String.class)
                );
                listener.onPharmacyLoaded(pharmacy);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    

    }


    public void toggleFavoriteStatus(String userId, String pharmacyId, OnFavoriteToggleListener listener) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        Query query = usersRef.orderByKey().equalTo(userId);

        Log.d("toggleFavoriteStatus", "Toggling favorite status for user: " + userId + ", pharmacy: " + pharmacyId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.w("toggleFavoriteStatus", "User not found in database.");
                    return; // Exit if the user doesn't exist
                }
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.child(FAVORITES_NODE).hasChild(pharmacyId)) {
                        Log.d("toggleFavoriteStatus", "Removing pharmacy from favorites.");
                        userSnapshot.child(FAVORITES_NODE).child(pharmacyId).getRef().removeValue()
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
                        userSnapshot.child(FAVORITES_NODE).child(pharmacyId).getRef().setValue(true)
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
                            userSnapshot.child("password").getValue(String.class),
                            userSnapshot.child("suspended").getValue(Boolean.class));
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
        Log.d("RegisterUser", "Registering user: " + user.getId() + " " + user.getName() + " " + user.getEmail() + " " + user.getPassword());


        if (user.getId() == null) {
            // Handle null ID (generate new ID, return error, etc.)
            // Generating new ID for the user
            user.generateId();
            Log.d("RegisterUser", "Generated new user ID: " + user.getId());
        }

        // Query to check if the email already exists
        Query emailQuery = usersRef.orderByChild("email").equalTo(user.getEmail());
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Email already exists
                    Log.d("RegisterUser", "Email already exists: " + user.getEmail());
                    listener.onEmailExists();
                } else {
                    // Email does not exist, check for username if necessary
                    Log.d("RegisterUser", "Email does not exist, proceeding to check username: " + user.getEmail());

                    // Query to check if the username already exists
                    Query usernameQuery = usersRef.orderByChild("name").equalTo(user.getName());
                    usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Username already exists
                                Log.d("RegisterUser", "Username already exists: " + user.getName());
                                listener.onUsernameExists();
                            } else {
                                // Proceed with registration
                                Log.d("RegisterUser", "Username does not exist, proceeding with registration: " + user.getName());
                                DatabaseReference ref = usersRef.child(user.getId());
                                ref.child("id").setValue(user.getId());
                                ref.child("name").setValue(user.getName());
                                ref.child("email").setValue(user.getEmail());
                                ref.child("password").setValue(user.getPassword());
                                if (user.getFlaggedPharmacies() != null) {
                                    ref.child(FLAGGED_NODE).setValue(user.getFlaggedPharmacies());
                                }
                                if (user.getFavoritesPharmacies() != null) {
                                    ref.child(FAVORITES_NODE).setValue(user.getFavoritesPharmacies());
                                }
                                ref.child("suspended").setValue(false).addOnSuccessListener(
                                                aVoid -> {
                                                    // User registered successfully
                                                    Log.d("RegisterUser", "User registered successfully: " + user.getId());
                                                    listener.onRegistrationSuccess();
                                                })
                                        .addOnFailureListener(e -> {
                                            Log.e("RegisterUser", "Failed to register user: " + user.getId(), e);
                                            listener.onRegistrationFailure(e);
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Username check cancelled due to an error
                            Log.e("RegisterUser", "Username check cancelled: ", databaseError.toException());
                            listener.onRegistrationFailure(databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Email check cancelled due to an error
                Log.e("RegisterUser", "Email check cancelled: ", databaseError.toException());
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
        //look for pharmacyId and medicineId
        Query query = inventoryRef.orderByChild("pharmacyId").equalTo(pharmacyId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("medicineId").getValue(String.class).equals(medicineId)) {
                        int currentQuantity = snapshot.child("quantity").getValue(Integer.class);
                        int newQuantity = currentQuantity + quantity;
                        snapshot.child("quantity").getRef().setValue(newQuantity);
                        listener.onSuccess();
                        return;
                    }
                }
                // If the medicine is not found in the inventory, add it
                inventoryRef.child("pharmacyId").setValue(pharmacyId);
                inventoryRef.child("medicineId").setValue(medicineId);
                inventoryRef.child("quantity").setValue(quantity)
                        .addOnSuccessListener(aVoid -> listener.onSuccess())
                        .addOnFailureListener(listener::onFailure);
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
    public void getInventoryForMedicine(Medicine medicine, OnGetInventory listener) {
        DatabaseReference inventoryRef = databaseReference.child(INVENTORY_NODE);
        Query query = inventoryRef.orderByChild("medicineId").equalTo(medicine.getId());
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

    public void purchaseMedicineFromPharmacy(String medicineId, String pharmacyId, int quantity, OnPurchaseMedicineListener listener) {
        DatabaseReference inventoryRef = databaseReference.child(INVENTORY_NODE);
        Query query = inventoryRef.orderByChild("pharmacyId").equalTo(pharmacyId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("medicineId").getValue(String.class).equals(medicineId)) {
                        int currentQuantity = snapshot.child("quantity").getValue(Integer.class);

                        if (currentQuantity < quantity) {
                            listener.onNotEnoughStock();
                        }
                        else if (currentQuantity == quantity) {
                            snapshot.getRef().removeValue();
                            listener.onSuccess();

                        } else {
                            int newQuantity = currentQuantity - quantity;
                            snapshot.child("quantity").getRef().setValue(newQuantity);
                            listener.onSuccess();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

     /* ============================================================================================
                                                NOTIFICATIONS
      ============================================================================================= */


    public void checkNotificationExists(String medicineId, String userId, OnCheckNotificationExists listener) {
        DatabaseReference notificationsRef = databaseReference.child(USER_NODE).child(userId).child(NOTIFICATIONS_NODE);
        Query query = notificationsRef.orderByKey().equalTo(medicineId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listener.onExists(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    public void toggleNotification(String userId, String medicineId, OnNotificationToggleListener listener) {
        DatabaseReference usersRef = databaseReference.child(USER_NODE);
        Query query = usersRef.orderByKey().equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("toggleNotification", "onDataChange triggered.");
                if (!dataSnapshot.exists()) {
                    Log.d("toggleNotification", "User does not exist: " + userId);
                    return; // Exit if the user doesn't exist
                }
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Log.d("toggleNotification", "Processing user: " + userSnapshot.getKey());
                    if (userSnapshot.child(NOTIFICATIONS_NODE).hasChild(medicineId)) {
                        Log.d("toggleNotification", "Notification exists for medicine: " + medicineId);
                        userSnapshot.child(NOTIFICATIONS_NODE).child(medicineId).getRef().removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("toggleNotification", "Notification removed successfully for medicine: " + medicineId);
                                        listener.onRemovedNotification();
                                    } else {
                                        Log.e("toggleNotification", "Failed to remove notification for medicine: " + medicineId, task.getException());
                                        listener.onFailure(task.getException());
                                    }
                                });
                    } else {
                        Log.d("toggleNotification", "Notification does not exist for medicine: " + medicineId);
                        userSnapshot.child(NOTIFICATIONS_NODE).child(medicineId).getRef().setValue(true)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("toggleNotification", "Notification added successfully for medicine: " + medicineId);
                                        listener.onAddedNotification();
                                    } else {
                                        Log.e("toggleNotification", "Failed to add notification for medicine: " + medicineId, task.getException());
                                        listener.onFailure(task.getException());
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("toggleNotification", "Database access cancelled.", databaseError.toException());
                listener.onFailure(databaseError.toException());
            }
        });
    }


    /* ============================================================================================
                                           META-DATA CONTROL
      ============================================================================================= */


    public void flagPharmacy(String userId, String pharmacyId, OnFlaggedPharmacy listener) {
        DatabaseReference userRef = databaseReference.child(USER_NODE).child(userId);
        DatabaseReference pharmacyRef = databaseReference.child(PHARMACIES_NODE).child(pharmacyId);

        Log.d("Flagging", "Attempting to flag pharmacy: " + pharmacyId + " by user: " + userId);

        userRef.child(FLAGGED_NODE).child(pharmacyId).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Flagging", "Successfully flagged pharmacy: " + pharmacyId);
                checkAndHandleUserSuspension(userId, listener);
            } else {
                Log.e("Flagging", "Failed to flag pharmacy: " + pharmacyId, task.getException());
                listener.onFailure(task.getException());
            }
        });

        pharmacyRef.child(FLAGGED_BY_NODE).child(userId).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Flagging", "Successfully flagged pharmacy: " + pharmacyId);
                checkAndHandlePharmacySuspension(pharmacyId, listener);
            } else {
                Log.e("Flagging", "Failed to flag pharmacy: " + pharmacyId, task.getException());
                listener.onFailure(task.getException());
            }
        });
    }

    private void checkAndHandleUserSuspension(String userId, OnFlaggedPharmacy listener) {
        DatabaseReference userRef = databaseReference.child(USER_NODE).child(userId).child(FLAGGED_NODE);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long flagCount = dataSnapshot.getChildrenCount();
                Log.d("SuspensionCheck", "User " + userId + " flag count: " + flagCount);

                if (flagCount > 5) {  // Check if flags exceed the limit
                    DatabaseReference userStatusRef = databaseReference.child(USER_NODE).child(userId).child("suspended");
                    userStatusRef.setValue(true).addOnSuccessListener(aVoid -> {
                        Log.d("SuspensionCheck", "User " + userId + " has been suspended.");
                        listener.onAccountSuspended();
                    }).addOnFailureListener(e -> {
                        Log.e("SuspensionCheck", "Failed to suspend user " + userId, e);
                        listener.onFailure(e);
                    });
                } else {
                    listener.onSuccess();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SuspensionCheck", "Failed during user suspension check", databaseError.toException());
                listener.onFailure(databaseError.toException());
            }
        });
    }

    private void checkAndHandlePharmacySuspension(String pharmacyId, OnFlaggedPharmacy listener) {
        DatabaseReference pharmacyRef = databaseReference.child(PHARMACIES_NODE).child(pharmacyId).child(FLAGGED_BY_NODE);

        pharmacyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long flagCount = dataSnapshot.getChildrenCount();
                Log.d("SuspensionCheck", "Pharmacy " + pharmacyId + " flag count: " + flagCount);

                if (flagCount >= 1) {  // Assuming suspension if flagged more than 3 times
                    // Delete the pharmacy entirely from the database
                    DatabaseReference pharmacyToDeleteRef = databaseReference.child(PHARMACIES_NODE).child(pharmacyId);
                    pharmacyToDeleteRef.removeValue().addOnSuccessListener(aVoid -> {
                        Log.d("SuspensionCheck", "Pharmacy " + pharmacyId + " has been deleted due to suspension.");
                        listener.onPharmacySuspended();
                    }).addOnFailureListener(e -> {
                        Log.e("SuspensionCheck", "Failed to delete pharmacy " + pharmacyId, e);
                        listener.onFailure(e);
                    });
                } else {
                    listener.onSuccess();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SuspensionCheck", "Failed during pharmacy suspension check", databaseError.toException());
                listener.onFailure(databaseError.toException());
            }
        });
    }


    public void checkNotifications(String userId, OnNotificationCheckListener listener) {
        DatabaseReference userRef = databaseReference.child(USER_NODE).child(userId);
        DatabaseReference inventoryRef = databaseReference.child(INVENTORY_NODE);

        // Fetch user data for notifications and favorites
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Boolean> notifications = (Map<String, Boolean>) dataSnapshot.child(NOTIFICATIONS_NODE).getValue();
                Map<String, Boolean> favoritePharmacies = (Map<String, Boolean>) dataSnapshot.child(FAVORITES_NODE).getValue();

                if (notifications == null || favoritePharmacies == null) {
                    listener.onFailure(new Exception("No notifications or favorite pharmacies set"));
                    return;
                }

                // Check inventory for each medicine the user wants notifications about
                for (String medicineId : notifications.keySet()) {
                    for (String pharmacyId : favoritePharmacies.keySet()) {
                        Query inventoryQuery = inventoryRef.orderByChild("medicineId").equalTo(medicineId);
                        inventoryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot inventorySnapshot) {
                                for (DataSnapshot snapshot : inventorySnapshot.getChildren()) {
                                    String inventoryPharmacyId = snapshot.child("pharmacyId").getValue(String.class);
                                    if (pharmacyId.equals(inventoryPharmacyId)) {
                                        int quantity = snapshot.child("quantity").getValue(Integer.class);
                                        if (quantity > 0) {
                                            // Fetch pharmacy and medicine details
                                            getPharmacyById(pharmacyId, new OnPharmacyLoadedListener() {
                                                @Override
                                                public void onPharmacyLoaded(Pharmacy pharmacy) {
                                                    getMedicineById(medicineId, new OnMedicineLoadedListener() {
                                                        @Override
                                                        public void onMedicineLoaded(Medicine medicine) {
                                                            listener.onNotificationAvailable(medicine.getName(), pharmacy.getName(), quantity);
                                                        }

                                                        @Override
                                                        public void onFailure(Exception e) {
                                                            listener.onFailure(e);
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    listener.onFailure(e);
                                                }
                                            });
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                listener.onFailure(databaseError.toException());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }


    public interface OnNotificationCheckListener {
        void onNotificationAvailable(String medicineName, String pharmacyName, int quantity);

        void onFailure(Exception exception);
    }




    /* ============================================================================================
                                           SEARCH DYNAMICALLY METHODS
      ============================================================================================= */

    public void getMedicineNames(OnMedicineNamesAndIdsLoaded listener) {
        DatabaseReference medicinesRef = databaseReference.child("medicine");
        Query query = medicinesRef.orderByChild("name"); // Assuming 'name' is a field under each medicine entry

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> medicineNamesAndIds = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String medicineName = snapshot.child("name").getValue(String.class);
                    String medicineId = snapshot.getKey(); // Assuming the key of the snapshot is the medicine ID
                    if (medicineName != null && !medicineNamesAndIds.containsKey(medicineName)) {
                        medicineNamesAndIds.put(medicineName, medicineId);
                    }
                }
                listener.onLoaded(medicineNamesAndIds);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onError(databaseError.toException());
            }
        });
    }

    public interface OnMedicineNamesAndIdsLoaded {
        void onLoaded(HashMap<String, String> medicineNamesAndIds);
        void onError(Exception e);
    }



    public interface OnPharmaciesWithMedicineLoaded {
        void onLoaded(HashMap<Pharmacy, Integer> pharmacies);
        void onError(Exception e);
    }

    public void getPharmaciesWithMedicine(String medicineName, OnPharmaciesWithMedicineLoaded listener) {
        DatabaseReference inventoryRef = databaseReference.child("inventory");
        DatabaseReference pharmaciesRef = databaseReference.child("pharmacy");

        // Log to start fetching process
        Log.d(TAG, "Starting to fetch pharmacies that have the medicine: " + medicineName);

        HashMap<String, Integer> pharmacyQuantities = new HashMap<>();

        // Fetching pharmacies that have the specified medicine in their inventory
        Query query = inventoryRef.orderByChild("medicineId").equalTo(medicineName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot inventorySnapshot : dataSnapshot.getChildren()) {
                    String pharmacyId = inventorySnapshot.child("pharmacyId").getValue(String.class);
                    Integer quantity = inventorySnapshot.child("quantity").getValue(Integer.class);

                    Log.d(TAG, "Pharmacy ID: " + pharmacyId + ", Quantity: " + quantity);

                    if (pharmacyId != null && quantity != null) {
                        pharmacyQuantities.put(pharmacyId, pharmacyQuantities.getOrDefault(pharmacyId, 0) + quantity);
                    } else {
                        Log.d(TAG, "Null value found for pharmacy ID or quantity at inventory: " + inventorySnapshot.getKey());
                    }
                }

                Log.d(TAG, "Fetched " + pharmacyQuantities.size() + " pharmacies with their medicine quantities.");

                //Query pharmacyQuery = pharmaciesRef.orderByKey().equalsTo(pharmacyQuantities.keySet();
                // Now fetching the details of these pharmacies
                HashMap<Pharmacy, Integer> resultPharmacies = new HashMap<>();
                final int[] queryCount = {pharmacyQuantities.size()}; // This is the number of asynchronous operations you expect to complete.

                for (String pharmacyId : pharmacyQuantities.keySet()) {
                    Query pharmacyQuery = pharmaciesRef.orderByKey().equalTo(pharmacyId);
                    Log.d(TAG, "Fetching pharmacy details for pharmacy ID: " + pharmacyId);
                    pharmacyQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot pharmaciesSnapshot) {
                            if (pharmaciesSnapshot.exists()) {
                                Pharmacy pharmacy = new Pharmacy(
                                        pharmaciesSnapshot.child(pharmacyId).child("name").getValue(String.class),
                                        pharmaciesSnapshot.child(pharmacyId).child("address").getValue(String.class)
                                );
                                pharmacy.setId(pharmacyId);
                                resultPharmacies.put(pharmacy, pharmacyQuantities.get(pharmacyId));

                                Log.d(TAG, "Pharmacy ID: " + pharmacy.getId() + ", Name: " + pharmacy.getName() + ", Quantity: " + pharmacyQuantities.get(pharmacyId) + " units");
                            } else {
                                Log.d(TAG, "Pharmacy ID not found in the database: " + pharmacyId);
                            }

                            // Decrement the count and check if it's time to call the final listener
                            synchronized (this) {
                                queryCount[0]--;
                                if (queryCount[0] == 0) {
                                    Log.d(TAG, "Fetched " + resultPharmacies.size() + " pharmacies with their medicine quantities.");
                                    listener.onLoaded(resultPharmacies);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Database error on fetching pharmacy details: " + error.getMessage());
                            listener.onError(error.toException());
                            synchronized (this) {
                                queryCount[0]--;
                                if (queryCount[0] == 0) {
                                    listener.onLoaded(resultPharmacies);
                                }
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error on fetching inventory data: " + databaseError.getMessage());
                listener.onError(databaseError.toException());
            }
        });
    }



    /* ============================================================================================
                                           LISTENER INTERFACES
      ============================================================================================= */


    public interface PasswordCallback {
        void onUserNotFound();

        void onSuccessfulLogin(User user);

        void onWrongPassword();

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

    public interface OnPharmacyLoadedListener extends FirebaseDBHandlerListener {
        void onPharmacyLoaded(Pharmacy pharmacy);
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

    public interface OnFlaggedPharmacy extends FirebaseDBHandlerListener{
        void onSuccess();  // Called when the pharmacy is successfully flagged
        void onPharmacySuspended();  // Called when the pharmacy is suspended due to excessive flags
        void onAccountSuspended();  // Called when the user account is suspended due to excessive flags
        void onFailure(Exception e);  // Called when there is an error in the flagging process
    }

    public interface OnPurchaseMedicineListener extends FirebaseDBHandlerListener {
        void onNotEnoughStock();

        void onSuccess();
    }

    public interface OnNotificationToggleListener extends FirebaseDBHandlerListener {
        void onAddedNotification();

        void onRemovedNotification();
    }

    public interface OnLoadUserNotifications extends FirebaseDBHandlerListener {
        void onLoaded(ArrayList<String> medicine_ids);
    }

    public interface OnCheckNotificationExists extends FirebaseDBHandlerListener {
        void onExists(boolean exists);
    }

}


