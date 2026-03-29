package com.aeroambiental.sensoriamento.model;

import java.time.Instant;
import java.util.Objects;

/**
 * RF06 - Monitoramento de Posição
 * Representa um registro de posição do veículo em relação ao referencial Terra.
 */
public class RegistroPosicao {

    private String idVeiculo;

    // Referencial terrestre (geodésico)
    private double latitude;   // graus
    private double longitude;  // graus
    private double altitude;   // metros

    // Tempo da medição
    private Instant timestamp;

    // Grandezas complementares para análise vetorial
    private Double velocidade;      // m/s (opcional)
    private Double direcao;         // graus em relação ao norte geográfico (0-360) (opcional)

    public RegistroPosicao() {
    }

    public RegistroPosicao(String idVeiculo, double latitude, double longitude, double altitude,
                          Instant timestamp, Double velocidade, Double direcao) {
        this.idVeiculo = idVeiculo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.timestamp = timestamp;
        this.velocidade = velocidade;
        this.direcao = direcao;
    }

    // Getters e Setters
    public String getIdVeiculo() {
        return idVeiculo;
    }

    public void setIdVeiculo(String idVeiculo) {
        this.idVeiculo = idVeiculo;
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

    public Double getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(Double velocidade) {
        this.velocidade = velocidade;
    }

    public Double getDirecao() {
        return direcao;
    }

    public void setDirecao(Double direcao) {
        this.direcao = direcao;
    }

    /**
     * Validação básica para coordenadas geográficas.
     */
    public boolean isValido() {
        return latitude >= -90.0 && latitude <= 90.0
            && longitude >= -180.0 && longitude <= 180.0
            && timestamp != null
            && idVeiculo != null
            && !idVeiculo.isBlank();
    }

    @Override
    public String toString() {
        return "RegistroPosicao{" +
                "idVeiculo='" + idVeiculo + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", timestamp=" + timestamp +
                ", velocidade=" + velocidade +
                ", direcao=" + direcao +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistroPosicao)) return false;
        RegistroPosicao that = (RegistroPosicao) o;
        return Double.compare(that.latitude, latitude) == 0 &&
               Double.compare(that.longitude, longitude) == 0 &&
               Double.compare(that.altitude, altitude) == 0 &&
               Objects.equals(idVeiculo, that.idVeiculo) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(velocidade, that.velocidade) &&
               Objects.equals(direcao, that.direcao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idVeiculo, latitude, longitude, altitude, timestamp, velocidade, direcao);
    }
}