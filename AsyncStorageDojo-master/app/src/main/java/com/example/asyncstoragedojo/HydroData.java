package com.example.asyncstoragedojo;

import java.util.List;

public class HydroData {
    public final Station station;
    public final List<Observation> observations;

    public HydroData(Station station, List<Observation> observations) {
        this.station = station;
        this.observations = observations;
    }
}