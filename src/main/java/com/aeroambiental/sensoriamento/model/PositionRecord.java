package com.aeroambiental.sensoriamento.model;

import java.time.Instant;
import java.util.Objects;

/**
 * RF06 - Monitoramento de Posição
 * Representa um registro de posição do veículo em relação ao referencial Terra.
 */
public class PositionRecord {

    private String vehicleId;

    // Referencial terrestre (geodésico)
    private double latitude;   // graus
    private double longitude;  // graus
    private double altitude;   // metros

    // Tempo da medição
    private Instant timestamp;

    // Grandezas complementares para análise vetorial
    private Double speed;      // m/s (opcional)
    private Double heading;    // graus em relação ao norte geográfico (0-360) (opcional)

    public PositionRecord() {
    }

    public PositionRecord(String vehicleId, double latitude, double longitude, double altitude,
                          Instant timestamp, Double speed, Double heading) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.timestamp = timestamp;
        this.speed = speed;
        this.heading = heading;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    /**
     * Validação básica para coordenadas geográficas.
     */
    public boolean isValid() {
        return latitude >= -90.0 && latitude <= 90.0
            && longitude >= -180.0 && longitude <= 180.0
            && timestamp != null
            && vehicleId != null
            && !vehicleId.isBlank();
    }

    @Override
    public String toString() {
        return "PositionRecord{" +
                "vehicleId='" + vehicleId + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", timestamp=" + timestamp +
                ", speed=" + speed +
                ", heading=" + heading +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositionRecord)) return false;
        PositionRecord that = (PositionRecord) o;
        return Double.compare(that.latitude, latitude) == 0 &&
               Double.compare(that.longitude, longitude) == 0 &&
               Double.compare(that.altitude, altitude) == 0 &&
               Objects.equals(vehicleId, that.vehicleId) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(speed, that.speed) &&
               Objects.equals(heading, that.heading);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, latitude, longitude, altitude, timestamp, speed, heading);
    }
}