package com.Laborex.Application.SegmentationDTO;

public class ClientRFMDTO {
	private String nomClient;
    private long recence; // Nombre de jours depuis la dernière commande
    private long frequence; // Nombre total de commandes
    private double montant; // Montant total dépensé
    private int scoreR;
    private int scoreF;
    private int scoreM;
    private String segmentRFM;
    private String interpretation;
	public ClientRFMDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ClientRFMDTO(String nomClient, long recence, long frequence, double montant, int scoreR, int scoreF,
			int scoreM, String segmentRFM, String interpretation) {
		super();
		this.nomClient = nomClient;
		this.recence = recence;
		this.frequence = frequence;
		this.montant = montant;
		this.scoreR = scoreR;
		this.scoreF = scoreF;
		this.scoreM = scoreM;
		this.segmentRFM = segmentRFM;
		this.interpretation = interpretation;
	}
	public String getNomClient() {
		return nomClient;
	}
	public void setNomClient(String nomClient) {
		this.nomClient = nomClient;
	}
	public long getRecence() {
		return recence;
	}
	public void setRecence(long recence) {
		this.recence = recence;
	}
	public long getFrequence() {
		return frequence;
	}
	public void setFrequence(long frequence) {
		this.frequence = frequence;
	}
	public double getMontant() {
		return montant;
	}
	public void setMontant(double montant) {
		this.montant = montant;
	}
	public int getScoreR() {
		return scoreR;
	}
	public void setScoreR(int scoreR) {
		this.scoreR = scoreR;
	}
	public int getScoreF() {
		return scoreF;
	}
	public void setScoreF(int scoreF) {
		this.scoreF = scoreF;
	}
	public int getScoreM() {
		return scoreM;
	}
	public void setScoreM(int scoreM) {
		this.scoreM = scoreM;
	}
	public String getSegmentRFM() {
		return segmentRFM;
	}
	public void setSegmentRFM(String segmentRFM) {
		this.segmentRFM = segmentRFM;
	}
	public String getInterpretation() {
		return interpretation;
	}
	public void setInterpretation(String interpretation) {
		this.interpretation = interpretation;
	}
    

}
