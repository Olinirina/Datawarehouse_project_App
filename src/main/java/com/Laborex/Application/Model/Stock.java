package com.Laborex.Application.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Stock {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	public Long codeStock;
	public Integer quantiteStocke;
	public Integer coursRoute;
	@OneToOne
    @JoinColumn(name = "codeArticle", unique = true)
    private Article article;
	public Stock() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Stock(Integer quantiteStocke, Integer coursRoute, Article article) {
		super();
		this.quantiteStocke = quantiteStocke;
		this.coursRoute = coursRoute;
		this.article = article;
	}
	public Long getCodeStock() {
		return codeStock;
	}
	public void setCodeStock(Long codeStock) {
		this.codeStock = codeStock;
	}
	public Integer getQuantiteStocke() {
		return quantiteStocke;
	}
	public void setQuantiteStocke(Integer quantiteStocke) {
		this.quantiteStocke = quantiteStocke;
	}
	public Integer getCoursRoute() {
		return coursRoute;
	}
	public void setCoursRoute(Integer coursRoute) {
		this.coursRoute = coursRoute;
	}
	public Article getArticle() {
		return article;
	}
	public void setArticle(Article article) {
		this.article = article;
	}
	
	

}
