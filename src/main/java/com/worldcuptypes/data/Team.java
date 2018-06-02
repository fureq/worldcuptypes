package com.worldcuptypes.data;

public enum Team {
    RUS("Rosja"),
    EGY("Egipt"),
    URU("Urugwaj"),
    SAU("Arabia Saudyjska"),
    MAR("Maroko"),
    IRA("Iran"),
    POR("Portugalia"),
    ESP("Hiszpania"),
    FRA("Francja"),
    AUS("Australia"),
    PER("Peru"),
    DEN("Dania"),
    ARG("Argentyna"),
    CRO("Chorwacja"),
    ISL("Islandia"),
    NIG("Nigeria"),
    BRA("Brazylia"),
    CRI("Kostaryka"),
    SRB("Serbia"),
    SUI("Szwajcaria"),
    GER("Niemcy"),
    KOR("Korea Płd."),
    MEX("Meksyk"),
    SWE("Szwecja"),
    BEL("Belgia"),
    ENG("Anglia"),
    PAN("Panama"),
    TUN("Tunezja"),
    POL("Polska"),
    COL("Kolumbia"),
    JAP("Japonia"),
    SEN("Senegal");

    private String name;

    Team(String name) {
        this.name = name;
    }
}
