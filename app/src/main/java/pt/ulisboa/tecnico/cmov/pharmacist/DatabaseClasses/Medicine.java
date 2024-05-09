package pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Medicine {

    private Map<String, Integer> pharmacies; // Map of pharmacy names to quantities

    public Medicine() {
        // Default constructor required for Firebase
    }

    public Medicine(Map<String, Integer> pharmacies) {
        this.pharmacies = pharmacies;
    }

    public Map<String, Integer> getPharmacies() {
        return pharmacies;
    }

    public void setPharmacies(Map<String, Integer> pharmacies) {
        this.pharmacies = pharmacies;
    }

    public List<String> getPharmacyNames() {
        return new ArrayList<>(pharmacies.keySet());
    }

    public int getQuantity(String medicineName) {
        return pharmacies.getOrDefault(medicineName, 0);
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "pharmacies=" + pharmacies +
                '}';
    }
}
