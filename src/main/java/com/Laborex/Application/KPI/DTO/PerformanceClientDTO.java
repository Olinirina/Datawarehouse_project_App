package com.Laborex.Application.KPI.DTO;

import java.time.LocalDate;

public class PerformanceClientDTO {
	private String nomClient;
    private double chiffreAffaires;
    private long frequenceAchat;
    private LocalDate derniereCommande;
    private String interpretation;
	public PerformanceClientDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PerformanceClientDTO(String nomClient, double chiffreAffaires, long frequenceAchat,
			LocalDate derniereCommande,String interpretation) {
		super();
		this.nomClient = nomClient;
		this.chiffreAffaires = chiffreAffaires;
		this.frequenceAchat = frequenceAchat;
		this.derniereCommande = derniereCommande;
		this.interpretation= interpretation;
	}
	public String getNomClient() {
		return nomClient;
	}
	public void setNomClient(String nomClient) {
		this.nomClient = nomClient;
	}
	public double getChiffreAffaires() {
		return chiffreAffaires;
	}
	public void setChiffreAffaires(double chiffreAffaires) {
		this.chiffreAffaires = chiffreAffaires;
	}
	public long getFrequenceAchat() {
		return frequenceAchat;
	}
	public void setFrequenceAchat(long frequenceAchat) {
		this.frequenceAchat = frequenceAchat;
	}
	public LocalDate getDerniereCommande() {
		return derniereCommande;
	}
	public void setDerniereCommande(LocalDate derniereCommande) {
		this.derniereCommande = derniereCommande;
	}
	public String getInterpretation() {
		return interpretation;
	}
	public void setInterpretation(String interpretation) {
		this.interpretation = interpretation;
	}
    

}
