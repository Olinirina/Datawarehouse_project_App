package com.Laborex.Application.KPI.DTO;

public class FrequenceVenteDTO {
	private String libelleArticle;
    private long nombreDeVentes;
    private String interpretation;
	public FrequenceVenteDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public FrequenceVenteDTO(String libelleArticle, long nombreDeVentes, String interpretation) {
		super();
		this.libelleArticle = libelleArticle;
		this.nombreDeVentes = nombreDeVentes;
		this.interpretation= interpretation;
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
	public String getInterpretation() {
		return interpretation;
	}
	public void setInterpretation(String interpretation) {
		this.interpretation = interpretation;
	}
	
    

}
