package com.aeroambiental.sensoriamento.model;


/**
 * RF06 - Representa o deslocamento vetorial entre duas posições consecutivas.
 */
public class VetorDeslocamento {

    private double deltaLatitude;    // diferença em graus
    private double deltaLongitude;   // diferença em graus
    private double deltaAltitude;    // metros

    private double magnitude;        // módulo do deslocamento em metros
    private double orientacao;       // ângulo/direção em graus (0-360, 0 = Norte)

    public VetorDeslocamento() {
    }

    public VetorDeslocamento(double deltaLatitude, double deltaLongitude, double deltaAltitude,
                              double magnitude, double orientacao) {
        this.deltaLatitude = deltaLatitude;
        this.deltaLongitude = deltaLongitude;
        this.deltaAltitude = deltaAltitude;
        this.magnitude = magnitude;
        this.orientacao = orientacao;
    }

    // Getters e Setters
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

    public double getOrientacao() {
        return orientacao;
    }

    public void setOrientacao(double orientacao) {
        this.orientacao = orientacao;
    }

    /**
     * Retorna a orientação como texto (Norte, Nordeste, etc)
     */
    public String getOrientacaoTexto() {
        if (orientacao < 22.5) return "N";
        if (orientacao < 67.5) return "NE";
        if (orientacao < 112.5) return "L";
        if (orientacao < 157.5) return "SE";
        if (orientacao < 202.5) return "S";
        if (orientacao < 247.5) return "SO";
        if (orientacao < 292.5) return "O";
        if (orientacao < 337.5) return "NO";
        return "N";
    }

    @Override
    public String toString() {
        return String.format("VetorDeslocamento{Δlat=%.6f°, Δlon=%.6f°, Δalt=%.2fm, magnitude=%.2fm, orientação=%.1f° (%s)}",
                deltaLatitude, deltaLongitude, deltaAltitude, magnitude, orientacao, getOrientacaoTexto());
    }
}