package ie.com.covidproject;

import java.util.ArrayList;

public class Business {

    private String business_name;
    private int max_capacity;
    private String description;
    private Double latitude;
    private Double longitude;
    private int occupancy;
    private ArrayList<String> myTags;


    public Business() {

    }

    public Business(String business_name, int max_capacity, String description, Double latitude, Double longitude, int occupancy, ArrayList<String> myTags) {
        this.business_name = business_name;
        this.max_capacity = max_capacity;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.occupancy = occupancy;
        this.myTags = myTags;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    public int getMax_capacity() {
        return max_capacity;
    }

    public void setMax_capacity(int max_capacity) {
        this.max_capacity = max_capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(int occupancy) {
        this.occupancy = occupancy;
    }

    public ArrayList<String> getMyTags() {
        return myTags;
    }

    public void setMyTags(ArrayList<String> myTags) {
        this.myTags = myTags;
    }
}