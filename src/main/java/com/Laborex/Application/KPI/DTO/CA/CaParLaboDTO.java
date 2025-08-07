package com.Laborex.Application.KPI.DTO.CA;

public class CaParLaboDTO {
	private String nomLabo;
    private Double chiffreAffaires;
	public CaParLaboDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CaParLaboDTO(String nomLabo, Double chiffreAffaires) {
		super();
		this.nomLabo = nomLabo;
		this.chiffreAffaires = chiffreAffaires;
	}
	public String getNomLabo() {
		return nomLabo;
	}
	public void setNomLabo(String nomLabo) {
		this.nomLabo = nomLabo;
	}
	public Double getChiffreAffaires() {
		return chiffreAffaires;
	}
	public void setChiffreAffaires(Double chiffreAffaires) {
		this.chiffreAffaires = chiffreAffaires;
	}
    

}
