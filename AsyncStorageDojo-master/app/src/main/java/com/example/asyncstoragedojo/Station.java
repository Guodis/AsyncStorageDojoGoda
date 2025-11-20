package com.example.asyncstoragedojo;

public class Station {
    public final String code;
    public final String name;
    public final String waterBody;
    public final Coordinates coordinates;

    public Station(String code, String name, String waterBody, Coordinates coordinates) {
        this.code = code;
        this.name = name;
        this.waterBody = waterBody;
        this.coordinates = coordinates;
    }
}
