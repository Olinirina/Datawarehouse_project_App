package com.Laborex.Application.SegmentationDTO;

public class ClassificationACBDTO {
	private String nomElement; // Nom du client, produit ou labo
    private double chiffreAffaires;
    private String classe; // A, B ou C
    private String interpretation; 
	public ClassificationACBDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ClassificationACBDTO(String nomElement, double chiffreAffaires, String classe, String interpretation) {
		super();
		this.nomElement = nomElement;
		this.chiffreAffaires = chiffreAffaires;
		this.classe = classe;
		this.interpretation= interpretation;
	}
	
	public String getInterpretation() {
		return interpretation;
	}
	public void setInterpretation(String interpretation) {
		this.interpretation = interpretation;
	}
	public String getNomElement() {
		return nomElement;
	}
	public void setNomElement(String nomElement) {
		this.nomElement = nomElement;
	}
	public double getChiffreAffaires() {
		return chiffreAffaires;
	}
	public void setChiffreAffaires(double chiffreAffaires) {
		this.chiffreAffaires = chiffreAffaires;
	}
	public String getClasse() {
		return classe;
	}
	public void setClasse(String classe) {
		this.classe = classe;
	}
    

}
