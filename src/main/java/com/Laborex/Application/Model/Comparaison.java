package com.Laborex.Application.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Comparaison {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codeComparaison;
	private Double prixConcurrent;
	private Double variation;
	private Double nouveauPrix;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="codeConcurrent")
	private Concurrent concurrent;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="codeArticle")
	private Article article;
	public Comparaison() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Long getCodeComparaison() {
		return codeComparaison;
	}
	public void setCodeComparaison(Long codeComparaison) {
		this.codeComparaison = codeComparaison;
	}
	public Double getPrixConcurrent() {
		return prixConcurrent;
	}
	public void setPrixConcurrent(Double prixConcurrent) {
		this.prixConcurrent = prixConcurrent;
	}
	public Double getVariation() {
		return variation;
	}
	public void setVariation(Double variation) {
		this.variation = variation;
	}
	public Double getNouveauPrix() {
		return nouveauPrix;
	}
	public void setNouveauPrix(Double nouveauPrix) {
		this.nouveauPrix = nouveauPrix;
	}
	public Concurrent getConcurrent() {
		return concurrent;
	}
	public void setConcurrent(Concurrent concurrent) {
		this.concurrent = concurrent;
	}
	public Article getArticle() {
		return article;
	}
	public void setArticle(Article article) {
		this.article = article;
	}
	public Comparaison(Double prixConcurrent, Double variation, Double nouveauPrix, Concurrent concurrent,
			Article article) {
		super();
		this.prixConcurrent = prixConcurrent;
		this.variation = variation;
		this.nouveauPrix = nouveauPrix;
		this.concurrent = concurrent;
		this.article = article;
	}
	
	

}
