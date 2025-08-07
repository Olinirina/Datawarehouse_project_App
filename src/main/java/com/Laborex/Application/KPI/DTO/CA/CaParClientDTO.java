package com.Laborex.Application.KPI.DTO.CA;

public class CaParClientDTO {
	private String nomClient;
    private Double chiffreAffaires;
	public CaParClientDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CaParClientDTO(String nomClient, Double chiffreAffaires) {
		super();
		this.nomClient = nomClient;
		this.chiffreAffaires = chiffreAffaires;
	}
	public String getNomClient() {
		return nomClient;
	}
	public void setNomClient(String nomClient) {
		this.nomClient = nomClient;
	}
	public Double getChiffreAffaires() {
		return chiffreAffaires;
	}
	public void setChiffreAffaires(Double chiffreAffaires) {
		this.chiffreAffaires = chiffreAffaires;
	}
    

}
