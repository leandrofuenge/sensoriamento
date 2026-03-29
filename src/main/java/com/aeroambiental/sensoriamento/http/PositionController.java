package com.aeroambiental.sensoriamento.http;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aeroambiental.sensoriamento.model.RegistroPosicao;
import com.aeroambiental.sensoriamento.model.VetorDeslocamento;
import com.aeroambiental.sensoriamento.service.AnalisePosicaoService;
import com.aeroambiental.sensoriamento.service.PersistenciaPosicaoService;

/**
 * Controller REST para expor endpoints HTTP de consulta de posições.
 * RF06 e RF07 - Endpoints para posição e deslocamento.
 */
@RestController
@RequestMapping("/api/veiculos")
public class PositionController {

    private static final Logger logger = LoggerFactory.getLogger(PositionController.class);

    private final PersistenciaPosicaoService persistenciaService;
    private final AnalisePosicaoService analisePosicaoService;

    public PositionController(PersistenciaPosicaoService persistenciaService,
                              AnalisePosicaoService analisePosicaoService) {
        this.persistenciaService = persistenciaService;
        this.analisePosicaoService = analisePosicaoService;
    }

    /**
     * GET /api/veiculos/{id}/posicoes
     * Recupera todo o histórico de posições de um veículo.
     */
    @GetMapping("/{id}/posicoes")
    public Map<String, Object> getPosicoes(@PathVariable String id) {
        logger.info("GET /api/veiculos/{}/posicoes", id);

        Map<String, Object> response = new HashMap<>();
        List<RegistroPosicao> posicoes = persistenciaService.getPosicoesPorVeiculo(id);

        response.put("idVeiculo", id);
        response.put("quantidade", posicoes.size());
        response.put("posicoes", posicoes);

        return response;
    }

    /**
     * GET /api/veiculos/{id}/ultima-posicao
     * Recupera a última posição conhecida do veículo.
     */
    @GetMapping("/{id}/ultima-posicao")
    public Map<String, Object> getUltimaPosicao(@PathVariable String id) {
        logger.info("GET /api/veiculos/{}/ultima-posicao", id);

        Map<String, Object> response = new HashMap<>();
        Optional<RegistroPosicao> ultimaPosicao = persistenciaService.getUltimaPosicao(id);

        response.put("idVeiculo", id);
        response.put("temPosicao", ultimaPosicao.isPresent());
        ultimaPosicao.ifPresent(posicao -> response.put("posicao", posicao));

        return response;
    }

    /**
     * GET /api/veiculos/{id}/deslocamento-total
     * Calcula o deslocamento total entre a primeira e última posição.
     */
    @GetMapping("/{id}/deslocamento-total")
    public Map<String, Object> getDeslocamentoTotal(@PathVariable String id) {
        logger.info("GET /api/veiculos/{}/deslocamento-total", id);

        Map<String, Object> response = new HashMap<>();
        List<RegistroPosicao> posicoes = persistenciaService.getPosicoesPorVeiculo(id);

        if (posicoes.size() < 2) {
            response.put("erro", "Dados insuficientes para calcular deslocamento");
            response.put("posicoesNecessarias", 2);
            response.put("posicoesAtuais", posicoes.size());
            return response;
        }

        RegistroPosicao primeira = posicoes.get(0);
        RegistroPosicao ultima = posicoes.get(posicoes.size() - 1);

        VetorDeslocamento deslocamento = analisePosicaoService.calcularDeslocamento(primeira, ultima);

        response.put("idVeiculo", id);
        response.put("inicioPeriodo", primeira.getTimestamp());
        response.put("fimPeriodo", ultima.getTimestamp());
        response.put("deslocamento", deslocamento);
        response.put("magnitudeDeslocamentoMetros", deslocamento.getMagnitude());
        response.put("orientacao", deslocamento.getOrientacao());

        return response;
    }

