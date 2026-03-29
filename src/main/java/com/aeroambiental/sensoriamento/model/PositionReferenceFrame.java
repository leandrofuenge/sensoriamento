package com.aeroambiental.sensoriamento.model;

/**
 * Define o referencial utilizado no sistema.
 * RF06 - Especifica que o referencial é a Terra.
 */
public enum PositionReferenceFrame {
    TERRA("Terra", "Sistema de referência geodésico WGS84"),
    MARS("Marte", "Sistema de referência para exploração futura"),
    LUA("Lua", "Sistema de referência para missões lunares");

    private final String name;
    private final String description;

    PositionReferenceFrame(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}