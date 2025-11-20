package com.example.asyncstoragedojo;

public class Observation {
    public final String observationTimeUtc;
    public final double waterLevel;
    public final double waterTemperature;

    public Observation(String observationTimeUtc, double waterLevel, double waterTemperature) {
        this.observationTimeUtc = observationTimeUtc;
        this.waterLevel = waterLevel;
        this.waterTemperature = waterTemperature;
    }
}
