package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.util.Log;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;


public class MedicineManager {

    private List<Medicine> medicines;
    FirebaseDBHandler dbHandler;

    public MedicineManager(FirebaseDBHandler dbHandler) {
        this.medicines = new ArrayList<>();
        this.dbHandler = dbHandler;
    }

    public List<Medicine> getMedicines() {
        return new ArrayList<>(medicines); // Defensive copy
    }


    public void setMedicines(List<Medicine> medicines) {
        this.medicines = new ArrayList<Medicine>(medicines); // Defensive copy
    }

    public void addMedicine(Medicine medicine) {
        medicines.add(medicine);
    }

    public void removeMedicine(Medicine medicine) {
        medicines.remove(medicine);
    }
}
