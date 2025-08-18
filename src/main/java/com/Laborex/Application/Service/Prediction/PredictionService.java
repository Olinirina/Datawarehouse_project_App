package com.Laborex.Application.Service.Prediction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.Model.PredictionDTO.DemandePredictionDTO;

import smile.timeseries.ARMA;


@Service
public class PredictionService {

    // 🔹 Charger les données mensuelles par article
    public Map<String, List<DemandePredictionDTO>> getMonthlySalesByArticle() {
        String query = """
            SELECT v.CodeArticle, a.LibArticle, d.Mois, SUM(v.Quantite_Vendu) as total_ventes
            FROM VENTE v
            JOIN DATE_PERSO d ON v.CodeDate = d.CodeDate
            JOIN ARTICLE a ON v.CodeArticle = a.CodeArticle
            GROUP BY v.CodeArticle, a.LibArticle, d.Mois
            ORDER BY v.CodeArticle, d.Mois
        """;

        Map<String, List<DemandePredictionDTO>> ventesMap = new HashMap<>();

        try (Connection conn = DuckDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String codeArticle = rs.getString("CodeArticle");
                String libArticle = rs.getString("LibArticle");
                int mois = rs.getInt("Mois");
                Double totalVente = rs.getDouble("total_ventes");

                DemandePredictionDTO dto = new DemandePredictionDTO();
                dto.setCodeArticle(codeArticle);
                dto.setLibelle(libArticle);
                dto.setMois(mois);
                dto.setTotalVente(totalVente);

                ventesMap.computeIfAbsent(codeArticle, k -> new ArrayList<>()).add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ventesMap;
    }

    public List<DemandePredictionDTO> predictNextMonths(int nbMois) {
        Map<String, List<DemandePredictionDTO>> ventesParArticle = getMonthlySalesByArticle();
        List<DemandePredictionDTO> predictions = new ArrayList<>();
        int window = 3; // moyenne mobile sur 3 mois

        int[] moisPrevisionnels = {1, 2, 3}; // Janvier, Février, Mars

        for (String codeArticle : ventesParArticle.keySet()) {
            List<DemandePredictionDTO> ventes = ventesParArticle.get(codeArticle);

            List<Double> ventesTotales = ventes.stream()
                    .sorted(Comparator.comparingInt(DemandePredictionDTO::getMois))
                    .map(DemandePredictionDTO::getTotalVente)
                    .collect(Collectors.toList());

            for (int i = 0; i < nbMois; i++) {
                int n = ventesTotales.size();
                double sum = 0;
                int w = Math.min(window, n);
                for (int j = n - w; j < n; j++) sum += ventesTotales.get(j);
                double forecast = sum / w;
                ventesTotales.add(forecast);

                DemandePredictionDTO dto = new DemandePredictionDTO();
                dto.setCodeArticle(codeArticle);
                dto.setLibelle(ventes.get(0).getLibelle());
                dto.setMois(moisPrevisionnels[i]); // forcer janvier, février, mars
                dto.setTotalVente(forecast);
                predictions.add(dto);
            }
        }

        // Regrouper par mois et trier les articles par libelle
        Map<Integer, List<DemandePredictionDTO>> predictionsParMois = predictions.stream()
                .collect(Collectors.groupingBy(DemandePredictionDTO::getMois));

        predictionsParMois.forEach((mois, liste) ->
                liste.sort(Comparator.comparing(DemandePredictionDTO::getLibelle))
        );

        // Retourner liste finale triée par mois
        List<DemandePredictionDTO> result = new ArrayList<>();
        new TreeMap<>(predictionsParMois).forEach((mois, liste) -> result.addAll(liste));

        return result;
    }
    
   


}