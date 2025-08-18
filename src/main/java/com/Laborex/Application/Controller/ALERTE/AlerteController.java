package com.Laborex.Application.Controller.ALERTE;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Laborex.Application.KPI.DTO.CA.CaParClientDTO;
import com.Laborex.Application.Model.Alerte.Alerte;
import com.Laborex.Application.Model.Alerte.AlerteDTO;
import com.Laborex.Application.Model.Alerte.TypeAlerte;
import com.Laborex.Application.Service.ALERTE.AlerteSchedulerService;
import com.Laborex.Application.Service.ALERTE.AlerteService;
import com.Laborex.Application.Service.ALERTE.CalculAlerteService;


@RestController
@RequestMapping("/api/alertes")
@CrossOrigin(origins = "*")
public class AlerteController {
  
    @Autowired
    private AlerteService alerteService;
    @Autowired
    private CalculAlerteService calculAlerte;;
    
    @Autowired
    private AlerteSchedulerService schedulerService;
    
    //STOCK CRITIQUE
    @GetMapping("/critiques")
    public ResponseEntity<List<AlerteDTO>> getStockCritique() {
        return ResponseEntity.ok(calculAlerte.detecterStockCritique());
    }
  //CLIENTS INACTIFS
    @GetMapping("/clientsInactifs")
    public ResponseEntity<List<AlerteDTO>> getClientsInactifs() {
        return ResponseEntity.ok(calculAlerte.detecterClientsInactifs());
    }
  //ANOMALIES DES VENTES
    @GetMapping("/anomalies")
    public ResponseEntity<List<AlerteDTO>> getVentesAnomalies() {
        return ResponseEntity.ok(calculAlerte.detecterAnomaliesVentes());
    }
    
    
    
    /**
     * Récupère toutes les alertes actives
     */
    @GetMapping
    public ResponseEntity<List<Alerte>> getAlertes() {
        try {
            List<Alerte> alertes = alerteService.getAlertesActives();
            return ResponseEntity.ok(alertes);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des alertes" + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Récupère les statistiques des alertes
     */
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        try {
            Map<String, Object> stats = alerteService.getStatistiquesAlertes();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
        	System.err.println("Erreur lors de la récupération des statistiques" + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    
    
    /**
     * Récupère un résumé complet
     */
    @GetMapping("/resume")
    public ResponseEntity<Map<String, Object>> getResume() {
        try {
            Map<String, Object> resume = schedulerService.getResumeAlertes();
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
        	System.err.println("Erreur lors de la récupération du résumé"+ e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Résout une alerte
     */
    @PutMapping("/{id}/resoudre")
    public ResponseEntity<Void> resoudreAlerte(@PathVariable Long id) {
        try {
            alerteService.resoudreAlerte(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
        	System.err.println("Erreur lors de la résolution de l'alerte {}"+ id + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Déclenche une vérification manuelle
     */
    @PostMapping("/verifier")
    public ResponseEntity<Map<String, String>> declencherVerification() {
        try {
            // Exécuter la vérification de manière asynchrone
            CompletableFuture.runAsync(() -> {
                schedulerService.executerVerificationManuelle();
            });
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vérification lancée avec succès");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
        	System.err.println("Erreur lors du déclenchement de la vérification" + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Récupère les alertes par type
     */
    @GetMapping("/type/{typeAlerte}")
    public ResponseEntity<List<Alerte>> getAlertesByType(@PathVariable String typeAlerte) {
        try {
            TypeAlerte type = TypeAlerte.valueOf(typeAlerte.toUpperCase());
            List<Alerte> alertes = alerteService.getAlertesActives()
                .stream()
                .filter(a -> a.getType() == type)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(alertes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
        	System.err.println("Erreur lors de la récupération des alertes par type"+ e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}