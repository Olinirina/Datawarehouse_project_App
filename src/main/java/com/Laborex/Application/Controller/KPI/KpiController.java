package com.Laborex.Application.Controller.KPI;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.KPI.DTO.CaVenteDTO;
import com.Laborex.Application.KPI.DTO.FrequenceVenteDTO;
import com.Laborex.Application.KPI.DTO.ImpactPromotionDTO;
import com.Laborex.Application.KPI.DTO.PerformanceClientDTO;
import com.Laborex.Application.KPI.DTO.PerformanceLaboDTO;
import com.Laborex.Application.KPI.DTO.SaisonaliteDTO;
import com.Laborex.Application.KPI.DTO.CA.CaParArticleDTO;
import com.Laborex.Application.KPI.DTO.CA.CaParClientDTO;
import com.Laborex.Application.KPI.DTO.CA.CaParLaboDTO;
import com.Laborex.Application.KPI.DTO.CA.CaParPeriodeDTO;
import com.Laborex.Application.Service.KPI.DuckDBSyncService;
import com.Laborex.Application.Service.KPI.KpiService;



@RestController
@RequestMapping("/api/kpi")
public class KpiController {

	private final KpiService kpiService;
    private final DuckDBSyncService duckDBSyncService; // Ajouter cette dépendance

    public KpiController(KpiService kpiService,DuckDBSyncService duckDBSyncService) {
        this.kpiService= kpiService;
        this.duckDBSyncService = duckDBSyncService;
    }

   //Pour les CA de chaque dimensions
    @GetMapping("/ca-par-client")
    public ResponseEntity<List<CaParClientDTO>> getCaParClient() {
        return ResponseEntity.ok(kpiService.getCaParClient());
    }

    @GetMapping("/ca-par-article")
    public ResponseEntity<List<CaParArticleDTO>> getCaParArticle() {
        return ResponseEntity.ok(kpiService.getCaParArticle());
    }

    @GetMapping("/ca-par-labo")
    public ResponseEntity<List<CaParLaboDTO>> getCaParLabo() {
        return ResponseEntity.ok(kpiService.getCaParLabo());
    }

    @GetMapping("/ca-par-periode")
    public ResponseEntity<List<CaParPeriodeDTO>> getCaParPeriode() {
        return ResponseEntity.ok(kpiService.getCaParPeriode());
    }
    @GetMapping("/interpretation/CAperiode")
    public ResponseEntity<String> getCaInterpretationParPeriode() {
        return ResponseEntity.ok(kpiService.getInterpretationCaParPeriode());
    }
    @GetMapping("/interpretation/CAClient")
    public ResponseEntity<String> getCaInterpretationParClient() {
        return ResponseEntity.ok(kpiService.getInterpretationCaParClient());
    }
    @GetMapping("/interpretation/CAArticle")
    public ResponseEntity<String> getCaInterpretationParArticle() {
        return ResponseEntity.ok(kpiService.getInterpretationCaParArticle());
    }
    @GetMapping("/interpretation/CALabo")
    public ResponseEntity<String> getCaInterpretationParLabo() {
        return ResponseEntity.ok(kpiService.getInterpretationCaParLabo());
    }
    
    //CA GLOBAL
    @GetMapping("/dashboard")
    public ResponseEntity<CaVenteDTO> getCaDashboard() {
        try {
        	CaVenteDTO dashboardData = kpiService.getCaDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            // Gestion d'erreur
            return ResponseEntity.status(500).body(null);
        }
    }
    //ROTATION 
    @GetMapping("/rotation-article")
    public ResponseEntity<List<FrequenceVenteDTO>> getFrequenceDeVenteParArticle() {
        try {
            List<FrequenceVenteDTO> result = kpiService.getFrequenceDeVenteParArticle();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Gérer les erreurs et renvoyer une réponse appropriée
            return ResponseEntity.status(500).body(null);
        }
    }
    
    //PERFORMANCE CLIENT
    @GetMapping("/performance-client")
    public ResponseEntity<List<PerformanceClientDTO>> getPerformanceClients() {
        try {
            List<PerformanceClientDTO> result = kpiService.getPerformanceClients();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Gérer les erreurs
            return ResponseEntity.status(500).body(null);
        }
    }
    
    ///PERFORMANCE LABO
    @GetMapping("/performance-labo")
    public ResponseEntity<List<PerformanceLaboDTO>> getPerformanceLabo() {
        try {
            List<PerformanceLaboDTO> result = kpiService.getPerformanceLabo();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Gérer les erreurs
            return ResponseEntity.status(500).body(null);
        }
    }
    
    ///SAISONALITE DES VENTES
    @GetMapping("/tendance-ventes")
    public ResponseEntity<List<SaisonaliteDTO>> getTendanceVentes() {
        try {
            List<SaisonaliteDTO> result = kpiService.getTendancesMesuelles();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Gérer les erreurs
            return ResponseEntity.status(500).body(null);
        }
    }
    
    //IMPACT PROMOTION
    @GetMapping("/impact-promotions")
    public ResponseEntity<List<ImpactPromotionDTO>> getImpactPromotions() {
        try {
            List<ImpactPromotionDTO> result = kpiService.getImpactPromotions();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    
    
    // Endpoint pour forcer la synchronisation
    @PostMapping("/sync-force")
    public ResponseEntity<?> forcerSynchronisation() {
        try {
            duckDBSyncService.forcerResynchronisation();
            return ResponseEntity.ok(Map.of("message", "Synchronisation forcée terminée"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    // Endpoint de debug pour vérifier le contenu des tables
    @GetMapping("/debug-tables")
    public ResponseEntity<?> debugTables() {
        Map<String, Object> debug = new HashMap<>();
        
        try (Connection conn = DuckDBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            String[] tables = {"CLIENT", "VENTE", "ARTICLE", "DATE_PERSO", "PROMOTION"};
            
            for (String table : tables) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + table)) {
                    if (rs.next()) {
                        debug.put(table.toLowerCase() + "_count", rs.getInt("count"));
                    }
                }
            }
            
            // Test de la requête KPI directement
            try (ResultSet rs = stmt.executeQuery("""
                SELECT c.NomCli, SUM(v.Montant_Vente) AS CA
                FROM VENTE v
                JOIN CLIENT c ON v.CodeClient = c.CodeCli
                GROUP BY c.NomCli
                ORDER BY CA DESC
                LIMIT 5
                """)) {
                
                List<Map<String, Object>> testResults = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("client", rs.getString("NomCli"));
                    row.put("ca", rs.getDouble("CA"));
                    testResults.add(row);
                }
                debug.put("test_kpi_results", testResults);
            }
            
        } catch (SQLException e) {
            debug.put("sql_error", e.getMessage());
        }
        
        return ResponseEntity.ok(debug);
    }
    
   
    
}