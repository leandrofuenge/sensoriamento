package com.aeroambiental.sensoriamento.service;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aeroambiental.sensoriamento.model.RegistroPosicao;
import com.aeroambiental.sensoriamento.model.VetorDeslocamento;

/**
 * RF06 - Serviço responsável por análise de posição e cálculo de deslocamento.
 */
@Service
public class AnalisePosicaoService {

    private static final Logger logger = LoggerFactory.getLogger(AnalisePosicaoService.class);
    private static final double RAIO_TERRA_METROS = 6371000.0; // Raio médio da Terra em metros

    /**
     * Calcula deslocamento vetorial entre duas posições usando Haversine.
     */
    public VetorDeslocamento calcularDeslocamento(RegistroPosicao anterior, RegistroPosicao atual) {
        if (anterior == null || atual == null) {
            throw new IllegalArgumentException("As posições anterior e atual são obrigatórias.");
        }

        // Calcula distância Haversine (horizontal)
        double distanciaHorizontal = calcularDistanciaHaversine(anterior, atual);
        
        // Calcula rumo (bearing)
        double rumo = calcularRumo(anterior, atual);
        
        // Diferença de altitude
        double deltaAltitude = atual.getAltitude() - anterior.getAltitude();
        
        // Módulo 3D combinando distância horizontal com altitude
        double magnitude3D = Math.sqrt(Math.pow(distanciaHorizontal, 2) + Math.pow(deltaAltitude, 2));
        
        // Diferenças em graus (para referência)
        double deltaLatitude = atual.getLatitude() - anterior.getLatitude();
        double deltaLongitude = atual.getLongitude() - anterior.getLongitude();

        logger.debug("Deslocamento calculado: magnitude={}m, rumo={}°, deltaAlt={}m",
                magnitude3D, rumo, deltaAltitude);

        return new VetorDeslocamento(deltaLatitude, deltaLongitude, deltaAltitude, magnitude3D, rumo);
    }

    /**
     * Calcula a distância entre dois pontos usando a fórmula de Haversine.
     * Retorna a distância em metros.
     */
    public double calcularDistanciaHaversine(RegistroPosicao p1, RegistroPosicao p2) {
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

        return RAIO_TERRA_METROS * c;
    }

    /**
     * Calcula o rumo (bearing) entre dois pontos.
     * Retorna ângulo em graus (0-360), onde 0 = Norte, 90 = Leste.
     */
    public double calcularRumo(RegistroPosicao p1, RegistroPosicao p2) {
        double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());

        double dlon = lon2 - lon1;
        double x = Math.sin(dlon) * Math.cos(lat2);
        double y = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dlon);

        double rumo = Math.toDegrees(Math.atan2(x, y));
        return (rumo + 360) % 360;
    }

    /**
     * Calcula deslocamentos consecutivos para uma lista de posições.
     */
    public List<VetorDeslocamento> calcularDeslocamentosConsecutivos(List<RegistroPosicao> posicoes) {
        List<VetorDeslocamento> deslocamentos = new ArrayList<>();

        if (posicoes == null || posicoes.size() < 2) {
            return deslocamentos;
        }

        for (int i = 0; i < posicoes.size() - 1; i++) {
            try {
                VetorDeslocamento deslocamento = calcularDeslocamento(posicoes.get(i), posicoes.get(i + 1));
                deslocamentos.add(deslocamento);
            } catch (IllegalArgumentException e) {
                logger.warn("Erro ao calcular deslocamento entre índices {} e {}", i, i + 1, e);
            }
        }

        return deslocamentos;
    }

    /**
     * Calcula a distância total percorrida.
     */
    public double calcularDistanciaTotal(List<RegistroPosicao> posicoes) {
        double distanciaTotal = 0.0;

        if (posicoes == null || posicoes.size() < 2) {
            return distanciaTotal;
        }

        for (int i = 0; i < posicoes.size() - 1; i++) {
            distanciaTotal += calcularDistanciaHaversine(posicoes.get(i), posicoes.get(i + 1));
        }

        return distanciaTotal;
    }

    /**
     * Verifica se houve movimento significativo (acima do limiar).
     */
    public boolean houveMovimentoSignificativo(RegistroPosicao anterior, 
                                                RegistroPosicao atual, 
                                                double limiarMetros) {
        double distancia = calcularDistanciaHaversine(anterior, atual);
        return distancia > limiarMetros;
    }
}