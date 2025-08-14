package com.Laborex.Application.ComparaisonDTO;

public class SimulationDTO {
	public SimulationDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	private String codeArticle;
    private String libelleArticle;
    private Double prixVenteActuel;
    private Double nouveauPrixVente;
    private Double margeActuelle;
    private Double nouvelleMarge;
    private Double ecartConcurrentielActuel;
    private Double nouvelEcartConcurrentiel;
    private Integer classementActuel;
    private Integer nouveauClassement;
    private String impactPositionnement;
    private Boolean ameliorationClassement;
	public String getCodeArticle() {
		return codeArticle;
	}
	public void setCodeArticle(String codeArticle) {
		this.codeArticle = codeArticle;
	}
	public String getLibelleArticle() {
		return libelleArticle;
	}
	public void setLibelleArticle(String libelleArticle) {
		this.libelleArticle = libelleArticle;
	}
	public Double getPrixVenteActuel() {
		return prixVenteActuel;
	}
	public void setPrixVenteActuel(Double prixVenteActuel) {
		this.prixVenteActuel = prixVenteActuel;
	}
	public Double getNouveauPrixVente() {
		return nouveauPrixVente;
	}
	public void setNouveauPrixVente(Double nouveauPrixVente) {
		this.nouveauPrixVente = nouveauPrixVente;
	}
	public Double getMargeActuelle() {
		return margeActuelle;
	}
	public void setMargeActuelle(Double margeActuelle) {
		this.margeActuelle = margeActuelle;
	}
	public Double getNouvelleMarge() {
		return nouvelleMarge;
	}
	public void setNouvelleMarge(Double nouvelleMarge) {
		this.nouvelleMarge = nouvelleMarge;
	}
	public Double getEcartConcurrentielActuel() {
		return ecartConcurrentielActuel;
	}
	public void setEcartConcurrentielActuel(Double ecartConcurrentielActuel) {
		this.ecartConcurrentielActuel = ecartConcurrentielActuel;
	}
	public Double getNouvelEcartConcurrentiel() {
		return nouvelEcartConcurrentiel;
	}
	public void setNouvelEcartConcurrentiel(Double nouvelEcartConcurrentiel) {
		this.nouvelEcartConcurrentiel = nouvelEcartConcurrentiel;
	}
	public Integer getClassementActuel() {
		return classementActuel;
	}
	public void setClassementActuel(Integer classementActuel) {
		this.classementActuel = classementActuel;
	}
	public Integer getNouveauClassement() {
		return nouveauClassement;
	}
	public void setNouveauClassement(Integer nouveauClassement) {
		this.nouveauClassement = nouveauClassement;
	}
	public String getImpactPositionnement() {
		return impactPositionnement;
	}
	public void setImpactPositionnement(String impactPositionnement) {
		this.impactPositionnement = impactPositionnement;
	}
	public Boolean getAmeliorationClassement() {
		return ameliorationClassement;
	}
	public void setAmeliorationClassement(Boolean ameliorationClassement) {
		this.ameliorationClassement = ameliorationClassement;
	}
	public SimulationDTO(String codeArticle, String libelleArticle, Double prixVenteActuel, Double nouveauPrixVente,
			Double margeActuelle, Double nouvelleMarge, Double ecartConcurrentielActuel,
			Double nouvelEcartConcurrentiel, Integer classementActuel, Integer nouveauClassement,
			String impactPositionnement, Boolean ameliorationClassement) {
		super();
		this.codeArticle = codeArticle;
		this.libelleArticle = libelleArticle;
		this.prixVenteActuel = prixVenteActuel;
		this.nouveauPrixVente = nouveauPrixVente;
		this.margeActuelle = margeActuelle;
		this.nouvelleMarge = nouvelleMarge;
		this.ecartConcurrentielActuel = ecartConcurrentielActuel;
		this.nouvelEcartConcurrentiel = nouvelEcartConcurrentiel;
		this.classementActuel = classementActuel;
		this.nouveauClassement = nouveauClassement;
		this.impactPositionnement = impactPositionnement;
		this.ameliorationClassement = ameliorationClassement;
	}
    

}
