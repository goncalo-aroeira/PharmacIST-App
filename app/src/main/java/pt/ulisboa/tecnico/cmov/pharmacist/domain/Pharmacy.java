package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.widget.ImageView;

import java.util.Map;
import java.util.Objects;

public class Pharmacy {

    String name;
    String address; // Missing location on map
    Boolean isFavorite;
    ImageView picture;
    private Map<Medicine, Integer> inventory;

    public Pharmacy(String name, String address, Boolean isFavorite, ImageView image) {
        this.name = name;
        this.address = address;
        this.isFavorite = isFavorite;
        this.picture = image;
    }

    public Pharmacy(String name, String address) {
        this.name = name;
        this.address = address;
        this.isFavorite = false;
        this.picture = null;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public ImageView getPicture() {
        return picture;
    }

    public void setPicture(ImageView picture) {
        this.picture = picture;
    }

    public void addMedicine(Medicine medicine, Integer quantity) {
        inventory.put(medicine, quantity);
    }

    public void increaseMedicineQuantity(Medicine medicine, Integer addValue) {
        if (addValue > 0) {
            Integer amount = inventory.get(medicine);
            inventory.put(medicine, amount + addValue);
        }
    }

    public Map<Medicine, Integer> getInventory() {
        return this.inventory;
    }

    public Integer getMedicineAmount(Medicine medicine) {
        return inventory.get(medicine);
    }

    public void decreaseMedicineQuantity(Medicine medicine, Integer removeValue) {
        Integer amount = inventory.get(medicine);
        if (Objects.equals(amount, removeValue)) {
            removeMedicine(medicine);
        } else if (amount < removeValue) {
            inventory.put(medicine, amount - removeValue);
        }
    }

    public void removeMedicine(Medicine medicine) {
        inventory.remove(medicine);
    }
}