package com.aeroambiental.sensoriamento.service;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aeroambiental.sensoriamento.model.PositionRecord;

/**
 * Serviço responsável pela persistência e recuperação de posições.
 * Em produção, seria substituído por um repositório com banco de dados.
 */
public class PositionPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(PositionPersistenceService.class);

    // Simulação de banco de dados: vehicleId -> lista de posições ordenada
    private final Map<String, List<PositionRecord>> positionDatabase = new ConcurrentHashMap<>();

    /**
     * Salva um registro de posição.
     */
    public void savePosition(PositionRecord position) {
        if (position == null || !position.isValid()) {
            logger.warn("Tentativa de salvar posição inválida: {}", position);
            return;
        }

        positionDatabase.computeIfAbsent(position.getVehicleId(), k -> new ArrayList<>())
                .add(position);

        // Ordenar por timestamp (garantia de consistência)
        positionDatabase.get(position.getVehicleId())
                .sort(Comparator.comparing(PositionRecord::getTimestamp));

        logger.info("Posição salva para veículo {}: lat={}, lon={}, timestamp={}",
                position.getVehicleId(), position.getLatitude(), position.getLongitude(), position.getTimestamp());
    }

    /**
     * Recupera todas as posições de um veículo.
     */
    public List<PositionRecord> getPositionsByVehicle(String vehicleId) {
        return positionDatabase.getOrDefault(vehicleId, Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(PositionRecord::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * Recupera a última posição de um veículo.
     */
    public Optional<PositionRecord> getLastPosition(String vehicleId) {
        List<PositionRecord> positions = positionDatabase.get(vehicleId);
        if (positions == null || positions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(positions.get(positions.size() - 1));
    }

    /**
     * Recupera posições em um intervalo de tempo.
     */
    public List<PositionRecord> getPositionsByTimeRange(String vehicleId, Instant start, Instant end) {
        List<PositionRecord> positions = positionDatabase.getOrDefault(vehicleId, Collections.emptyList());
        return positions.stream()
                .filter(p -> !p.getTimestamp().isBefore(start) && !p.getTimestamp().isAfter(end))
                .sorted(Comparator.comparing(PositionRecord::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * Recupera posições consecutivas para cálculo de deslocamento.
     */
    public List<PositionRecord> getConsecutivePositions(String vehicleId, int limit) {
        List<PositionRecord> positions = positionDatabase.getOrDefault(vehicleId, Collections.emptyList());
        if (positions.size() < 2) {
            return Collections.emptyList();
        }

        int end = positions.size();
        int start = Math.max(0, end - limit);
        return positions.subList(start, end);
    }

    /**
     * Limpa todas as posições de um veículo.
     */
    public void clearPositions(String vehicleId) {
        positionDatabase.remove(vehicleId);
        logger.info("Posições removidas para veículo: {}", vehicleId);
    }
}