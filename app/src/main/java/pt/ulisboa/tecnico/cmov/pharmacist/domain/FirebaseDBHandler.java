package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class FirebaseDBHandler {
    private static final String MEDICINES_NODE = "Medicine";
    private static final String PHARMACIES_NODE = "Pharmacy";
    private static final String USER_NODE = "User";


    private final DatabaseReference databaseReference;

    public FirebaseDBHandler() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void addMedicine(Medicine medicine) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        medicinesRef.push().setValue(medicine);
    }

    public void removeMedicine(String medicineId) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        medicinesRef.child(medicineId).removeValue();
    }

    public void updateMedicine(String medicineId, Medicine updatedMedicine) {
        DatabaseReference medicinesRef = databaseReference.child(MEDICINES_NODE);
        medicinesRef.child(medicineId).setValue(updatedMedicine);
    }

    public void addPharmacy(Pharmacy pharmacy) {
        DatabaseReference pharmaciesRef = databaseReference.child(PHARMACIES_NODE);
        pharmaciesRef.push().setValue(pharmacy);
    }

    public void removePharmacy(String pharmacyId) {
        DatabaseReference pharmaciesRef = databaseReference.child(PHARMACIES_NODE);
        pharmaciesRef.child(pharmacyId).removeValue();
    }

    public void updatePharmacy(String pharmacyId, Pharmacy updatedPharmacy) {
        DatabaseReference pharmaciesRef = databaseReference.child(PHARMACIES_NODE);
        pharmaciesRef.child(pharmacyId).setValue(updatedPharmacy);
    }

    public void addUser(String userId, User user) {
        DatabaseReference pharmaciesRef = databaseReference.child(USER_NODE);
        pharmaciesRef.child(userId).setValue(user);
    }

}
