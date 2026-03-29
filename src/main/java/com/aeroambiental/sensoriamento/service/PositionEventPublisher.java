package com.aeroambiental.sensoriamento.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aeroambiental.sensoriamento.model.DisplacementVector;
import com.aeroambiental.sensoriamento.model.PositionRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Serviço responsável por publicar eventos no RabbitMQ.
 * Em produção, seria integrado com um cliente RabbitMQ real.
 */
public class PositionEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PositionEventPublisher.class);
    private final ObjectMapper objectMapper;

    // Simulação de publicador RabbitMQ
    private final Map<String, EventListener> listeners = new HashMap<>();

    public PositionEventPublisher() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Interface para listeners de eventos.
     */
    public interface EventListener {
        void onEvent(String exchange, String routingKey, String message);
    }

    /**
     * Registra um listener para eventos.
     */
    public void registerListener(String eventType, EventListener listener) {
        listeners.put(eventType, listener);
        logger.info("Listener registrado para evento: {}", eventType);
    }

    /**
     * Publica evento de nova posição recebida.
     */
    public void publishPositionReceived(PositionRecord position) {
        try {
            String message = objectMapper.writeValueAsString(position);
            String routingKey = "telemetry.position.received";
            String exchange = "exchange.telemetry";

            logger.info("Publicando evento de posição recebida: {}", routingKey);

            // Em produção, enviaria para RabbitMQ
            simulatePublish(exchange, routingKey, message);

        } catch (Exception e) {
            logger.error("Erro ao publicar evento de posição", e);
        }
    }

    /**
     * Publica evento de deslocamento calculado.
     */
    public void publishDisplacementCalculated(String vehicleId, PositionRecord previous,
                                              PositionRecord current, DisplacementVector displacement) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("vehicleId", vehicleId);
            event.put("previousPosition", previous);
            event.put("currentPosition", current);
            event.put("displacement", displacement);
            event.put("timestamp", java.time.Instant.now());

            String message = objectMapper.writeValueAsString(event);
            String routingKey = "telemetry.displacement.calculated";
            String exchange = "exchange.telemetry";

            logger.info("Publicando evento de deslocamento calculado: {} - magnitude={}",
                    vehicleId, displacement.getMagnitude());

            simulatePublish(exchange, routingKey, message);

        } catch (Exception e) {
            logger.error("Erro ao publicar evento de deslocamento", e);
        }
    }

    /**
     * Publica evento de alerta de movimento.
     */
    public void publishMovementAlert(String vehicleId, double magnitude, double threshold) {
        try {
            Map<String, Object> alert = new HashMap<>();
            alert.put("vehicleId", vehicleId);
            alert.put("magnitude", magnitude);
            alert.put("threshold", threshold);
            alert.put("timestamp", java.time.Instant.now());
            alert.put("severity", magnitude > threshold * 1.5 ? "HIGH" : "MEDIUM");

            String message = objectMapper.writeValueAsString(alert);
            String routingKey = "telemetry.alert.movement";
            String exchange = "exchange.alerts";

            logger.warn("Alerta de movimento! Veículo: {}, Magnitude: {} > {}",
                    vehicleId, magnitude, threshold);

            simulatePublish(exchange, routingKey, message);

        } catch (Exception e) {
            logger.error("Erro ao publicar alerta de movimento", e);
        }
    }

    /**
     * Simula publicação no RabbitMQ.
     */
    private void simulatePublish(String exchange, String routingKey, String message) {
        logger.debug("Simulando publicação - Exchange: {}, RoutingKey: {}, Message: {}",
                exchange, routingKey, message);

        // Notifica listeners registrados
        listeners.values().forEach(listener ->
                listener.onEvent(exchange, routingKey, message)
        );
    }
}