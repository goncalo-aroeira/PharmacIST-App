package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import java.io.Serializable;

import pt.ulisboa.tecnico.cmov.pharmacist.elements.utils;

public class Pharmacy implements Serializable {

    private String id, name, address, imageBytes;
    Boolean isFavorite, isFlagged;
    double distance;

    public Pharmacy(String name, String address) {
        this.id= null;
        this.name = name;
        this.address = address;
        this.imageBytes = null;
        this.isFavorite = false;
        this.isFlagged = false;
    }

    public Pharmacy(String name, String address, String imageBytes) {
        this.id=null;
        this.name = name;
        this.address = address;
        this.imageBytes = imageBytes;
        this.isFavorite = false;
        this.isFlagged = false;
    }

    public Pharmacy(String id, String name, String address, String imageBytes) {
        this.id=id;
        this.name = name;
        this.address = address;
        this.imageBytes = imageBytes;
        this.isFavorite = false;
        this.isFlagged = false;
    }

    public void generateId() {
        this.id = utils.generateRandomId(5);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

    public String getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(String imageBytes) {
        this.imageBytes = imageBytes;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }

    public Boolean isFavorite() {
        return isFavorite;
    }

    public void setFlagged(Boolean flagged) {
        isFlagged = flagged;
    }

    public Boolean isFlagged() {
        return isFlagged;
    }

}