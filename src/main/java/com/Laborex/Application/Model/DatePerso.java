package com.Laborex.Application.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class DatePerso {
	@Id
	private String codeDate;
	private java.sql.Date date;
	private int jour;
	private int mois;
	private int annee;
	public DatePerso() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DatePerso(String codeDate, java.sql.Date date, int jour, int mois, int annee) {
		super();
		this.codeDate = codeDate;
		this.date = date;
		this.jour = jour;
		this.mois = mois;
		this.annee = annee;
	}
	public String getCodeDate() {
		return codeDate;
	}
	public void setCodeDate(String codeDate) {
		this.codeDate = codeDate;
	}
	public java.sql.Date getDate() {
		return date;
	}
	public void setDate(java.sql.Date date) {
		this.date = date;
	}
	public int getJour() {
		return jour;
	}
	public void setJour(int jour) {
		this.jour = jour;
	}
	public int getMois() {
		return mois;
	}
	public void setMois(int mois) {
		this.mois = mois;
	}
	public int getAnnee() {
		return annee;
	}
	public void setAnnee(int annee) {
		this.annee = annee;
	}
	

}
