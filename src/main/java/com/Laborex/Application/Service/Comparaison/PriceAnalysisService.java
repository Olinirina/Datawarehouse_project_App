package com.Laborex.Application.Service.Comparaison;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.Laborex.Application.ComparaisonDTO.ComparaisonDTO;
import com.Laborex.Application.ComparaisonDTO.SimulationDTO;
import com.Laborex.Application.Dao.DuckDBConnection;

@Service
public class PriceAnalysisService {
	// Constantes pour les seuils d'alerte (configurables)
    private static final double SEUIL_MARGE_BASSE = 10.0; // 10%
    private static final double SEUIL_PRIX_ELEVE = 15.0; // 15%
    
    /**
     * Calcule l'analyse complète des prix avec toutes les métriques avancées
     * Cette méthode effectue une analyse approfondie incluant :
     * - Différences concurrentielles
     * - Statut de positionnement
     * - Classement concurrentiel
     * - Alertes de marge et prix
     * - Min/Max des prix concurrents
     */
    public List<ComparaisonDTO> calculerAnalyseComplete() {
        List<ComparaisonDTO> resultats = new ArrayList<>();

        String sql = """
            WITH prix_stats AS (
                SELECT 
                    a.CodeArticle,
                    a.LibArticle,
                    a.PrixVente,
                    MIN(comp.PrixConcurrent) as min_prix_concurrent,
                    MAX(comp.PrixConcurrent) as max_prix_concurrent,
                    AVG(comp.PrixConcurrent) as avg_prix_concurrent,
                    COUNT(comp.PrixConcurrent) as nb_concurrents
                FROM ARTICLE a
                LEFT JOIN COMPARAISON comp ON a.CodeArticle = comp.CodeArticle
                WHERE a.PrixVente IS NOT NULL AND a.PrixVente > 0
                GROUP BY a.CodeArticle, a.LibArticle, a.PrixVente
            ),
            classements AS (
                SELECT 
                    ps.CodeArticle,
                    ps.PrixVente,
                    -- Calcul du classement : nombre de concurrents moins chers + 1
                    (SELECT COUNT(*) + 1 
                     FROM COMPARAISON c2 
                     WHERE c2.CodeArticle = ps.CodeArticle 
                     AND c2.PrixConcurrent < ps.PrixVente) as classement
                FROM prix_stats ps
            )
            SELECT 
                ps.CodeArticle,
                ps.LibArticle,
                ps.PrixVente,
                c.CodeConcurrent,
                c.NomConcurrent,
                comp.PrixConcurrent,
                (comp.PrixConcurrent - ps.PrixVente) as difference_absolue,
                CASE 
                    WHEN ps.PrixVente > 0 THEN 
                        ROUND(((comp.PrixConcurrent - ps.PrixVente) / ps.PrixVente) * 100, 2)
                    ELSE 0 
                END as difference_pourcentage,
                ps.min_prix_concurrent,
                ps.max_prix_concurrent,
                cls.classement,
                -- Calcul de la marge (supposons 30% de coût, à adapter selon vos données)
                ROUND(((ps.PrixVente - (ps.PrixVente * 0.7)) / ps.PrixVente) * 100, 2) as marge_actuelle
            FROM prix_stats ps
            LEFT JOIN COMPARAISON comp ON ps.CodeArticle = comp.CodeArticle
            LEFT JOIN CONCURRENT c ON comp.CodeConcurrent = c.CodeConcurrent
            LEFT JOIN classements cls ON ps.CodeArticle = cls.CodeArticle
            WHERE comp.PrixConcurrent IS NOT NULL
            ORDER BY ps.CodeArticle, difference_pourcentage DESC
            """;

        try (Connection conn = DuckDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Double prixVente = rs.getDouble("PrixVente");
                Double prixConcurrent = rs.getDouble("PrixConcurrent");
                Double differencePourcentage = rs.getDouble("difference_pourcentage");
                Double margeActuelle = rs.getDouble("marge_actuelle");
                Double minPrix = rs.getDouble("min_prix_concurrent");
                Double maxPrix = rs.getDouble("max_prix_concurrent");
                Integer classement = rs.getInt("classement");

                // Calcul du statut de positionnement
                String statutPositionnement = calculerStatutPositionnement(prixVente, prixConcurrent);
                
                // Calcul des alertes
                Boolean alerteMargeBasse = margeActuelle < SEUIL_MARGE_BASSE;
                Boolean alertePrixEleve = Math.abs(differencePourcentage) > SEUIL_PRIX_ELEVE && differencePourcentage > 0;

                ComparaisonDTO dto = new ComparaisonDTO(
                    rs.getString("CodeArticle"),
                    rs.getString("LibArticle"),
                    prixVente,
                    rs.getString("CodeConcurrent"),
                    rs.getString("NomConcurrent"),
                    prixConcurrent,
                    rs.getDouble("difference_absolue"),
                    differencePourcentage,
                    statutPositionnement,
                    classement,
                    alerteMargeBasse,
                    alertePrixEleve,
                    minPrix,
                    maxPrix,
                    margeActuelle
                );
                resultats.add(dto);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'analyse complète : " + e.getMessage());
            throw new RuntimeException("Erreur d'analyse des prix", e);
        }

        return resultats;
    }

