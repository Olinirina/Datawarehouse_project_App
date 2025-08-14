package com.Laborex.Application.Model.Alerte;

public enum NiveauSeverite {
	FAIBLE("Faible", "#28a745"),      // Vert
    MODERE("Modéré", "#ffc107"),      // Jaune
    ELEVE("Élevé", "#fd7e14"),        // Orange
    CRITIQUE("Critique", "#dc3545");   // Rouge
    
    private final String libelle;
    private final String couleur;
    
    NiveauSeverite(String libelle, String couleur) {
        this.libelle = libelle;
        this.couleur = couleur;
    }
    
    public String getLibelle() { return libelle; }
    public String getCouleur() { return couleur; }

}
