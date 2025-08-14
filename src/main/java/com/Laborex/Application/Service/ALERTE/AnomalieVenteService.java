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
public class AnomalieVenteService {
 
    @Autowired
    private AlerteService alerteService;
    
    @Autowired
    private AlerteConfiguration alerteConfig;
    
    /**
     * Détecte les anomalies dans les ventes (pics ou chutes inhabituelles)
     */
    public void detecterAnomaliesVente() {
        System.out.println("Début de la détection des anomalies de vente");
        
        try {
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
                        NiveauSeverite severite;
                        if (pourcentageEcart >= 200) severite = NiveauSeverite.CRITIQUE;
                        else if (pourcentageEcart >= 100) severite = NiveauSeverite.ELEVE;
                        else if (pourcentageEcart >= 75) severite = NiveauSeverite.MODERE;
                        else severite = NiveauSeverite.FAIBLE;
                        
                        String titre = String.format("Anomalie de vente (%s): %s", typeAnomalie, libelle);
                        String description = String.format(
                            "L'article '%s' présente un %s de vente inhabituel. " +
                            "Ventes des 7 derniers jours: %.0f unités (moyenne attendue: %.0f unités). " +
                            "Écart: %.1f%%. %s",
                            libelle, typeAnomalie, ventesRecentes, moyenneAttendue, pourcentageEcart,
                            estUnPic ? "Vérifier la disponibilité du stock." : 
                                      "Analyser les causes de la baisse de demande."
                        );
                        
                        alerteService.creerOuMettreAJourAlerte(
                            TypeAlerte.ANOMALIE_VENTE,
                            titre,
                            description,
                            codeArticle,
                            pourcentageEcart,
                            severite
                        );
                        anomaliesDetectees++;
                    }
                    System.out.println("Détection des anomalies de vente terminée. " + anomaliesDetectees + " anomalies détectées.");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la détection des anomalies de vente" + e);
        }
    }
}