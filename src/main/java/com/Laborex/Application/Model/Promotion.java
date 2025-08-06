package com.Laborex.Application.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;


@Entity
public class Promotion {
	@Id
	private String codePromo;
	private String nomPromo;
	private String typePromo;
	private Integer ugLivre;
	public Promotion() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Promotion(String codePromo, String nomPromo, String typePromo, Integer ugLivre) {
		super();
		this.codePromo = codePromo;
		this.nomPromo = nomPromo;
		this.typePromo = typePromo;
		this.ugLivre = ugLivre;
	}
	public String getCodePromo() {
		return codePromo;
	}
	public void setCodePromo(String codePromo) {
		this.codePromo = codePromo;
	}
	public String getNomPromo() {
		return nomPromo;
	}
	public void setNomPromo(String nomPromo) {
		this.nomPromo = nomPromo;
	}
	public String getTypePromo() {
		return typePromo;
	}
	public void setTypePromo(String typePromo) {
		this.typePromo = typePromo;
	}
	public Integer getUgLivre() {
		return ugLivre;
	}
	public void setUgLivre(Integer ugLivre) {
		this.ugLivre = ugLivre;
	}
	
	

}
