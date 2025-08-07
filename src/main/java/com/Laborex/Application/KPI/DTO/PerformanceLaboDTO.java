package com.Laborex.Application.KPI.DTO;

public class PerformanceLaboDTO {
	private String labo;
	private double chiffreAffaire;
	private Long nombreArticle;
	public PerformanceLaboDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PerformanceLaboDTO(String labo, double chiffreAffaire, Long nombreArticle) {
		super();
		this.labo = labo;
		this.chiffreAffaire = chiffreAffaire;
		this.nombreArticle = nombreArticle;
	}
	public String getLabo() {
		return labo;
	}
	public void setLabo(String labo) {
		this.labo = labo;
	}
	public double getChiffreAffaire() {
		return chiffreAffaire;
	}
	public void setChiffreAffaire(double chiffreAffaire) {
		this.chiffreAffaire = chiffreAffaire;
	}
	public Long getNombreArticle() {
		return nombreArticle;
	}
	public void setNombreArticle(Long nombreArticle) {
		this.nombreArticle = nombreArticle;
	}
	
	

}
