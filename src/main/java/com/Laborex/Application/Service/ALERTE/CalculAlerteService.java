package com.Laborex.Application.Service.ALERTE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.Model.Alerte.AlerteDTO;
import com.Laborex.Application.Model.Alerte.NiveauSeverite;

import com.Laborex.Application.Model.Alerte.TypeAlerte;
import com.Laborex.Application.Service.ALERTE.Configuration.AlerteConfiguration;

@Service
public class CalculAlerteService {
	@Autowired
    private SeuilCalculService seuilCalculService;
	@Autowired
    private AlerteConfiguration alerteConfig;
	
	private static final Logger log = LoggerFactory.getLogger(CalculAlerteService.class);
	
	//STOCK CRITIQUE
	public List<AlerteDTO> detecterStockCritique(){
		List<AlerteDTO> stockCritique= new ArrayList<>();
		// Requête pour récupérer tous les stocks actuels
        String sql = """
            SELECT s.CodeStock, s.QuantiteStocke, a.CodeArticle, a.LibArticle 
            FROM STOCK s 
            JOIN ARTICLE a ON s.CodeArticle = a.CodeArticle 
            WHERE s.QuantiteStocke > 0
        """;
        
        try (Connection conn = DuckDBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

               while (rs.next()) {
                   String codeArticle = rs.getString("CodeArticle");
                   String libelle = rs.getString("LibArticle");
                   // Utiliser getInt() pour QuantiteStocke
                   int quantiteActuelle = rs.getInt("QuantiteStocke");
                   
                   // Calculer le seuil critique pour cet article
                   double seuilCritique = seuilCalculService.calculerSeuilCritique(codeArticle);
                   
                   // Vérifier si le stock est en dessous du seuil
                   if (quantiteActuelle <= seuilCritique) {
                	    NiveauSeverite severite = seuilCalculService.determinerSeveriteStock(quantiteActuelle, seuilCritique);

                	    String titre = "Stock critique: " + libelle;
                	    String description;

                	    // Adapter la description selon la sévérité
                	    switch (severite) {
                	        case FAIBLE -> description = String.format(
                	            "L'article '%s' a un stock de %d unités, légèrement en dessous du seuil critique de %.0f unités. " +
                	            "Surveiller le réapprovisionnement prochainement.",
                	            libelle, quantiteActuelle, seuilCritique
                	        );
                	        case MODERE -> description = String.format(
                	            "L'article '%s' a un stock de %d unités, en dessous du seuil critique de %.0f unités. " +
                	            "Réapprovisionnement conseillé bientôt.",
                	            libelle, quantiteActuelle, seuilCritique
                	        );
                	        case ELEVE -> description = String.format(
                	            "L'article '%s' a un stock de %d unités, bien en dessous du seuil critique de %.0f unités. " +
                	            "Réapprovisionnement urgent recommandé.",
                	            libelle, quantiteActuelle, seuilCritique
                	        );
                	        case CRITIQUE -> description = String.format(
                	            "⚠ URGENCE : L'article '%s' a un stock de seulement %d unités, très en dessous du seuil critique de %.0f unités. " +
                	            "Rupture imminente, agir immédiatement !",
                	            libelle, quantiteActuelle, seuilCritique
                	        );
                	        default -> description = "Stock non évalué";
                	    }

                	    AlerteDTO dto = new AlerteDTO();
                	    dto.setCodeReference(codeArticle);
                	    dto.setDescription(description);
                	    dto.setSeverite(severite);
                	    dto.setTitre(titre);
                	    dto.setType(TypeAlerte.STOCK_CRITIQUE);
                	    dto.setValeur(quantiteActuelle);
                	    stockCritique.add(dto);
                	}

                   
                   
               }
           }catch (SQLException e) {
        	   log.warn("Erreur lors de la détection des stocks critiques" + e);               
           }

           return stockCritique;
	}
	
