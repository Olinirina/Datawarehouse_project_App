package com.Laborex.Application.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Article {
	@Id
	private String codeArticle;
	private String libelle;
	private Double prixVente;
	@ManyToOne(optional = true)
	@JoinColumn(name="codeTva",nullable = true)
	private Tva tva;
	@ManyToOne(optional = true)
	@JoinColumn(name="codeLabo",nullable = true)
	private Labo labo;
	
	public Article() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Article(String codeArticle, String libelle, Double prixVente, Tva tva, Labo labo) {
		super();
		this.codeArticle = codeArticle;
		this.libelle = libelle;
		this.prixVente = prixVente;
		this.tva = tva;
		this.labo = labo;
	}
	public String getCodeArticle() {
		return codeArticle;
	}
	public void setCodeArticle(String codeArticle) {
		this.codeArticle = codeArticle;
	}
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public Double getPrixVente() {
		return prixVente;
	}
	public void setPrixVente(Double prixVente) {
		this.prixVente = prixVente;
	}
	public Tva getTva() {
		return tva;
	}
	public void setTva(Tva tva) {
		this.tva = tva;
	}
	public Labo getLabo() {
		return labo;
	}
	public void setLabo(Labo labo) {
		this.labo = labo;
	}
	
}
