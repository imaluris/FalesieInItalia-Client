package com.example.falesieinitalia;

public class Crag {
    private String name;
    private String city;
    private String region;
    private String description;
    private String image;
    private String type;

    public Crag(String name, String city, String region, String description, String image, String type){
        this.name = name;
        this.city = city;
        this.region = region;
        this.description = description;
        this.image = image;
        this.type = type;
    }

    public String getName(){
        return name;
    }

    public String getCity(){
        return city;
    }

    public String getDescription(){
        return description;
    }

    public String getRegion(){
        return region;
    }

    public String getImage(){
        return image;
    }

    public String getType() {return type;}


}