    /**
     * GET /api/veiculos/{id}/trajetoria
     * Recupera trajetória em um período específico.
     */
    @GetMapping("/{id}/trajetoria")
    public Map<String, Object> getTrajetoria(@PathVariable String id,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim) {
        logger.info("GET /api/veiculos/{}/trajetoria?inicio={}&fim={}", id, inicio, fim);

        Map<String, Object> response = new HashMap<>();
        List<RegistroPosicao> posicoes = persistenciaService.getPosicoesPorIntervaloTempo(id, inicio, fim);

        // Calcula deslocamentos consecutivos
        List<VetorDeslocamento> deslocamentos = analisePosicaoService.calcularDeslocamentosConsecutivos(posicoes);

        response.put("idVeiculo", id);
        response.put("inicioPeriodo", inicio);
        response.put("fimPeriodo", fim);
        response.put("quantidadePosicoes", posicoes.size());
        response.put("posicoes", posicoes);
        response.put("deslocamentos", deslocamentos);

        return response;
    }

    /**
     * POST /api/veiculos/{id}/posicoes/simular
     * Endpoint para simular envio de posição via HTTP (alternativa ao MQTT).
     */
    @PostMapping("/{id}/posicoes/simular")
    public Map<String, Object> simularPosicao(@PathVariable String id, @RequestBody RegistroPosicao posicao) {
        logger.info("POST /api/veiculos/{}/posicoes/simular", id);

        Map<String, Object> response = new HashMap<>();

        if (posicao == null || !posicao.isValido()) {
            response.put("erro", "Posição inválida");
            return response;
        }

        // Garante que o ID do veículo está consistente
        posicao.setIdVeiculo(id);
        persistenciaService.salvarPosicao(posicao);

        response.put("status", "sucesso");
        response.put("mensagem", "Posição simulada e persistida com sucesso");
        response.put("posicao", posicao);

        return response;
    }

    /**
     * GET /api/veiculos/{id}/status
     * Retorna um resumo do status do veículo.
     */
    @GetMapping("/{id}/status")
    public Map<String, Object> getStatusVeiculo(@PathVariable String id) {
        logger.info("GET /api/veiculos/{}/status", id);

        Map<String, Object> response = new HashMap<>();
        Optional<RegistroPosicao> ultimaPosicao = persistenciaService.getUltimaPosicao(id);
        List<RegistroPosicao> posicoes = persistenciaService.getPosicoesPorVeiculo(id);

        response.put("idVeiculo", id);
        response.put("totalPosicoes", posicoes.size());
        response.put("temUltimaPosicao", ultimaPosicao.isPresent());

        if (ultimaPosicao.isPresent()) {
            RegistroPosicao pos = ultimaPosicao.get();
            Map<String, Object> ultima = new HashMap<>();
            ultima.put("timestamp", pos.getTimestamp());
            ultima.put("latitude", pos.getLatitude());
            ultima.put("longitude", pos.getLongitude());
            ultima.put("altitude", pos.getAltitude());
            ultima.put("velocidade", pos.getVelocidade());
            ultima.put("direcao", pos.getDirecao());
            response.put("ultimaPosicao", ultima);
        }

        return response;
    }

    /**
     * GET /api/veiculos/{id}/distancia-total
     * Calcula a distância total percorrida pelo veículo.
     */
    @GetMapping("/{id}/distancia-total")
    public Map<String, Object> getDistanciaTotal(@PathVariable String id) {
        logger.info("GET /api/veiculos/{}/distancia-total", id);

        Map<String, Object> response = new HashMap<>();
        List<RegistroPosicao> posicoes = persistenciaService.getPosicoesPorVeiculo(id);

        if (posicoes.size() < 2) {
            response.put("erro", "Dados insuficientes para calcular distância total");
            response.put("posicoesNecessarias", 2);
            response.put("posicoesAtuais", posicoes.size());
            return response;
        }

        double distanciaTotal = analisePosicaoService.calcularDistanciaTotal(posicoes);

        response.put("idVeiculo", id);
        response.put("distanciaTotalMetros", distanciaTotal);
        response.put("distanciaTotalKm", distanciaTotal / 1000);

        return response;
    }
}