    /**
     * Analyse pour un article spécifique avec toutes les métriques
     */
    public List<ComparaisonDTO> calculerAnalysePourArticle(String codeArticle) {
        List<ComparaisonDTO> resultats = new ArrayList<>();

        String sql = """
            WITH prix_stats AS (
                SELECT 
                    a.CodeArticle,
                    a.LibArticle,
                    a.PrixVente,
                    MIN(comp.PrixConcurrent) as min_prix_concurrent,
                    MAX(comp.PrixConcurrent) as max_prix_concurrent,
                    AVG(comp.PrixConcurrent) as avg_prix_concurrent
                FROM ARTICLE a
                LEFT JOIN COMPARAISON comp ON a.CodeArticle = comp.CodeArticle
                WHERE a.CodeArticle = ? AND a.PrixVente IS NOT NULL AND a.PrixVente > 0
                GROUP BY a.CodeArticle, a.LibArticle, a.PrixVente
            ),
            classement AS (
                SELECT 
                    (SELECT COUNT(*) + 1 
                     FROM COMPARAISON c2 
                     WHERE c2.CodeArticle = ? 
                     AND c2.PrixConcurrent < ps.PrixVente) as rang
                FROM prix_stats ps
            )
            SELECT 
                ps.CodeArticle,
                ps.LibArticle,
                ps.PrixVente,
                c.CodeConcurrent,
                c.NomConcurrent,
                comp.PrixConcurrent,
                (comp.PrixConcurrent - ps.PrixVente) as difference_absolue,
                CASE 
                    WHEN ps.PrixVente > 0 THEN 
                        ROUND(((comp.PrixConcurrent - ps.PrixVente) / ps.PrixVente) * 100, 2)
                    ELSE 0 
                END as difference_pourcentage,
                ps.min_prix_concurrent,
                ps.max_prix_concurrent,
                cls.rang as classement,
                ROUND(((ps.PrixVente - (ps.PrixVente * 0.7)) / ps.PrixVente) * 100, 2) as marge_actuelle
            FROM prix_stats ps
            LEFT JOIN COMPARAISON comp ON ps.CodeArticle = comp.CodeArticle
            LEFT JOIN CONCURRENT c ON comp.CodeConcurrent = c.CodeConcurrent
            CROSS JOIN classement cls
            WHERE comp.PrixConcurrent IS NOT NULL
            ORDER BY difference_pourcentage DESC
            """;

        try (Connection conn = DuckDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, codeArticle);
            pstmt.setString(2, codeArticle);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Double prixVente = rs.getDouble("PrixVente");
                    Double prixConcurrent = rs.getDouble("PrixConcurrent");
                    Double differencePourcentage = rs.getDouble("difference_pourcentage");
                    Double margeActuelle = rs.getDouble("marge_actuelle");
                    
                    String statutPositionnement = calculerStatutPositionnement(prixVente, prixConcurrent);
                    Boolean alerteMargeBasse = margeActuelle < SEUIL_MARGE_BASSE;
                    Boolean alertePrixEleve = Math.abs(differencePourcentage) > SEUIL_PRIX_ELEVE && differencePourcentage > 0;

                    ComparaisonDTO dto = new ComparaisonDTO(
                        rs.getString("CodeArticle"),
                        rs.getString("LibArticle"),
                        prixVente,
                        rs.getString("CodeConcurrent"),
                        rs.getString("NomConcurrent"),
                        prixConcurrent,
                        rs.getDouble("difference_absolue"),
                        differencePourcentage,
                        statutPositionnement,
                        rs.getInt("classement"),
                        alerteMargeBasse,
                        alertePrixEleve,
                        rs.getDouble("min_prix_concurrent"),
                        rs.getDouble("max_prix_concurrent"),
                        margeActuelle
                    );
                    resultats.add(dto);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'analyse pour l'article " + codeArticle + " : " + e.getMessage());
            throw new RuntimeException("Erreur d'analyse des prix", e);
        }

