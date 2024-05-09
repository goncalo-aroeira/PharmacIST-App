package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.widget.ImageView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Pharmacy implements Serializable {

    String name;
    String address; // Missing location on map
    ImageView picture;
    double distance;
    private HashMap<Medicine, Integer> inventory = new HashMap<Medicine, Integer>();


    public Pharmacy(String name, String address,  ImageView image) {
        this.name = name;
        this.address = address;
        this.picture = image;
    }

    public Pharmacy(String name, String address) {
        this.name = name;
        this.address = address;
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

    public ImageView getPicture() {
        return picture;
    }

    public void setPicture(ImageView picture) {
        this.picture = picture;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
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

    public HashMap<Medicine, Integer> getInventory() {
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