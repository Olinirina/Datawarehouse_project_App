package com.Laborex.Application.KPI.DTO.CA;

public class CaParPeriodeDTO {
	private int annee;
    private int mois;
    private int jour;
    private double chiffreAffaires;
	public CaParPeriodeDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CaParPeriodeDTO(int annee, int mois,int jour, double chiffreAffaires) {
		super();
		this.annee = annee;
		this.mois = mois;
		this.jour= jour;
		this.chiffreAffaires = chiffreAffaires;
	}
	public int getAnnee() {
		return annee;
	}
	public void setAnnee(int annee) {
		this.annee = annee;
	}
	public int getJour() {
		return jour;
	}
	public void setJour(int jour) {
		this.jour = jour;
	}
	public int getMois() {
		return mois;
	}
	public void setMois(int mois) {
		this.mois = mois;
	}
	public double getChiffreAffaires() {
		return chiffreAffaires;
	}
	public void setChiffreAffaires(double chiffreAffaires) {
		this.chiffreAffaires = chiffreAffaires;
	}
    

}