	//CLIENTS INACTIFS
	public List<AlerteDTO> detecterClientsInactifs(){
		List<AlerteDTO> clientsInactifs = new ArrayList<>();
			String sql = """             
            	SELECT
				     c.CodeCli,
				     c.NomCli,
				     COALESCE(EXTRACT(MONTH FROM AGE((SELECT MAX(CAST(DateValue AS DATE)) FROM DATE_PERSO), MAX(CAST(d.DateValue AS DATE)))), 12) AS mois_inactif,
				     COUNT(v.CodeVente) AS nb_ventes_historique
				 FROM CLIENT c
				 LEFT JOIN VENTE v ON c.CodeCli = v.CodeClient
				 LEFT JOIN DATE_PERSO d ON v.CodeDate = d.CodeDate
				 GROUP BY
				     c.CodeCli,
				     c.NomCli
				 HAVING
				     mois_inactif >= ?
				 ORDER BY
				     mois_inactif DESC
            """;
            try (Connection conn = DuckDBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Définir le seuil d'inactivité à partir de la configuration
                pstmt.setInt(1, alerteConfig.getMoisSeuilClientInactif());

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String codeClient = rs.getString("CodeCli");
                        String nomClient = rs.getString("NomCli");
                        Integer moisInactif = rs.getInt("mois_inactif");
                        Integer nbVentesHistorique = rs.getInt("nb_ventes_historique");
                        
                        // Ne pas alerter pour les clients qui n'ont jamais acheté
                        if (nbVentesHistorique == 0) {
                            continue;
                        }
                        
                        NiveauSeverite severite = seuilCalculService.determinerSeveriteInactivite(moisInactif);
                        
                        String titre = "Client inactif: " + nomClient;
                        String description;
                        switch (severite) {
                        case FAIBLE -> description = String.format(
                                "Le client '%s' (%s) présente une faible inactivité (%d mois). " +
                                "Historique : %d vente(s). Un simple rappel pourrait suffire.",
                                nomClient, codeClient, moisInactif, nbVentesHistorique
                            );
                            
                            case MODERE -> description = String.format(
                                "Le client '%s' (%s) n'a pas effectué d'achat depuis %d mois. " +
                                "Il avait %d vente(s) dans l'historique. Considérer une action de relance.",
                                nomClient, codeClient, moisInactif, nbVentesHistorique
                            );
                            
                            case ELEVE -> description = String.format(
                                "Le client '%s' (%s) est inactif depuis %d mois. " +
                                "Il avait %d vente(s) dans le passé. Risque élevé de perte, une action commerciale est fortement recommandée.",
                                nomClient, codeClient, moisInactif, nbVentesHistorique
                            );
                            
                            case CRITIQUE -> description = String.format(
                                "Le client '%s' (%s) est inactif depuis %d mois ! " +
                                "Il avait %d vente(s) dans l'historique. Situation critique : contacter immédiatement pour éviter la perte définitive.",
                                nomClient, codeClient, moisInactif, nbVentesHistorique
                            );
                            
                            default -> description = "Client non évalué";
            	    }
                        AlerteDTO dto = new AlerteDTO();
                	    dto.setCodeReference(codeClient);
                	    dto.setDescription(description);
                	    dto.setSeverite(severite);
                	    dto.setTitre(titre);
                	    dto.setType(TypeAlerte.CLIENT_INACTIF);
                	    dto.setValeur(moisInactif);
                	    clientsInactifs.add(dto);
                        
                    }
                    
                }
                   
        } catch (Exception e) {
            System.err.println("Erreur lors de la détection des clients inactifs" + e);
        }
            return clientsInactifs;     
	}
	
	//ANOMALIES DES VENTES
	public List<AlerteDTO> detecterAnomaliesVentes(){
		List<AlerteDTO> anomaliesVentes= new ArrayList<>();
		String sql = """
                WITH ventes_recentes AS (
                     SELECT
                         v.CodeArticle,
                         a.LibArticle,
                         SUM(v.Quantite_Vendu) AS ventes_7_jours
                     FROM VENTE v
                     JOIN DATE_PERSO d ON v.CodeDate = d.CodeDate
                     JOIN ARTICLE a ON v.CodeArticle = a.CodeArticle
                     WHERE
                         CAST(d.DateValue AS DATE) >= (SELECT MAX(CAST(DateValue AS DATE)) FROM DATE_PERSO) - INTERVAL '7 days'
                    GROUP BY
                         v.CodeArticle,
                         a.LibArticle
                 ), ventes_moyennes AS (
                     SELECT
                         v.CodeArticle,
                         AVG(v.Quantite_Vendu) * 7 AS moyenne_7_jours_mensuelle
                     FROM VENTE v
                     JOIN DATE_PERSO d ON v.CodeDate = d.CodeDate
                     WHERE
                         CAST(d.DateValue AS DATE) >= (SELECT MAX(CAST(DateValue AS DATE)) FROM DATE_PERSO) - INTERVAL '30 days'
                        AND CAST(d.DateValue AS DATE) < (SELECT MAX(CAST(DateValue AS DATE)) FROM DATE_PERSO) - INTERVAL '7 days'
                     GROUP BY
                         v.CodeArticle
                 )
                 SELECT
                     r.CodeArticle,
                     r.LibArticle,
                     r.ventes_7_jours,
                    COALESCE(m.moyenne_7_jours_mensuelle, 0) AS moyenne_attendue,
                     CASE
                         WHEN COALESCE(m.moyenne_7_jours_mensuelle, 0) = 0 THEN 0
                         ELSE ABS(r.ventes_7_jours - m.moyenne_7_jours_mensuelle) / m.moyenne_7_jours_mensuelle * 100
                    END AS pourcentage_ecart
                 FROM ventes_recentes r
                 LEFT JOIN ventes_moyennes m ON r.CodeArticle = m.CodeArticle
                 WHERE
                     CASE
                        WHEN COALESCE(m.moyenne_7_jours_mensuelle, 0) = 0 THEN 0
                        ELSE ABS(r.ventes_7_jours - COALESCE(m.moyenne_7_jours_mensuelle, 0)) / COALESCE(m.moyenne_7_jours_mensuelle, 1) * 100
                     END >= ?
                 ORDER BY
                     pourcentage_ecart DESC
            """;
		try (Connection conn = DuckDBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

               // Définir le seuil d'anomalie en pourcentage à partir de la configuration
               pstmt.setDouble(1, alerteConfig.getSeuilAnomalieVentePourcent());
           
               try (ResultSet rs = pstmt.executeQuery()) {
                   int anomaliesDetectees = 0;
                   while (rs.next()) {
                       String codeArticle = rs.getString("CodeArticle");
                       String libelle = rs.getString("LibArticle");
                       Double ventesRecentes = rs.getDouble("ventes_7_jours");
                       Double moyenneAttendue = rs.getDouble("moyenne_attendue");
                       Double pourcentageEcart = rs.getDouble("pourcentage_ecart");
                       
                       // Déterminer si c'est un pic ou une chute
                       boolean estUnPic = ventesRecentes > moyenneAttendue;
                       String typeAnomalie = estUnPic ? "pic" : "chute";
                       
                       // Déterminer la sévérité basée sur le pourcentage d'écart
                       NiveauSeverite severite = seuilCalculService.determinerSeveriteAnomalies(pourcentageEcart);
                       
                       String titre = String.format("Anomalie de vente (%s): %s", typeAnomalie, libelle);
                       String description = String.format(
                           "L'article '%s' présente un %s de vente inhabituel. " +
                           "Ventes des 7 derniers jours: %.0f unités (moyenne attendue: %.0f unités). " +
                           "Écart: %.1f%%. %s",
                           libelle, typeAnomalie, ventesRecentes, moyenneAttendue, pourcentageEcart,
                           estUnPic ? "Vérifier la disponibilité du stock." : 
                                     "Analyser les causes de la baisse de demande."
                       );
                       
                       switch (severite) {
                       case FAIBLE -> description = String.format(
                           "L'article '%s' montre une légère variation dans ses ventes (%s). " +
                           "Ventes récentes: %.0f unités (moyenne: %.0f unités). " +
                           "Écart observé: %.1f%%. Aucune action immédiate requise, mais garder un œil sur l'évolution.",
                           libelle, typeAnomalie, ventesRecentes, moyenneAttendue, pourcentageEcart
                       );

                       case MODERE -> description = String.format(
                           "L'article '%s' présente un %s de vente inhabituel. " +
                           "Ventes des 7 derniers jours: %.0f unités (moyenne attendue: %.0f unités). " +
                           "Écart: %.1f%%. %s",
                           libelle, typeAnomalie, ventesRecentes, moyenneAttendue, pourcentageEcart,
                           estUnPic ? "Vérifier la disponibilité du stock." : 
                                     "Analyser les causes de la baisse de demande."
                       );

                       case ELEVE -> description = String.format(
                           "Anomalie significative détectée pour l'article '%s'. " +
                           "%s constaté avec un écart de %.1f%% par rapport à la moyenne attendue (%.0f vs %.0f unités). " +
                           "Recommandation: %s",
                           libelle, typeAnomalie, pourcentageEcart, ventesRecentes, moyenneAttendue,
                           estUnPic ? "renforcer le stock pour éviter les ruptures." : 
                                     "envisager des actions marketing ou promotionnelles."
                       );

                       case CRITIQUE -> description = String.format(
                           "⚠️ Anomalie critique pour l'article '%s' ! %s majeur détecté. " +
                           "Ventes actuelles: %.0f unités, alors que la moyenne attendue est %.0f unités (écart: %.1f%%). " +
                           "%s",
                           libelle, typeAnomalie, ventesRecentes, moyenneAttendue, pourcentageEcart,
                           estUnPic ? "Risque de rupture imminente : réalimenter d'urgence." : 
                                     "Risque de désintérêt client : alerter le service commercial."
                       );

                       default -> description = "Vente non évaluée";
                   }  
                       
                       AlerteDTO dto = new AlerteDTO();
               	    dto.setCodeReference(codeArticle);
               	    dto.setDescription(description);
               	    dto.setSeverite(severite);
               	    dto.setTitre(titre);
               	    dto.setType(TypeAlerte.ANOMALIE_VENTE);
               	   
               	    anomaliesVentes.add(dto);
                       anomaliesDetectees++;                 
                   System.out.println("Détection des anomalies de vente terminée. " + anomaliesDetectees + " anomalies détectées.");
               }
           }
       } catch (Exception e) {
           System.err.println("Erreur lors de la détection des anomalies de vente" + e);
       }
		
		
		
		
		return anomaliesVentes;
	}
	
}
		
	



