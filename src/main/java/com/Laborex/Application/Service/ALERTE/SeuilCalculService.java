package com.Laborex.Application.Service.ALERTE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.springframework.stereotype.Service;

import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.Model.Alerte.NiveauSeverite;

@Service
public class SeuilCalculService {

    /**
     * Calcule le seuil critique pour un article basé sur l'historique des ventes
     * Formule : Moyenne des ventes sur 30 jours * coefficient de sécurité (1.5) * nombre de jours (7)
     */
    public double calculerSeuilCritique(String codeArticle) {
        Double moyenneVente = 0.0; // Initialisation par défaut

        try {
            // Requête SQL pour calculer la moyenne des ventes sur 30 jours pour l'article donné
            String sql = """
               SELECT
				     COALESCE(AVG(v.Quantite_Vendu), 0) as moyenne_vente
				FROM VENTE v
				JOIN DATE_PERSO d ON v.CodeDate = d.CodeDate
				WHERE v.CodeArticle = ?
				CAST(d.DateValue AS DATE) >= (SELECT MAX(CAST(DateValue AS DATE)) FROM DATE_PERSO) - INTERVAL '30 days'
            """;

            try (Connection conn = DuckDBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Définir le paramètre de la requête
                pstmt.setString(1, codeArticle);

                // Exécuter la requête
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        moyenneVente = rs.getDouble("moyenne_vente");
                    }
                }
            }

            if (moyenneVente == null || moyenneVente == 0) {
                // Si pas d'historique de ventes, utiliser un seuil par défaut de 10
                return 10.0;
            }

            // Coefficient de sécurité : 1.5 fois la moyenne quotidienne * 7 jours (pour une semaine de stock)
            return moyenneVente * 1.5 * 7;

        } catch (Exception e) {
            System.err.println("Erreur lors du calcul du seuil critique pour l'article " + codeArticle + ": " + e.getMessage());
            // En cas d'erreur, retourner un seuil par défaut pour éviter un crash
            return 10.0;
        }
    }

    /**
     * Détermine le niveau de sévérité basé sur le ratio stock/seuil
     */
    public NiveauSeverite determinerSeveriteStock(double stock, double seuil) {
        double ratio = stock / seuil;

        if (ratio <= 0.25) return NiveauSeverite.CRITIQUE;
        if (ratio <= 0.5) return NiveauSeverite.ELEVE;
        if (ratio <= 0.75) return NiveauSeverite.MODERE;
        return NiveauSeverite.FAIBLE;
    }

    /**
     * Détermine la sévérité pour l'inactivité client
     */
    public NiveauSeverite determinerSeveriteInactivite(int moisInactif) {
        if (moisInactif >= 12) return NiveauSeverite.CRITIQUE;
        if (moisInactif >= 6) return NiveauSeverite.ELEVE;
        if (moisInactif >= 3) return NiveauSeverite.MODERE;
        return NiveauSeverite.FAIBLE;
    }
}