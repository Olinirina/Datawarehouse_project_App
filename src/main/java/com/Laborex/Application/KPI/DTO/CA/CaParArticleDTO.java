package com.Laborex.Application.KPI.DTO.CA;

public class CaParArticleDTO {
	private String libelleArticle;
    private Double chiffreAffaires;
	public CaParArticleDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CaParArticleDTO(String libelleArticle, Double chiffreAffaires) {
		super();
		this.libelleArticle = libelleArticle;
		this.chiffreAffaires = chiffreAffaires;
	}
	public String getLibelleArticle() {
		return libelleArticle;
	}
	public void setLibelleArticle(String libelleArticle) {
		this.libelleArticle = libelleArticle;
	}
	public Double getChiffreAffaires() {
		return chiffreAffaires;
	}
	public void setChiffreAffaires(Double chiffreAffaires) {
		this.chiffreAffaires = chiffreAffaires;
	}

}
