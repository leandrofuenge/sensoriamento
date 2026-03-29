package com.aeroambiental.sensoriamento.model;

/**
 * Representa o deslocamento vetorial entre duas posições consecutivas.
 */
public class DisplacementVector {

    private double deltaLatitude;    // diferença em graus (simplificado)
    private double deltaLongitude;   // diferença em graus (simplificado)
    private double deltaAltitude;    // metros

    private double magnitude;        // módulo do deslocamento (aproximado)
    private double orientation;      // ângulo/direção em graus

    public DisplacementVector() {
    }

    public DisplacementVector(double deltaLatitude, double deltaLongitude, double deltaAltitude,
                              double magnitude, double orientation) {
        this.deltaLatitude = deltaLatitude;
        this.deltaLongitude = deltaLongitude;
        this.deltaAltitude = deltaAltitude;
        this.magnitude = magnitude;
        this.orientation = orientation;
    }

    public double getDeltaLatitude() {
        return deltaLatitude;
    }

    public void setDeltaLatitude(double deltaLatitude) {
        this.deltaLatitude = deltaLatitude;
    }

    public double getDeltaLongitude() {
        return deltaLongitude;
    }

    public void setDeltaLongitude(double deltaLongitude) {
        this.deltaLongitude = deltaLongitude;
    }

    public double getDeltaAltitude() {
        return deltaAltitude;
    }

    public void setDeltaAltitude(double deltaAltitude) {
        this.deltaAltitude = deltaAltitude;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(double orientation) {
        this.orientation = orientation;
    }

    @Override
    public String toString() {
        return "DisplacementVector{" +
                "deltaLatitude=" + deltaLatitude +
                ", deltaLongitude=" + deltaLongitude +
                ", deltaAltitude=" + deltaAltitude +
                ", magnitude=" + magnitude +
                ", orientation=" + orientation +
                '}';
    }
}