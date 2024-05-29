package pt.ulisboa.tecnico.cmov.pharmacist.helper;

import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Pharmacy;

public class PharmacyHelper {
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private float distance;

    // Getters and setters for all fields

    public void setPharmacy(Pharmacy pharmacy) {
        this.name = pharmacy.getName();
        this.address = pharmacy.getAddress();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "PharmacyHelper{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", distance=" + distance +
                '}';
    }
}
