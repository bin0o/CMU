package pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses;

public class Pharmacy {
    private String name;
    private String address;

    private String imageBase64;

    // Required empty constructor for Firebase
    public Pharmacy() {}

    public Pharmacy(String name, String address, String imageBase64) {
        this.name = name;
        this.address = address;
        this.imageBase64 = imageBase64;
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

    public String getImageBase64() {return imageBase64; }

    public void setImageBase64(String imageBase64)  {this.imageBase64 = imageBase64; }
}


