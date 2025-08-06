package com.Laborex.Application.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Labo {
	@Id
	private String codeLabo;
	private String nomLabo;
	public String getCodeLabo() {
		return codeLabo;
	}
	public void setCodeLabo(String codeLabo) {
		this.codeLabo = codeLabo;
	}
	public String getNomLabo() {
		return nomLabo;
	}
	public void setNomLabo(String nomLabo) {
		this.nomLabo = nomLabo;
	}
	public Labo() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Labo(String codeLabo, String nomLabo) {
		super();
		this.codeLabo = codeLabo;
		this.nomLabo = nomLabo;
	}
	

}
