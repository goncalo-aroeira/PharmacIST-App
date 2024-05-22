package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import java.util.ArrayList;

public class User {

    String name, password, email, id;
    ArrayList<Pharmacy> favoritePharmacy;

    public User(String name, String email, String password) {
        this.id = null;
        this.name = name;
        this.email = email;
        this.password = password;
        this.favoritePharmacy = new ArrayList<Pharmacy>();
    }

    public User(String id, String name, String email, String password) {
        this.id = id;
        this.name = email;
        this.email = email;
        this.password = password;
        this.favoritePharmacy = new ArrayList<Pharmacy>();
    }

    public void generateId() {
        this.id = java.util.UUID.randomUUID().toString();
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
}
