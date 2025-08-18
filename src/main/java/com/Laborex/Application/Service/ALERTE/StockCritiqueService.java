package com.Laborex.Application.Service.ALERTE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.Model.Alerte.NiveauSeverite;
import com.Laborex.Application.Model.Alerte.TypeAlerte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class StockCritiqueService {
	private static final Logger log = LoggerFactory.getLogger(StockCritiqueService.class);

    @Autowired
    private AlerteService alerteService;
    
    @Autowired
    private SeuilCalculService seuilCalculService;
    
    /**
     * Détecte les articles avec un stock critique
     */
    public void detecterStocksCritiques() {
        log.info("Début de la détection des stocks critiques");
        
        try {
            // Requête pour récupérer tous les stocks actuels
            // Note: La jointure est corrigée pour être 's.CodeArticle = a.CodeArticle'
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
                        String description = String.format(
                            "L'article '%s' a un stock de %d unités, en dessous du seuil critique de %.0f unités. " +
                            "Réapprovisionnement urgent recommandé.",
                            libelle, quantiteActuelle, seuilCritique
                        );
                        log.warn("Stock faible détecté...");
                        
                        alerteService.creerOuMettreAJourAlerte(
                            TypeAlerte.STOCK_CRITIQUE,
                            titre,
                            description,
                            codeArticle,
                            (double) quantiteActuelle,
                            severite
                        );
                    }
                }
            }
            
            log.info("Détection des stocks critiques terminée");
            
        } catch (Exception e) {
             log.warn("Erreur lors de la détection des stocks critiques" + e);
        }
    }
}