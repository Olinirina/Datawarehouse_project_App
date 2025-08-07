package com.Laborex.Application.KPI.DTO;

public class FrequenceVenteDTO {
	private String libelleArticle;
    private long nombreDeVentes;
	public FrequenceVenteDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public FrequenceVenteDTO(String libelleArticle, long nombreDeVentes) {
		super();
		this.libelleArticle = libelleArticle;
		this.nombreDeVentes = nombreDeVentes;
	}
	public String getLibelleArticle() {
		return libelleArticle;
	}
	public void setLibelleArticle(String libelleArticle) {
		this.libelleArticle = libelleArticle;
	}
	public long getNombreDeVentes() {
		return nombreDeVentes;
	}
	public void setNombreDeVentes(long nombreDeVentes) {
		this.nombreDeVentes = nombreDeVentes;
	}
    

}
