package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import java.util.ArrayList;
import java.util.List;


public class PharmacyManager {

    ArrayList<Pharmacy> pharmacies;

    public PharmacyManager() {
        this.pharmacies = new ArrayList<>();
    }

    public PharmacyManager(List<Pharmacy> pharmacies) {
        this.pharmacies = new ArrayList<>(pharmacies); // Defensive copy to avoid modifying original list
    }

    public List<Pharmacy> getPharmacies() {
        return new ArrayList<>(pharmacies); // Defensive copy to avoid modifying original list
    }

    public void setPharmacies(List<Pharmacy> pharmacies) {
        this.pharmacies = new ArrayList<>(pharmacies); // Defensive copy to avoid modifying original list
    }

    // Method to manage pharmacies (can be named appropriately)
    public void addPharmacy(Pharmacy pharmacy) {
        pharmacies.add(pharmacy);
    }

    public void removePharmacy(Pharmacy pharmacy) {
        pharmacies.remove(pharmacy);
    }

}
