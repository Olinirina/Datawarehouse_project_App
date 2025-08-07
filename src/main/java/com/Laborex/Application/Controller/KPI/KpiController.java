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
import com.Laborex.Application.KPI.DTO.CaClientDTO;
import com.Laborex.Application.Service.KPI.DuckDBSyncService;
import com.Laborex.Application.Service.KPI.KpiService;

@RestController
@RequestMapping("/api/kpi")
public class KpiController {

    private final KpiService kpiService;
    private final DuckDBSyncService duckDBSyncService; // Ajouter cette dépendance

    public KpiController(KpiService kpiService, DuckDBSyncService duckDBSyncService) {
        this.kpiService = kpiService;
        this.duckDBSyncService = duckDBSyncService;
    }

    @GetMapping("/ca-client")
    public ResponseEntity<?> getCAParClient() {
        try {
            List<CaClientDTO> result = kpiService.getChiffreAffairesParClient();
            
            if (result.isEmpty()) {
                // Si aucun résultat, vérifier le contenu des tables
                Map<String, Object> debug = new HashMap<>();
                debug.put("message", "Aucun résultat trouvé");
                debug.put("suggestion", "Vérifiez que les données sont synchronisées dans DuckDB");
                debug.put("debug_endpoint", "/api/kpi/debug-tables");
                debug.put("sync_endpoint", "/api/kpi/sync-force");
                return ResponseEntity.ok(debug);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(error);
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
    
    // Endpoint pour tester la requête KPI avec plus de debug
    @GetMapping("/ca-client-debug")
    public ResponseEntity<?> getCAParClientDebug() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection conn = DuckDBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Compter les ventes et clients
            ResultSet rsVentes = stmt.executeQuery("SELECT COUNT(*) FROM VENTE");
            rsVentes.next();
            response.put("total_ventes", rsVentes.getInt(1));
            
            ResultSet rsClients = stmt.executeQuery("SELECT COUNT(*) FROM CLIENT");
            rsClients.next();
            response.put("total_clients", rsClients.getInt(1));
            
            // Tester la jointure
            ResultSet rsJoin = stmt.executeQuery("""
                SELECT COUNT(*) 
                FROM VENTE v 
                JOIN CLIENT c ON v.CodeClient = c.CodeCli
                """);
            rsJoin.next();
            response.put("ventes_avec_client", rsJoin.getInt(1));
            
            // Les résultats finaux
            List<CaClientDTO> results = kpiService.getChiffreAffairesParClient();
            response.put("resultats", results);
            response.put("nombre_resultats", results.size());
            
        } catch (SQLException e) {
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}