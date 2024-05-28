package pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses;

public class Pharmacy {
    private String name;
    private String address;

    private String imageUrl;

    // Required empty constructor for Firebase
    public Pharmacy() {}

    public Pharmacy(String name, String address, String imageUrl) {
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() {return imageUrl; }

    public void setImageUrl(String imageUrl)  {this.imageUrl = imageUrl; }
}


