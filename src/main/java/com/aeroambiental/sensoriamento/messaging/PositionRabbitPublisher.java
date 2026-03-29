package com.aeroambiental.sensoriamento.messaging;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Publicador de mensagens RabbitMQ para processamento assíncrono.
 * Gerencia filas e exchanges para desacoplamento dos módulos.
 */
public class PositionRabbitPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PositionRabbitPublisher.class);

    private final ObjectMapper objectMapper;

    // Configuração das filas e exchanges
    public static final String EXCHANGE_TELEMETRY = "exchange.telemetry";
    public static final String EXCHANGE_ALERTS = "exchange.alerts";
    public static final String QUEUE_POSITION_PROCESSING = "queue.position.processing";
    public static final String QUEUE_DISPLACEMENT = "queue.displacement";
    public static final String QUEUE_ALERTS = "queue.alerts";

    // Routing keys
    public static final String ROUTING_KEY_POSITION_RECEIVED = "telemetry.position.received";
    public static final String ROUTING_KEY_DISPLACEMENT_CALCULATED = "telemetry.displacement.calculated";
    public static final String ROUTING_KEY_MOVEMENT_ALERT = "telemetry.alert.movement";

    // Simulação de filas (em produção seria RabbitMQ real)
    private final Map<String, BlockingQueue<String>> queues;

    public PositionRabbitPublisher() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        this.queues = new HashMap<>();
        queues.put(QUEUE_POSITION_PROCESSING, new LinkedBlockingQueue<>());
        queues.put(QUEUE_DISPLACEMENT, new LinkedBlockingQueue<>());
        queues.put(QUEUE_ALERTS, new LinkedBlockingQueue<>());

        logger.info("RabbitMQ Publisher inicializado");
    }

    /**
     * Publica mensagem em uma exchange com routing key.
     */
    public void publish(String exchange, String routingKey, Object message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);

            // Determina a fila baseada no routing key
            String queue = getQueueForRoutingKey(routingKey);

            if (queue != null && queues.containsKey(queue)) {
                queues.get(queue).offer(messageJson);
                logger.debug("Mensagem publicada - Exchange: {}, RoutingKey: {}, Queue: {}",
                        exchange, routingKey, queue);
            } else {
                logger.warn("Nenhuma fila mapeada para routing key: {}", routingKey);
            }

        } catch (Exception e) {
            logger.error("Erro ao publicar mensagem no RabbitMQ", e);
        }
    }

    /**
     * Consome mensagem de uma fila.
     */
    public String consume(String queueName) {
        BlockingQueue<String> queue = queues.get(queueName);
        if (queue == null) {
            logger.warn("Fila não encontrada: {}", queueName);
            return null;
        }

        String message = queue.poll();
        if (message != null) {
            logger.debug("Mensagem consumida da fila {}: {}", queueName, message);
        }

        return message;
    }

    /**
     * Mapeia routing key para fila.
     */
    private String getQueueForRoutingKey(String routingKey) {
        switch (routingKey) {
            case ROUTING_KEY_POSITION_RECEIVED:
                return QUEUE_POSITION_PROCESSING;
            case ROUTING_KEY_DISPLACEMENT_CALCULATED:
                return QUEUE_DISPLACEMENT;
            case ROUTING_KEY_MOVEMENT_ALERT:
                return QUEUE_ALERTS;
            default:
                return null;
        }
    }

    /**
     * Obtém estatísticas das filas.
     */
    public Map<String, Integer> getQueueStats() {
        Map<String, Integer> stats = new HashMap<>();
        for (Map.Entry<String, BlockingQueue<String>> entry : queues.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().size());
        }
        return stats;
    }
}