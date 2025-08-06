package com.Laborex.Application.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Concurrent {
	@Id
	private String codeConcurrent;
	private String nomConcurrent;
	public String getCodeConcurrent() {
		return codeConcurrent;
	}
	public void setCodeConcurrent(String codeConcurrent) {
		this.codeConcurrent = codeConcurrent;
	}
	public String getNomConcurrent() {
		return nomConcurrent;
	}
	public void setNomConcurrent(String nomConcurrent) {
		this.nomConcurrent = nomConcurrent;
	}
	public Concurrent() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Concurrent(String codeConcurrent, String nomConcurrent) {
		super();
		this.codeConcurrent = codeConcurrent;
		this.nomConcurrent = nomConcurrent;
	}
	
	

}
