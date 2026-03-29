package com.aeroambiental.sensoriamento.model;


import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * RF07 - Representa as métricas de velocidade entre duas posições.
 * Inclui velocidade média (vetorial) e velocidade escalar média.
 */
public class MetricasVelocidade {
    
    private String idVeiculo;
    private Instant instanteInicial;
    private Instant instanteFinal;
    private double intervaloEmSegundos;
    
    // Dados do deslocamento
    private VetorDeslocamento deslocamento;
    private double distanciaTotalEmMetros;  // Distância total percorrida (escalar)
    
    // Velocidades
    private double velocidadeMedia;      // Velocidade média vetorial (m/s)
    private double velocidadeEscalarMedia; // Velocidade escalar média (m/s)
    
    // Componentes da velocidade vetorial
    private double velocidadeNorte;        // Componente Norte-Sul (m/s)
    private double velocidadeLeste;        // Componente Leste-Oeste (m/s)
    private double velocidadeVertical;     // Componente vertical (m/s)
    private double direcaoVelocidade;      // Direção da velocidade (graus)
    
    public MetricasVelocidade() {
    }
    
    public MetricasVelocidade(String idVeiculo, Instant instanteInicial, Instant instanteFinal,
                              VetorDeslocamento deslocamento, double distanciaTotalEmMetros) {
        this.idVeiculo = idVeiculo;
        this.instanteInicial = instanteInicial;
        this.instanteFinal = instanteFinal;
        this.intervaloEmSegundos = ChronoUnit.SECONDS.between(instanteInicial, instanteFinal);
        this.deslocamento = deslocamento;
        this.distanciaTotalEmMetros = distanciaTotalEmMetros;
        
        calcularVelocidades();
    }
    
    private void calcularVelocidades() {
        if (intervaloEmSegundos <= 0) {
            this.velocidadeMedia = 0;
            this.velocidadeEscalarMedia = 0;
            this.velocidadeNorte = 0;
            this.velocidadeLeste = 0;
            this.velocidadeVertical = 0;
            this.direcaoVelocidade = 0;
            return;
        }
        
        // Velocidade média vetorial = deslocamento / tempo
        this.velocidadeMedia = deslocamento.getMagnitude() / intervaloEmSegundos;
        
        // Velocidade escalar média = distância total / tempo
        this.velocidadeEscalarMedia = distanciaTotalEmMetros / intervaloEmSegundos;
        
        // Componentes da velocidade (considerando latitude/longitude como plano local)
        double deltaLatEmMetros = deslocamento.getDeltaLatitude() * 111319.9; // Aprox: 1° lat ≈ 111.32 km
        double deltaLonEmMetros = deslocamento.getDeltaLongitude() * 111319.9 * 
                                  Math.cos(Math.toRadians(instanteInicial != null ? 0 : 0)); // Simplificado
        
        this.velocidadeNorte = deltaLatEmMetros / intervaloEmSegundos;
        this.velocidadeLeste = deltaLonEmMetros / intervaloEmSegundos;
        this.velocidadeVertical = deslocamento.getDeltaAltitude() / intervaloEmSegundos;
        
        // Direção da velocidade (rumo)
        this.direcaoVelocidade = deslocamento.getOrientacao();
    }
    
    // Getters e Setters
    public String getIdVeiculo() {
        return idVeiculo;
    }
    
    public void setIdVeiculo(String idVeiculo) {
        this.idVeiculo = idVeiculo;
    }
    
    public Instant getInstanteInicial() {
        return instanteInicial;
    }
    
    public void setInstanteInicial(Instant instanteInicial) {
        this.instanteInicial = instanteInicial;
        recalcular();
    }
    
    public Instant getInstanteFinal() {
        return instanteFinal;
    }
    
    public void setInstanteFinal(Instant instanteFinal) {
        this.instanteFinal = instanteFinal;
        recalcular();
    }
    
    public double getIntervaloEmSegundos() {
        return intervaloEmSegundos;
    }
    
    public VetorDeslocamento getDeslocamento() {
        return deslocamento;
    }
    
    public void setDeslocamento(VetorDeslocamento deslocamento) {
        this.deslocamento = deslocamento;
        recalcular();
    }
    
    public double getDistanciaTotalEmMetros() {
        return distanciaTotalEmMetros;
    }
    
    public void setDistanciaTotalEmMetros(double distanciaTotalEmMetros) {
        this.distanciaTotalEmMetros = distanciaTotalEmMetros;
        recalcular();
    }
    
    public double getVelocidadeMedia() {
        return velocidadeMedia;
    }
    
    public double getVelocidadeEscalarMedia() {
        return velocidadeEscalarMedia;
    }
    
    public double getVelocidadeNorte() {
        return velocidadeNorte;
    }
    
    public double getVelocidadeLeste() {
        return velocidadeLeste;
    }
    
    public double getVelocidadeVertical() {
        return velocidadeVertical;
    }
    
    public double getDirecaoVelocidade() {
        return direcaoVelocidade;
    }
    
    /**
     * Obtém a velocidade média em km/h (para exibição amigável)
     */
    public double getVelocidadeMediaKmh() {
        return velocidadeMedia * 3.6;
    }
    
    /**
     * Obtém a velocidade escalar média em km/h
     */
    public double getVelocidadeEscalarMediaKmh() {
        return velocidadeEscalarMedia * 3.6;
    }
    
    /**
     * Obtém a magnitude da velocidade horizontal (Norte + Leste)
     */
    public double getVelocidadeHorizontal() {
        return Math.sqrt(Math.pow(velocidadeNorte, 2) + Math.pow(velocidadeLeste, 2));
    }
    
    private void recalcular() {
        if (instanteInicial != null && instanteFinal != null && deslocamento != null) {
            this.intervaloEmSegundos = ChronoUnit.SECONDS.between(instanteInicial, instanteFinal);
            calcularVelocidades();
        }
    }
    
    /**
     * Verifica se o movimento foi uniforme (velocidade vetorial ≈ velocidade escalar)
     */
    public boolean isMovimentoUniforme(double tolerancia) {
        return Math.abs(velocidadeMedia - velocidadeEscalarMedia) <= tolerancia;
    }
    
    /**
     * Verifica se houve aceleração significativa entre dois trechos
     */
    public boolean houveAceleracao(MetricasVelocidade anterior, double toleranciaMs2) {
        if (anterior == null) return false;
        double variacaoVelocidade = Math.abs(this.velocidadeMedia - anterior.velocidadeMedia);
        double variacaoTempo = this.intervaloEmSegundos;
        
        if (variacaoTempo <= 0) return false;
        
        double aceleracaoMedia = variacaoVelocidade / variacaoTempo;
        return aceleracaoMedia > toleranciaMs2;
    }
    
    @Override
    public String toString() {
        return String.format(
            "MetricasVelocidade{veiculo='%s', intervalo=%.1fs, v_media=%.2f m/s (%.2f km/h), " +
            "v_escalar=%.2f m/s (%.2f km/h), direcao=%.1f°}",
            idVeiculo, intervaloEmSegundos, velocidadeMedia, getVelocidadeMediaKmh(),
            velocidadeEscalarMedia, getVelocidadeEscalarMediaKmh(), direcaoVelocidade
        );
    }
}