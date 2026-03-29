package com.aeroambiental.sensoriamento.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aeroambiental.sensoriamento.model.DisplacementVector;
import com.aeroambiental.sensoriamento.model.PositionRecord;

/**
 * Serviço responsável por calcular deslocamento vetorial entre posições.
 * Inclui versões simplificada e geodésica (Haversine).
 */
public class PositionAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(PositionAnalysisService.class);
    private static final double EARTH_RADIUS_METERS = 6371000.0; // Raio médio da Terra em metros

    /**
     * Calcula deslocamento simplificado usando latitude/longitude como plano local.
     * OBS: Para produção, o ideal é usar Haversine ou conversão geodésica.
     */
    public DisplacementVector calculateDisplacement(PositionRecord previous, PositionRecord current) {
        if (previous == null || current == null) {
            throw new IllegalArgumentException("As posições anterior e atual são obrigatórias.");
        }

        // Usando Haversine para maior precisão
        double distance = calculateHaversineDistance(previous, current);
        double bearing = calculateBearing(previous, current);
        double deltaAlt = current.getAltitude() - previous.getAltitude();

        // Módulo 3D combinando distância horizontal com altitude
        double magnitude3D = Math.sqrt(Math.pow(distance, 2) + Math.pow(deltaAlt, 2));

        // Diferenças em graus (para referência)
        double deltaLat = current.getLatitude() - previous.getLatitude();
        double deltaLon = current.getLongitude() - previous.getLongitude();

        logger.debug("Deslocamento calculado: magnitude={}m, bearing={}°, deltaAlt={}m",
                magnitude3D, bearing, deltaAlt);

        return new DisplacementVector(deltaLat, deltaLon, deltaAlt, magnitude3D, bearing);
    }

    /**
     * Calcula a distância entre dois pontos usando a fórmula de Haversine.
     * Retorna a distância em metros.
     */
    public double calculateHaversineDistance(PositionRecord p1, PositionRecord p2) {
        double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dlon / 2) * Math.sin(dlon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Calcula o rumo (bearing) entre dois pontos.
     * Retorna ângulo em graus (0-360), onde 0 = Norte, 90 = Leste.
     */
    public double calculateBearing(PositionRecord p1, PositionRecord p2) {
        double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());

        double dlon = lon2 - lon1;
        double x = Math.sin(dlon) * Math.cos(lat2);
        double y = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dlon);

        double bearing = Math.toDegrees(Math.atan2(x, y));
        return (bearing + 360) % 360;
    }

    /**
     * Calcula deslocamentos consecutivos para uma lista de posições.
     */
    public List<DisplacementVector> calculateConsecutiveDisplacements(List<PositionRecord> positions) {
        List<DisplacementVector> displacements = new ArrayList<>();

        if (positions == null || positions.size() < 2) {
            return displacements;
        }

        for (int i = 0; i < positions.size() - 1; i++) {
            try {
                DisplacementVector displacement = calculateDisplacement(positions.get(i), positions.get(i + 1));
                displacements.add(displacement);
            } catch (IllegalArgumentException e) {
                logger.warn("Erro ao calcular deslocamento entre índices {} e {}", i, i + 1, e);
            }
        }

        return displacements;
    }

    /**
     * Calcula o deslocamento total acumulado.
     */
    public double calculateTotalDistance(List<PositionRecord> positions) {
        double totalDistance = 0.0;

        if (positions == null || positions.size() < 2) {
            return totalDistance;
        }

        for (int i = 0; i < positions.size() - 1; i++) {
            totalDistance += calculateHaversineDistance(positions.get(i), positions.get(i + 1));
        }

        return totalDistance;
    }

    /**
     * Calcula a velocidade média entre duas posições.
     */
    public double calculateAverageSpeed(PositionRecord p1, PositionRecord p2) {
        double distance = calculateHaversineDistance(p1, p2);
        double timeDiffSeconds = p2.getTimestamp().getEpochSecond() - p1.getTimestamp().getEpochSecond();

        if (timeDiffSeconds <= 0) {
            return 0.0;
        }

        return distance / timeDiffSeconds; // m/s
    }

    /**
     * Verifica se houve movimento significativo (acima do threshold).
     */
    public boolean hasSignificantMovement(PositionRecord previous, PositionRecord current, double thresholdMeters) {
        double distance = calculateHaversineDistance(previous, current);
        return distance > thresholdMeters;
    }
}