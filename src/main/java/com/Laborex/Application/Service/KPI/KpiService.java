package com.Laborex.Application.Service.KPI;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.KPI.DTO.CaClientDTO;
//Contient la logique de calcul des Kpi
@Service
public class KpiService {

	//Methode pour calculer le CA par client
    public List<CaClientDTO> getChiffreAffairesParClient() {
        List<CaClientDTO> result = new ArrayList<>();

        //Requete SQL analytique
        String sql = """
            SELECT c.NomCli, SUM(v.Montant_Vente) AS CA
            FROM VENTE v
            JOIN CLIENT c ON v.CodeClient = c.CodeCli
            GROUP BY c.NomCli
            ORDER BY CA DESC
        """;

        //Execution de cette requete
        try (Connection conn = DuckDBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
            	//Ajouter dans la liste d'objets 
                result.add(new CaClientDTO(
                        rs.getString("NomCli"),
                        rs.getDouble("CA")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // à améliorer avec une gestion d'erreur
        }

        return result;
    }
}
