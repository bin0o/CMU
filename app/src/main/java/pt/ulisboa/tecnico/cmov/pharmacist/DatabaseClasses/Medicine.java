package pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Medicine {

    private List<String> pharmacies; // Map of pharmacy names to quantities

    public Medicine() {
        // Default constructor required for Firebase
    }

    public Medicine(List<String> pharmacies) {
        this.pharmacies = pharmacies;
    }


    public void setPharmacies(List<String> pharmacies) {
        this.pharmacies = pharmacies;
    }

    public List<String> getPharmacyNames() {
        return pharmacies;
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "pharmacies=" + pharmacies +
                '}';
    }
}
