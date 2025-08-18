package com.Laborex.Application.Model.Alerte;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class AlerteDTO {
	 @Enumerated(EnumType.STRING)
	    private TypeAlerte type;
	    private String titre;
	    private String description;	  
	    private String CodeReference; // Code article, client, vente.	    
	    private Integer valeur; // Quantité, nombre de jours, ....	    
	    @Enumerated(EnumType.STRING)
	    private NiveauSeverite severite;
		public AlerteDTO() {
			super();
			// TODO Auto-generated constructor stub
		}
		public AlerteDTO(TypeAlerte type, String titre, String description, String CodeReference, Integer valeur,
				NiveauSeverite severite) {
			super();
			this.type = type;
			this.titre = titre;
			this.description = description;
			this.CodeReference = CodeReference;
			this.valeur = valeur;
			this.severite = severite;
		}
		public TypeAlerte getType() {
			return type;
		}
		public void setType(TypeAlerte type) {
			this.type = type;
		}
		public String getTitre() {
			return titre;
		}
		public void setTitre(String titre) {
			this.titre = titre;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		
		public String getCodeReference() {
			return CodeReference;
		}
		public void setCodeReference(String codeReference) {
			CodeReference = codeReference;
		}
		public Integer getValeur() {
			return valeur;
		}
		public void setValeur(Integer valeur) {
			this.valeur = valeur;
		}
		public NiveauSeverite getSeverite() {
			return severite;
		}
		public void setSeverite(NiveauSeverite severite) {
			this.severite = severite;
		}
	    

}
