package com.Laborex.Application.KPI.DTO;

public class CaClientDTO {
	private String nomClient;
    private Double ca;
	public CaClientDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CaClientDTO(String nomClient, Double ca) {
		super();
		this.nomClient = nomClient;
		this.ca = ca;
	}
	public String getNomClient() {
		return nomClient;
	}
	public void setNomClient(String nomClient) {
		this.nomClient = nomClient;
	}
	public Double getCa() {
		return ca;
	}
	public void setCa(Double ca) {
		this.ca = ca;
	}
    

}
