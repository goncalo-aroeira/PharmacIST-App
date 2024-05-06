package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import java.util.ArrayList;

public class User {

    String name, password, email;
    ArrayList<Pharmacy> favoritePharmacy;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.favoritePharmacy = new ArrayList<Pharmacy>();
    }

    public User(String name, String password) {

        this.password = password;
        this.name = name;
        this.favoritePharmacy = new ArrayList<Pharmacy>();
    }
    public User() {
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