        return resultats;
    }

    /**
     * Simule un nouveau prix et calcule l'impact sur toutes les métriques
     * Cette méthode permet de :
     * - Tester différents scénarios de prix
     * - Calculer l'impact sur la marge
     * - Prévoir le nouveau classement concurrentiel
     * - Analyser l'impact sur le positionnement
     */
    public SimulationDTO simulerNouveauPrix(String codeArticle, Double nouveauPrix) {
        SimulationDTO simulation = new SimulationDTO();

        String sql = """
            WITH donnees_actuelles AS (
                SELECT 
                    a.CodeArticle,
                    a.LibArticle,
                    a.PrixVente as prix_actuel,
                    MIN(comp.PrixConcurrent) as min_prix,
                    MAX(comp.PrixConcurrent) as max_prix,
                    AVG(comp.PrixConcurrent) as avg_prix,
                    (SELECT COUNT(*) + 1 
                     FROM COMPARAISON c2 
                     WHERE c2.CodeArticle = a.CodeArticle 
                     AND c2.PrixConcurrent < a.PrixVente) as classement_actuel,
                    (SELECT COUNT(*) + 1 
                     FROM COMPARAISON c3 
                     WHERE c3.CodeArticle = a.CodeArticle 
                     AND c3.PrixConcurrent < ?) as nouveau_classement
                FROM ARTICLE a
                LEFT JOIN COMPARAISON comp ON a.CodeArticle = comp.CodeArticle
                WHERE a.CodeArticle = ?
                GROUP BY a.CodeArticle, a.LibArticle, a.PrixVente
            )
            SELECT 
                CodeArticle,
                LibArticle,
                prix_actuel,
                min_prix,
                max_prix,
                avg_prix,
                classement_actuel,
                nouveau_classement,
                -- Calcul marge actuelle (supposons coût = 70% du prix de vente)
                ROUND(((prix_actuel - (prix_actuel * 0.7)) / prix_actuel) * 100, 2) as marge_actuelle,
                -- Calcul nouvelle marge
                ROUND(((?1 - (?1 * 0.7)) / ?1) * 100, 2) as nouvelle_marge,
                -- Écart concurrentiel actuel (vs moyenne)
                ROUND(((prix_actuel - avg_prix) / avg_prix) * 100, 2) as ecart_actuel,
                -- Nouvel écart concurrentiel
                ROUND(((?1 - avg_prix) / avg_prix) * 100, 2) as nouvel_ecart
            FROM donnees_actuelles
            """;

        try (Connection conn = DuckDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, nouveauPrix);
            pstmt.setString(2, codeArticle);
            pstmt.setDouble(3, nouveauPrix);
            pstmt.setDouble(4, nouveauPrix);
            pstmt.setDouble(5, nouveauPrix);
            pstmt.setDouble(6, nouveauPrix);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    simulation.setCodeArticle(rs.getString("CodeArticle"));
                    simulation.setLibelleArticle(rs.getString("LibArticle"));
                    simulation.setPrixVenteActuel(rs.getDouble("prix_actuel"));
                    simulation.setNouveauPrixVente(nouveauPrix);
                    simulation.setMargeActuelle(rs.getDouble("marge_actuelle"));
                    simulation.setNouvelleMarge(rs.getDouble("nouvelle_marge"));
                    simulation.setEcartConcurrentielActuel(rs.getDouble("ecart_actuel"));
                    simulation.setNouvelEcartConcurrentiel(rs.getDouble("nouvel_ecart"));
                    simulation.setClassementActuel(rs.getInt("classement_actuel"));
                    simulation.setNouveauClassement(rs.getInt("nouveau_classement"));
                    
                    // Calcul de l'impact sur le positionnement
                    String impactPositionnement = calculerImpactPositionnement(
                        rs.getInt("classement_actuel"), 
                        rs.getInt("nouveau_classement")
                    );
                    simulation.setImpactPositionnement(impactPositionnement);
                    simulation.setAmeliorationClassement(rs.getInt("nouveau_classement") < rs.getInt("classement_actuel"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la simulation : " + e.getMessage());
            throw new RuntimeException("Erreur de simulation", e);
        }

        return simulation;
    }

    /**
     * Calcule le statut de positionnement en comparant notre prix au prix concurrent
     * Logique : 
     * - Si différence < 1% : "Égal"
     * - Si notre prix < prix concurrent : "Moins cher"  
     * - Si notre prix > prix concurrent : "Plus cher"
     */
    private String calculerStatutPositionnement(Double prixVente, Double prixConcurrent) {
        if (prixVente == null || prixConcurrent == null) {
            return "Indéterminé";
        }
        
        double difference = Math.abs(prixVente - prixConcurrent);
        double pourcentageDifference = (difference / prixConcurrent) * 100;
        
        if (pourcentageDifference < 1.0) {
            return "Égal";
        } else if (prixVente < prixConcurrent) {
            return "Moins cher";
        } else {
            return "Plus cher";
        }
    }

    /**
     * Calcule l'impact d'un changement de prix sur le classement
     * Analyse l'évolution du positionnement concurrentiel
     */
    private String calculerImpactPositionnement(Integer classementActuel, Integer nouveauClassement) {
        if (classementActuel == null || nouveauClassement == null) {
            return "Indéterminé";
        }
        
        int difference = nouveauClassement - classementActuel;
        
        if (difference == 0) {
            return "Aucun changement";
        } else if (difference < 0) {
            return "Amélioration de " + Math.abs(difference) + " place(s)";
        } else {
            return "Dégradation de " + difference + " place(s)";
        }
    }

}
