package com.aeroambiental.sensoriamento.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aeroambiental.sensoriamento.model.RegistroPosicao;

/**
 * Serviço responsável pela persistência e recuperação de posições.
 * Em produção, seria substituído por um repositório com banco de dados.
 */
@Service
public class PersistenciaPosicaoService {

    private static final Logger logger = LoggerFactory.getLogger(PersistenciaPosicaoService.class);

    // Simulação de banco de dados: idVeiculo -> lista de posições ordenada
    private final Map<String, List<RegistroPosicao>> bancoPosicoes = new ConcurrentHashMap<>();

    /**
     * Salva um registro de posição.
     */
    public void salvarPosicao(RegistroPosicao posicao) {
        if (posicao == null || !posicao.isValido()) {
            logger.warn("Tentativa de salvar posição inválida: {}", posicao);
            return;
        }

        bancoPosicoes.computeIfAbsent(posicao.getIdVeiculo(), k -> new ArrayList<>())
                .add(posicao);

        // Ordenar por timestamp (garantia de consistência)
        bancoPosicoes.get(posicao.getIdVeiculo())
                .sort(Comparator.comparing(RegistroPosicao::getTimestamp));

        logger.info("Posição salva para veículo {}: lat={}, lon={}, timestamp={}",
                posicao.getIdVeiculo(), posicao.getLatitude(), posicao.getLongitude(), posicao.getTimestamp());
    }

    /**
     * Recupera todas as posições de um veículo.
     */
    public List<RegistroPosicao> getPosicoesPorVeiculo(String idVeiculo) {
        return bancoPosicoes.getOrDefault(idVeiculo, Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(RegistroPosicao::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * Recupera a última posição de um veículo.
     */
    public Optional<RegistroPosicao> getUltimaPosicao(String idVeiculo) {
        List<RegistroPosicao> posicoes = bancoPosicoes.get(idVeiculo);
        if (posicoes == null || posicoes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(posicoes.get(posicoes.size() - 1));
    }

    /**
     * Recupera posições em um intervalo de tempo.
     */
    public List<RegistroPosicao> getPosicoesPorIntervaloTempo(String idVeiculo, Instant inicio, Instant fim) {
        List<RegistroPosicao> posicoes = bancoPosicoes.getOrDefault(idVeiculo, Collections.emptyList());
        return posicoes.stream()
                .filter(p -> !p.getTimestamp().isBefore(inicio) && !p.getTimestamp().isAfter(fim))
                .sorted(Comparator.comparing(RegistroPosicao::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * Recupera posições consecutivas para cálculo de deslocamento.
     */
    public List<RegistroPosicao> getPosicoesConsecutivas(String idVeiculo, int limite) {
        List<RegistroPosicao> posicoes = bancoPosicoes.getOrDefault(idVeiculo, Collections.emptyList());
        if (posicoes.size() < 2) {
            return Collections.emptyList();
        }

        int fim = posicoes.size();
        int inicio = Math.max(0, fim - limite);
        return posicoes.subList(inicio, fim);
    }

    /**
     * Limpa todas as posições de um veículo.
     */
    public void limparPosicoes(String idVeiculo) {
        bancoPosicoes.remove(idVeiculo);
        logger.info("Posições removidas para veículo: {}", idVeiculo);
    }

    /**
     * Retorna todos os veículos com posições registradas.
     */
    public Set<String> getTodosVeiculos() {
        return new HashSet<>(bancoPosicoes.keySet());
    }
}