package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.pharmacist.elements.utils;

public class User {

    String name, password, email, id;
    Boolean isGuest, isSuspended;
    ArrayList<Pharmacy> favoritePharmacy;
    ArrayList<Pharmacy> flaggedPharmacy;

    ArrayList<Medicine> notificationMedicine;

    public User(String name, String email, String password) {
        this.id = null;
        this.name = name;
        this.email = email;
        this.password = password;
        this.favoritePharmacy = new ArrayList<Pharmacy>();
        this.flaggedPharmacy = new ArrayList<Pharmacy>();
        this.notificationMedicine = new ArrayList<Medicine>();
        this.isGuest = name.equals("Guest");

    }

    public User(String id, String name, String email, String password) {
        this.id = id;
        this.name = email;
        this.email = email;
        this.password = password;
        this.favoritePharmacy = new ArrayList<Pharmacy>();
        this.flaggedPharmacy = new ArrayList<Pharmacy>();
        this.notificationMedicine = new ArrayList<Medicine>();
        this.isGuest = name.equals("Guest");
    }

    public User(String id, String name, String email, String password, Boolean isSuspended) {
        this.id = id;
        this.name = email;
        this.email = email;
        this.password = password;
        this.favoritePharmacy = new ArrayList<Pharmacy>();
        this.flaggedPharmacy = new ArrayList<Pharmacy>();
        this.notificationMedicine = new ArrayList<Medicine>();
        this.isGuest = name.equals("Guest");
        this.isSuspended = isSuspended;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void generateId() {
        this.id = utils.generateRandomId(5);
    }

    public String getId() {
        return this.id;
    }

    public void addPharmacyToFavorites(Pharmacy pharmacy) {
        this.favoritePharmacy.add(pharmacy);
    }

    public void removePharmacyFromFavorites(Pharmacy pharmacy) {
        this.favoritePharmacy.remove(pharmacy);
    }

    public void addPharmacyToFlagged(Pharmacy pharmacy) {
        this.flaggedPharmacy.add(pharmacy);
    }

    public void removePharmacyFromFlagged(Pharmacy pharmacy) {
        this.flaggedPharmacy.remove(pharmacy);
    }

    public ArrayList<Pharmacy> getFlaggedPharmacies() {
        return this.flaggedPharmacy;
    }

    public ArrayList<Pharmacy> getFavoritesPharmacies() {
        return this.favoritePharmacy;
    }

    public Boolean isFavorite(Pharmacy pharmacy) {
        return this.favoritePharmacy.contains(pharmacy);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Boolean isGuest() {
        return isGuest;
    }

    public Boolean isSuspended() {
        return isSuspended;
    }

    public void setSuspended(Boolean suspended) {
        isSuspended = suspended;
    }

    public void addNotificationMedicine(Medicine medicine) {
        this.notificationMedicine.add(medicine);
    }

    public void removeNotificationMedicine(Medicine medicine) {
        this.notificationMedicine.remove(medicine);
    }

}
