package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.util.Log;

import java.util.ArrayList;


public class PharmacyManager {

    ArrayList<Pharmacy> pharmacies = new ArrayList<Pharmacy>();
    FirebaseDBHandler dbHandler;

    public PharmacyManager(FirebaseDBHandler dbHandler) {
        this.dbHandler = dbHandler;
        this.pharmacies = new ArrayList<>();
    }

    public void loadPharmacies(OnPharmaciesLoadedListener listener) {
        this.dbHandler.getAllPharmacies(new FirebaseDBHandler.OnPharmaciesLoadedListener() {
            @Override
            public void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies) {
                setPharmacies(pharmacies);
                Log.d("Debug", "PharmacyManager: " + pharmacies.size() + " pharmacies loaded.");
                listener.onPharmaciesLoaded(pharmacies);

            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Error", "Failed to load pharmacies", e);
                listener.onPharmaciesLoadFailed(e);
            }
        });
    }

    public ArrayList<Pharmacy> getPharmacies() {
        return pharmacies;
    }

    private void setPharmacies(ArrayList<Pharmacy> pharmacies) {
        this.pharmacies = pharmacies;
    }

    // Method to manage pharmacies (can be named appropriately)
    public void addPharmacy(Pharmacy pharmacy, OnPharmaciesAddListener listener) {
        this.dbHandler.addPharmacy(pharmacy, new FirebaseDBHandler.OnChangeListener() {
            @Override
            public void onSuccess() {
                pharmacies.add(pharmacy);
                listener.onPharmaciesAdd();
            }

            @Override
            public void onFailure(Exception e) {
                listener.onPharmaciesAddFailed(e);
            }
        });
    }

    public void removePharmacy(Pharmacy pharmacy) {
        this.dbHandler.removePharmacy(pharmacy.getName(), new FirebaseDBHandler.OnChangeListener() {
            @Override
            public void onSuccess() {
                pharmacies.remove(pharmacy);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Error", "Failed to remove pharmacy", e);
            }
        });
    }

    public Pharmacy getPharmacyByName(String name) {
        return pharmacies.stream().filter(pharmacy -> pharmacy.getName().equals(name)).findFirst().orElse(null);
    }

    public interface OnPharmaciesLoadedListener {
        void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies);

        void onPharmaciesLoadFailed(Exception e);
    }

    public interface OnPharmaciesAddListener {
        void onPharmaciesAdd();

        void onPharmaciesAddFailed(Exception e);
    }

}
