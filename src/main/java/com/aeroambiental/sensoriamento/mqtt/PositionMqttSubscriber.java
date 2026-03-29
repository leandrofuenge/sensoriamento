package com.aeroambiental.sensoriamento.mqtt;


import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aeroambiental.sensoriamento.model.PositionRecord;
import com.aeroambiental.sensoriamento.service.PositionEventPublisher;
import com.aeroambiental.sensoriamento.service.PositionPersistenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Subscritor MQTT para receber telemetria de posição.
 * Responsável por receber dados do veículo e encaminhar para processamento.
 */
public class PositionMqttSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(PositionMqttSubscriber.class);

    private final ObjectMapper objectMapper;
    private final PositionPersistenceService persistenceService;
    private final PositionEventPublisher eventPublisher;

    // Tópico MQTT configurado para receber posições
    private static final String MQTT_TOPIC = "telemetria/veiculo/posicao";

    public PositionMqttSubscriber(PositionPersistenceService persistenceService,
                                  PositionEventPublisher eventPublisher) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.persistenceService = persistenceService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Simula a conexão e recebimento de mensagens MQTT.
     * Em produção, isso seria substituído por um cliente MQTT real (Eclipse Paho).
     */
    public void connect() {
        logger.info("Conectando ao broker MQTT e subscrevendo ao tópico: {}", MQTT_TOPIC);
        logger.info("Aguardando mensagens de telemetria...");

        // Simulação: em produção, aqui teria o callback do MQTT
        // this.mqttClient.subscribe(MQTT_TOPIC, this::onMessageReceived);
    }

    /**
     * Callback executado quando uma mensagem MQTT é recebida.
     */
    public void onMessageReceived(String topic, String payload) {
        logger.info("Mensagem recebida no tópico {}: {}", topic, payload);

        try {
            // Parse do JSON recebido
            PositionRecord position = parsePositionMessage(payload);

            if (position.isValid()) {
                logger.info("Posição válida recebida para veículo: {}", position.getVehicleId());

                // Publica evento de posição recebida (RabbitMQ)
                eventPublisher.publishPositionReceived(position);

                // Persiste a posição
                persistenceService.savePosition(position);

            } else {
                logger.warn("Posição inválida recebida: {}", payload);
            }

        } catch (Exception e) {
            logger.error("Erro ao processar mensagem MQTT: {}", payload, e);
        }
    }

    /**
     * Converte payload JSON para PositionRecord.
     */
    private PositionRecord parsePositionMessage(String payload) throws Exception {
        // Parse manual (simplificado) - em produção usaria ObjectMapper
        // Exemplo de payload esperado:
        // {
        //   "vehicleId": "SAT-01",
        //   "latitude": -15.6014,
        //   "longitude": -56.0979,
        //   "altitude": 350.0,
        //   "timestamp": "2026-03-28T15:10:00Z",
        //   "speed": 12.5,
        //   "heading": 78.0
        // }

        return objectMapper.readValue(payload, PositionRecord.class);
    }

    /**
     * Simula o envio de uma mensagem de teste.
     */
    public void sendTestMessage() {
        String testPayload = "{"
                + "\"vehicleId\":\"SAT-01\","
                + "\"latitude\":-15.6014,"
                + "\"longitude\":-56.0979,"
                + "\"altitude\":350.0,"
                + "\"timestamp\":\"" + Instant.now().toString() + "\","
                + "\"speed\":12.5,"
                + "\"heading\":78.0"
                + "}";

        onMessageReceived(MQTT_TOPIC, testPayload);
    }
}