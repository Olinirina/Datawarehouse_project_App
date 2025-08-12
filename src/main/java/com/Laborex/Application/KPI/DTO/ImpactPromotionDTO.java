package com.Laborex.Application.KPI.DTO;

public class ImpactPromotionDTO {
	 private String nomPromotion;
	 private long ugLivre;
	 private double chiffreAffaires;
	 private String interpretation;
	 public ImpactPromotionDTO() {
		super();
		// TODO Auto-generated constructor stub
	 }
	 public ImpactPromotionDTO(String nomPromotion, long ugLivre, double chiffreAffaires, String interpretation) {
		super();
		this.nomPromotion = nomPromotion;
		this.ugLivre = ugLivre;
		this.chiffreAffaires = chiffreAffaires;
		this.interpretation= interpretation;
	 }
	 
	 public String getInterpretation() {
		return interpretation;
	}
	 public void setInterpretation(String interpretation) {
		 this.interpretation = interpretation;
	 }
	 public String getNomPromotion() {
		 return nomPromotion;
	 }
	 public void setNomPromotion(String nomPromotion) {
		 this.nomPromotion = nomPromotion;
	 }
	 public long getUgLivre() {
		 return ugLivre;
	 }
	 public void setUgLivre(long ugLivre) {
		 this.ugLivre = ugLivre;
	 }
	 public double getChiffreAffaires() {
		 return chiffreAffaires;
	 }
	 public void setChiffreAffaires(double chiffreAffaires) {
		 this.chiffreAffaires = chiffreAffaires;
	 }
	 

}
