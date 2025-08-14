package com.Laborex.Application.Service.Prediction;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DemandePrevisionnelle {
	@Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Double> getVentesMensuelles2024() {
        String sql = """
            SELECT SUM(f."Quantite_Vendu") AS quantite
            FROM "Fait.VENTE" f
            JOIN "Dim.DATE" d ON f."CodeDate" = d."CodeDate"
            WHERE EXTRACT(YEAR FROM d."Date") = 2024
            GROUP BY EXTRACT(MONTH FROM d."Date")
            ORDER BY EXTRACT(MONTH FROM d."Date")
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getDouble("quantite"));
    }

}
