package com.aeroambiental.sensoriamento.model;

/**
 * RF07 - Representa estatísticas completas do movimento de um veículo.
 */
public class EstatisticasMovimento {
    
    private int numeroAmostras;
    private double duracaoTotalSegundos;
    private VetorDeslocamento deslocamentoTotal;
    private double distanciaTotalPercorrida;
    private double velocidadeMediaGeral;
    private double velocidadeEscalarMediaGeral;
    private double velocidadeMaxima;
    private double velocidadeMinima;
    private double velocidadeMediaKmh;
    private double velocidadeEscalarMediaKmh;
    
    public EstatisticasMovimento() {
        this.numeroAmostras = 0;
        this.duracaoTotalSegundos = 0;
        this.distanciaTotalPercorrida = 0;
        this.velocidadeMediaGeral = 0;
        this.velocidadeEscalarMediaGeral = 0;
        this.velocidadeMaxima = 0;
        this.velocidadeMinima = 0;
    }
    
    // Getters e Setters
    public int getNumeroAmostras() {
        return numeroAmostras;
    }
    
    public void setNumeroAmostras(int numeroAmostras) {
        this.numeroAmostras = numeroAmostras;
    }
    
    public double getDuracaoTotalSegundos() {
        return duracaoTotalSegundos;
    }
    
    public void setDuracaoTotalSegundos(double duracaoTotalSegundos) {
        this.duracaoTotalSegundos = duracaoTotalSegundos;
    }
    
    public VetorDeslocamento getDeslocamentoTotal() {
        return deslocamentoTotal;
    }
    
    public void setDeslocamentoTotal(VetorDeslocamento deslocamentoTotal) {
        this.deslocamentoTotal = deslocamentoTotal;
    }
    
    public double getDistanciaTotalPercorrida() {
        return distanciaTotalPercorrida;
    }
    
    public void setDistanciaTotalPercorrida(double distanciaTotalPercorrida) {
        this.distanciaTotalPercorrida = distanciaTotalPercorrida;
    }
    
    public double getVelocidadeMediaGeral() {
        return velocidadeMediaGeral;
    }
    
    public void setVelocidadeMediaGeral(double velocidadeMediaGeral) {
        this.velocidadeMediaGeral = velocidadeMediaGeral;
        this.velocidadeMediaKmh = velocidadeMediaGeral * 3.6;
    }
    
    public double getVelocidadeEscalarMediaGeral() {
        return velocidadeEscalarMediaGeral;
    }
    
    public void setVelocidadeEscalarMediaGeral(double velocidadeEscalarMediaGeral) {
        this.velocidadeEscalarMediaGeral = velocidadeEscalarMediaGeral;
        this.velocidadeEscalarMediaKmh = velocidadeEscalarMediaGeral * 3.6;
    }
    
    public double getVelocidadeMaxima() {
        return velocidadeMaxima;
    }
    
    public void setVelocidadeMaxima(double velocidadeMaxima) {
        this.velocidadeMaxima = velocidadeMaxima;
    }
    
    public double getVelocidadeMinima() {
        return velocidadeMinima;
    }
    
    public void setVelocidadeMinima(double velocidadeMinima) {
        this.velocidadeMinima = velocidadeMinima;
    }
    
    public double getVelocidadeMediaKmh() {
        return velocidadeMediaKmh;
    }
    
    public double getVelocidadeEscalarMediaKmh() {
        return velocidadeEscalarMediaKmh;
    }
    
    /**
     * Calcula o rendimento do movimento (razão entre deslocamento e distância percorrida)
     * Quanto mais próximo de 1, mais retilíneo foi o movimento.
     */
    public double getRendimentoMovimento() {
        if (distanciaTotalPercorrida <= 0) return 0;
        return deslocamentoTotal != null ? 
               deslocamentoTotal.getMagnitude() / distanciaTotalPercorrida : 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "EstatisticasMovimento{amostras=%d, duracao=%.1fs, distancia=%.2fm, " +
            "v_media=%.2f m/s (%.2f km/h), v_escalar=%.2f m/s (%.2f km/h), " +
            "v_max=%.2f m/s, rendimento=%.2f}",
            numeroAmostras, duracaoTotalSegundos, distanciaTotalPercorrida,
            velocidadeMediaGeral, velocidadeMediaKmh,
            velocidadeEscalarMediaGeral, velocidadeEscalarMediaKmh,
            velocidadeMaxima, getRendimentoMovimento()
        );
    }
}