package com.Laborex.Application.Model.Alerte;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "alertes")
public class Alerte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAlerte type;
    
    @Column(nullable = false)
    private String titre;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "code_reference")
    private String codeReference; // Code article, client, etc.
    
    @Column(nullable = false)
    private Double valeur; // Quantité, nombre de jours, etc.
    
    @Enumerated(EnumType.STRING)
    private NiveauSeverite severite;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;
    
    private Boolean active = true;

	public Alerte() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Alerte(TypeAlerte type, String titre, String description, String codeReference, Double valeur,
			NiveauSeverite severite, LocalDateTime dateCreation, LocalDateTime dateResolution, Boolean active) {
		super();
		this.type = type;
		this.titre = titre;
		this.description = description;
		this.codeReference = codeReference;
		this.valeur = valeur;
		this.severite = severite;
		this.dateCreation = dateCreation;
		this.dateResolution = dateResolution;
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TypeAlerte getType() {
		return type;
	}

	public void setType(TypeAlerte type) {
		this.type = type;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCodeReference() {
		return codeReference;
	}

	public void setCodeReference(String codeReference) {
		this.codeReference = codeReference;
	}

	public Double getValeur() {
		return valeur;
	}

	public void setValeur(Double valeur) {
		this.valeur = valeur;
	}

	public NiveauSeverite getSeverite() {
		return severite;
	}

	public void setSeverite(NiveauSeverite severite) {
		this.severite = severite;
	}

	public LocalDateTime getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(LocalDateTime dateCreation) {
		this.dateCreation = dateCreation;
	}

	public LocalDateTime getDateResolution() {
		return dateResolution;
	}

	public void setDateResolution(LocalDateTime dateResolution) {
		this.dateResolution = dateResolution;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
    
}