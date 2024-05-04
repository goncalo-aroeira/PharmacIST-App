package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import java.util.ArrayList;
import java.util.List;


public class MedicineManager {

    private List<Medicine> medicines;

    public MedicineManager() {
        this.medicines = new ArrayList<>();
    }

    public MedicineManager(List<Medicine> medicines) {
        this.medicines = new ArrayList<>(medicines); // Defensive copy
    }

    public List<Medicine> getMedicines() {
        return new ArrayList<>(medicines); // Defensive copy
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = new ArrayList<>(medicines); // Defensive copy
    }

    // Methods to manage medicines (can be named appropriately)
    public void addMedicine(Medicine medicine) {
        medicines.add(medicine);
    }

    public void removeMedicine(Medicine medicine) {
        medicines.remove(medicine);
    }
}
