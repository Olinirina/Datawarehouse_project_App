package com.Laborex.Application.Model.PredictionDTO;

public class DemandePredictionDTO {
	private String CodeArticle;
	private String libelle;
	private int mois;
	private Double totalVente;
	public DemandePredictionDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public DemandePredictionDTO(String codeArticle, String libelle, int mois, Double totalVente) {
		super();
		CodeArticle = codeArticle;
		this.libelle = libelle;
		this.mois = mois;
		this.totalVente = totalVente;
	}

	public String getCodeArticle() {
		return CodeArticle;
	}
	public void setCodeArticle(String codeArticle) {
		CodeArticle = codeArticle;
	}
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public int getMois() {
		return mois;
	}
	public void setMois(int mois) {
		this.mois = mois;
	}

	public Double getTotalVente() {
		return totalVente;
	}

	public void setTotalVente(Double totalVente) {
		this.totalVente = totalVente;
	}
	

}
