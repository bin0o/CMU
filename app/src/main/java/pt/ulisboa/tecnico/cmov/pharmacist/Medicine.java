package pt.ulisboa.tecnico.cmov.pharmacist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Medicine {
    private String pharmacyName;

    private String name;

    public Medicine() {}

    public Medicine(String pharmacyName, String name) {
        this.pharmacyName = pharmacyName;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

}
