package com.Laborex.Application.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Tva {
	@Id
	private String codeTva;
	private Double taux;
	private String nature;
	public Tva() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Tva(String codeTva, Double taux, String nature) {
		super();
		this.codeTva = codeTva;
		this.taux = taux;
		this.nature = nature;
	}
	public String getCodeTva() {
		return codeTva;
	}
	public void setCodeTva(String codeTva) {
		this.codeTva = codeTva;
	}
	public Double getTaux() {
		return taux;
	}
	public void setTaux(Double taux) {
		this.taux = taux;
	}
	public String getNature() {
		return nature;
	}
	public void setNature(String nature) {
		this.nature = nature;
	}
	
	
	
	
	

}
