package com.Laborex.Application.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Region {
	@Id
	private String codeRegion;
	private String nomRegion;
	public Region() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Region(String codeRegion, String nomRegion) {
		super();
		this.codeRegion = codeRegion;
		this.nomRegion = nomRegion;
	}
	public String getCodeRegion() {
		return codeRegion;
	}
	public void setCodeRegion(String codeRegion) {
		this.codeRegion = codeRegion;
	}
	public String getNomRegion() {
		return nomRegion;
	}
	public void setNomRegion(String nomRegion) {
		this.nomRegion = nomRegion;
	}
	

}
