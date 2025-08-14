package com.Laborex.Application.Service.ALERTE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.Model.Alerte.NiveauSeverite;
import com.Laborex.Application.Model.Alerte.TypeAlerte;
import com.Laborex.Application.Service.ALERTE.Configuration.AlerteConfiguration;

@Service
public class ClientInactifService {

    @Autowired
    private AlerteService alerteService;
    
    @Autowired
    private SeuilCalculService seuilCalculService;
    
    @Autowired
    private AlerteConfiguration alerteConfig;
    
    /**
     * Détecte les clients qui n'ont pas acheté depuis longtemps
     */
    public void detecterClientsInactifs() {
        System.out.println("Début de la détection des clients inactifs");
        
        try {
            // Requête pour trouver les clients inactifs
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
            
            // On peut maintenant utiliser un PreparedStatement car la requête a un paramètre
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
                        String description = String.format(
                            "Le client '%s' (%s) n'a pas effectué d'achat depuis %d mois. " +
                            "Il avait %d vente(s) dans l'historique. Considérer une action de relance.",
                            nomClient, codeClient, moisInactif, nbVentesHistorique
                        );
                        
                        alerteService.creerOuMettreAJourAlerte(
                            TypeAlerte.CLIENT_INACTIF,
                            titre,
                            description,
                            codeClient,
                            moisInactif.doubleValue(),
                            severite
                        );
                    }
                }
            }
            
            System.out.println("Détection des clients inactifs terminée.");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la détection des clients inactifs" + e);
        }
    }
}