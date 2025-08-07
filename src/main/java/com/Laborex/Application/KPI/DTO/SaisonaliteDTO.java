package com.Laborex.Application.KPI.DTO;

public class SaisonaliteDTO {
	private int annee;
	private int mois;
	private double chiffreAffaire;
	public SaisonaliteDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public SaisonaliteDTO(int annee, int mois, double chiffreAffaire) {
		super();
		this.annee = annee;
		this.mois = mois;
		this.chiffreAffaire = chiffreAffaire;
	}
	public int getAnnee() {
		return annee;
	}
	public void setAnnee(int annee) {
		this.annee = annee;
	}
	public int getMois() {
		return mois;
	}
	public void setMois(int mois) {
		this.mois = mois;
	}
	public double getChiffreAffaire() {
		return chiffreAffaire;
	}
	public void setChiffreAffaire(double chiffreAffaire) {
		this.chiffreAffaire = chiffreAffaire;
	}
	

}
