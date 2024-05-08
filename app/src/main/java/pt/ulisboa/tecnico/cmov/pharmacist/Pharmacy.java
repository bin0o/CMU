package pt.ulisboa.tecnico.cmov.pharmacist;

public class Pharmacy {
    private String name;
    private String address;

    // Required empty constructor for Firebase
    public Pharmacy() {}

    public Pharmacy(String name, String address) {
        this.name = name;
        this.address = address;
    }

    // Getter and setter methods
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
}


