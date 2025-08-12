package com.Laborex.Application.Model;

import jakarta.persistence.*;

@Entity
public class Vente {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVente;
    public Long getIdVente() {
		return idVente;
	}

	public void setIdVente(Long idVente) {
		this.idVente = idVente;
	}

	private String codeVente;
    private int quantiteVendu;
    private double montantVente;

    @ManyToOne
    @JoinColumn(name = "codeCli")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "codeArticle")
    private Article article;

    @ManyToOne
    @JoinColumn(name = "codeDate") // supposé être une entité personnalisée Date
    private DatePerso date;
    @ManyToOne
    @JoinColumn(name = "codePromo")
    private Promotion promotion;

    public Vente() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Vente(String codeVente, int quantiteVendu, double montantVente, Client client,
			Article article, DatePerso date, Promotion promotion) {
		super();
		this.codeVente = codeVente;
		this.quantiteVendu = quantiteVendu;
		this.montantVente = montantVente;
		
		this.client = client;
		this.article = article;
		this.date = date;
		this.promotion = promotion;
	}

	public Promotion getPromotion() {
		return promotion;
	}

	public void setPromotion(Promotion promotion) {
		this.promotion = promotion;
	}

	// Getters et Setters
    public String getCodeVente() {
        return codeVente;
    }

    public void setCodeVente(String codeVente) {
        this.codeVente = codeVente;
    }

    public int getQuantiteVendu() {
        return quantiteVendu;
    }

    public void setQuantiteVendu(int quantiteVendu) {
        this.quantiteVendu = quantiteVendu;
    }

    public double getMontantVente() {
        return montantVente;
    }

    public void setMontantVente(double montantVente) {
        this.montantVente = montantVente;
    }

   
    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public DatePerso getDate() {
        return date;
    }

    public void setDate(DatePerso date) {
        this.date = date;
    }
}

