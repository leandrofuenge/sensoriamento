package com.aeroambiental.sensoriamento.service;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aeroambiental.sensoriamento.model.EstatisticasMovimento;
import com.aeroambiental.sensoriamento.model.MetricasVelocidade;
import com.aeroambiental.sensoriamento.model.RegistroPosicao;
import com.aeroambiental.sensoriamento.model.VetorDeslocamento;

/**
 * RF07 - Serviço especializado em análise de movimento e cálculo de velocidades.
 */
@Service
public class AnaliseMovimentoService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnaliseMovimentoService.class);
    private final AnalisePosicaoService analisePosicaoService;
    
    public AnaliseMovimentoService(AnalisePosicaoService analisePosicaoService) {
        this.analisePosicaoService = analisePosicaoService;
    }
    
    /**
     * Calcula as métricas de velocidade entre duas posições consecutivas.
     */
    public MetricasVelocidade calcularVelocidades(RegistroPosicao anterior, RegistroPosicao atual) {
        if (anterior == null || atual == null) {
            throw new IllegalArgumentException("Posições anterior e atual são obrigatórias.");
        }
        
        logger.debug("Calculando velocidades entre posições: {} -> {}", 
                     anterior.getTimestamp(), atual.getTimestamp());
        
        // Calcula deslocamento vetorial
        VetorDeslocamento deslocamento = analisePosicaoService.calcularDeslocamento(anterior, atual);
        
        // Calcula distância total percorrida (para este trecho, é a mesma magnitude do deslocamento)
        // Em movimento retilíneo, a distância percorrida é igual à magnitude do deslocamento
        double distanciaPercorrida = deslocamento.getMagnitude();
        
        // Cria métricas de velocidade
        MetricasVelocidade metricas = new MetricasVelocidade(
            anterior.getIdVeiculo(),
            anterior.getTimestamp(),
            atual.getTimestamp(),
            deslocamento,
            distanciaPercorrida
        );
        
        logger.info("Velocidades calculadas: Vmédia={:.2f} m/s, Vmédia escalar={:.2f} m/s",
                    metricas.getVelocidadeMedia(), metricas.getVelocidadeEscalarMedia());
        
        return metricas;
    }
    
    /**
     * Calcula métricas de velocidade para toda a trajetória.
     */
    public List<MetricasVelocidade> calcularVelocidadesTrajetoria(List<RegistroPosicao> posicoes) {
        List<MetricasVelocidade> metricasList = new ArrayList<>();
        
        if (posicoes == null || posicoes.size() < 2) {
            logger.warn("Número insuficiente de posições para calcular velocidades: {}", 
                        posicoes != null ? posicoes.size() : 0);
            return metricasList;
        }
        
        for (int i = 0; i < posicoes.size() - 1; i++) {
            try {
                MetricasVelocidade metricas = calcularVelocidades(posicoes.get(i), posicoes.get(i + 1));
                metricasList.add(metricas);
            } catch (IllegalArgumentException e) {
                logger.error("Erro ao calcular velocidades entre índices {} e {}", i, i + 1, e);
            }
        }
        
        return metricasList;
    }
    
    /**
     * Calcula a velocidade média geral entre o início e fim da trajetória.
     */
    public MetricasVelocidade calcularVelocidadeMediaGeral(List<RegistroPosicao> posicoes) {
        if (posicoes == null || posicoes.size() < 2) {
            throw new IllegalArgumentException("Pelo menos 2 posições são necessárias.");
        }
        
        RegistroPosicao primeiraPosicao = posicoes.get(0);
        RegistroPosicao ultimaPosicao = posicoes.get(posicoes.size() - 1);
        
        // Calcula deslocamento total (vetorial)
        VetorDeslocamento deslocamentoTotal = analisePosicaoService.calcularDeslocamento(
            primeiraPosicao, ultimaPosicao
        );
        
        // Calcula distância total percorrida (escalar) somando todos os trechos
        double distanciaTotalPercorrida = 0;
        for (int i = 0; i < posicoes.size() - 1; i++) {
            distanciaTotalPercorrida += analisePosicaoService.calcularDistanciaHaversine(
                posicoes.get(i), posicoes.get(i + 1)
            );
        }
        
        MetricasVelocidade metricasGerais = new MetricasVelocidade(
            primeiraPosicao.getIdVeiculo(),
            primeiraPosicao.getTimestamp(),
            ultimaPosicao.getTimestamp(),
            deslocamentoTotal,
            distanciaTotalPercorrida
        );
        
        logger.info("Métricas gerais calculadas: Vmédia={:.2f} m/s, Vmédia escalar={:.2f} m/s",
                    metricasGerais.getVelocidadeMedia(), metricasGerais.getVelocidadeEscalarMedia());
        
        return metricasGerais;
    }
    
    /**
     * Calcula a aceleração média entre dois trechos consecutivos.
     */
    public double calcularAceleracaoMedia(MetricasVelocidade anterior, MetricasVelocidade atual) {
        if (anterior == null || atual == null) {
            throw new IllegalArgumentException("Métricas anterior e atual são obrigatórias.");
        }
        
        double variacaoVelocidade = atual.getVelocidadeMedia() - anterior.getVelocidadeMedia();
        double intervaloMedio = (anterior.getIntervaloEmSegundos() + atual.getIntervaloEmSegundos()) / 2;
        
        if (intervaloMedio <= 0) return 0;
        
        return variacaoVelocidade / intervaloMedio;
    }
    
    /**
     * Classifica o tipo de movimento baseado nas velocidades.
     */
    public String classificarMovimento(MetricasVelocidade metricas, double tolerancia) {
        if (metricas.getVelocidadeMedia() < 0.1) {
            return "ESTACIONÁRIO";
        }
        
        if (metricas.isMovimentoUniforme(tolerancia)) {
            return "MOVIMENTO UNIFORME (retilíneo)";
        } else if (metricas.getVelocidadeEscalarMedia() > metricas.getVelocidadeMedia()) {
            return "MOVIMENTO CURVILÍNEO (trajetória não retilínea)";
        } else {
            return "MOVIMENTO VARIADO";
        }
    }
    
    /**
     * Obtém estatísticas completas do movimento.
     */
    public EstatisticasMovimento obterEstatisticasMovimento(List<RegistroPosicao> posicoes) {
        EstatisticasMovimento estatisticas = new EstatisticasMovimento();
        
        if (posicoes == null || posicoes.size() < 2) {
            return estatisticas;
        }
        
        List<MetricasVelocidade> metricasList = calcularVelocidadesTrajetoria(posicoes);
        MetricasVelocidade metricasGerais = calcularVelocidadeMediaGeral(posicoes);
        
        estatisticas.setNumeroAmostras(posicoes.size());
        estatisticas.setDuracaoTotalSegundos(metricasGerais.getIntervaloEmSegundos());
        estatisticas.setDeslocamentoTotal(metricasGerais.getDeslocamento());
        estatisticas.setDistanciaTotalPercorrida(metricasGerais.getDistanciaTotalEmMetros());
        estatisticas.setVelocidadeMediaGeral(metricasGerais.getVelocidadeMedia());
        estatisticas.setVelocidadeEscalarMediaGeral(metricasGerais.getVelocidadeEscalarMedia());
        
        // Calcula velocidade máxima
        double velocidadeMaxima = metricasList.stream()
            .mapToDouble(MetricasVelocidade::getVelocidadeMedia)
            .max()
            .orElse(0);
        estatisticas.setVelocidadeMaxima(velocidadeMaxima);
        
        // Calcula velocidade mínima
        double velocidadeMinima = metricasList.stream()
            .mapToDouble(MetricasVelocidade::getVelocidadeMedia)
            .min()
            .orElse(0);
        estatisticas.setVelocidadeMinima(velocidadeMinima);
        
        logger.info("Estatísticas geradas: {} amostras, distância total={:.2f}m, tempo={:.1f}s",
                    estatisticas.getNumeroAmostras(), 
                    estatisticas.getDistanciaTotalPercorrida(),
                    estatisticas.getDuracaoTotalSegundos());
        
        return estatisticas;
    }
}