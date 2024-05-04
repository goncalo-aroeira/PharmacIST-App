package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import java.util.ArrayList;

public class User {

    String name, username, password;
    ArrayList<Pharmacy> favoritePharmacy;

    public User(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.favoritePharmacy = new ArrayList<Pharmacy>();
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.name = username;
        this.favoritePharmacy = new ArrayList<Pharmacy>();
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
}
