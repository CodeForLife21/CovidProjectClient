package ie.com.covidproject;

public class Pubs {
    public double latitude;
    public double longitude;
    public String description;
    public int capacity;
    public int occupancy;

    public Pubs() {

    }



    public Pubs(double latitude, double longitude, String description, int capacity, int occupancy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.capacity = capacity;
        this.occupancy = occupancy;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(int occupancy) {
        this.occupancy = occupancy;
    }

    @Override
    public String toString() {
        return "Pubs{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", description='" + description + '\'' +
                ", capacity=" + capacity +
                ", occupancy=" + occupancy +
                '}';
    }
}

