package com.rh.models;

public enum Departement {
    CENTRE_APPEL_B2B("Centre d'appel B2B"),
    CENTRE_APPEL_B2C("Centre d'appel B2C"),
    BUREAU_ETUDE("Bureau d'étude");

    private final String label;

    Departement(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}

