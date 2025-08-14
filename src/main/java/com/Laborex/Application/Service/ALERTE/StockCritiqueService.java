package com.Laborex.Application.Service.ALERTE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.Model.Alerte.NiveauSeverite;
import com.Laborex.Application.Model.Alerte.TypeAlerte;

@Service
public class StockCritiqueService {

    @Autowired
    private AlerteService alerteService;
    
    @Autowired
    private SeuilCalculService seuilCalculService;
    
    /**
     * Détecte les articles avec un stock critique
     */
    public void detecterStocksCritiques() {
        System.out.println("Début de la détection des stocks critiques");
        
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
            
            System.out.println("Détection des stocks critiques terminée");
            
        } catch (Exception e) {
             System.err.println("Erreur lors de la détection des stocks critiques" + e);
        }
    }
}