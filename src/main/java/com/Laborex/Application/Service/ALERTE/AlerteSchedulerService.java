package com.Laborex.Application.Service.ALERTE;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.Laborex.Application.Model.Alerte.Alerte;
import com.Laborex.Application.Service.ALERTE.Configuration.AlerteConfiguration;

import jakarta.annotation.PostConstruct;

@Service
public class AlerteSchedulerService {
	
	private static final Logger log = LoggerFactory.getLogger(StockCritiqueService.class);

	

    
    
    @Autowired
    private StockCritiqueService stockCritiqueService;
    

    @Autowired
    private ClientInactifService clientInactifService;
    
    @Autowired
    private AnomalieVenteService anomalieVenteService;
    
    @Autowired
    private AlerteService alerteService;
    
    @Autowired
    private AlerteConfiguration alerteConfig;
   /* 
    @PostConstruct
    public void lancementInitial() {
        log.info("=== DÉMARRAGE DE L'APPLICATION : vérification immédiate des alertes ===");

        try {
            if (alerteConfig.isActiverNotifications()) {
                log.info("Détection initiale des stocks critiques au démarrage...");
                stockCritiqueService.detecterStocksCritiques();
                log.info("Détection initiale terminée ✅");
            } else {
                log.warn("Notifications automatiques désactivées, rien exécuté au démarrage");
            }
        } catch (Exception e) {
            log.error("Erreur lors de la détection initiale au démarrage", e);
        }
    }*/
    
    /**
     * Exécute toutes les vérifications d'alertes
     * S'exécute toutes les heures pendant les heures d'ouverture (8h-18h)
     */
    @Scheduled(cron = "0 0 8-18 * * MON-FRI")
    public void executerVerificationsAlertes() {
        if (!alerteConfig.isActiverNotifications()) {
            System.out.println("Les alertes automatiques sont désactivées");
            return;
        }
        
        System.out.println("=== DÉBUT DU CYCLE DE VÉRIFICATION DES ALERTES ===");
        long debut = System.currentTimeMillis();
        
        try {
            // 1. Vérifier les stocks critiques
        	System.out.println("1/4 - Vérification des stocks critiques...");
            stockCritiqueService.detecterStocksCritiques();
            

            
            // 3. Vérifier les clients inactifs (une fois par jour seulement)
            if (LocalTime.now().getHour() == 9) { // À 9h du matin
            	System.out.println("3/4 - Vérification des clients inactifs...");
                clientInactifService.detecterClientsInactifs();
            }
            
            // 4. Vérifier les anomalies de vente (2 fois par jour)
            if (LocalTime.now().getHour() == 10 || LocalTime.now().getHour() == 16) {
            	System.out.println("4/4 - Vérification des anomalies de vente...");
                anomalieVenteService.detecterAnomaliesVente();
            }
            
            long duree = System.currentTimeMillis() - debut;
            System.out.println("=== CYCLE DE VÉRIFICATION TERMINÉ EN {}ms ==="+ duree);
            
        } catch (Exception e) {
        	System.err.println("Erreur lors du cycle de vérification des alertes"+ e);
        }
    }
    
    /**
     * Nettoyage quotidien des anciennes alertes
     * S'exécute tous les jours à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void nettoyageQuotidien() {
    	System.out.println("=== DÉBUT DU NETTOYAGE QUOTIDIEN ===");
        
        try {
            alerteService.nettoyerAlertesObsoletes();
            System.out.println("=== NETTOYAGE QUOTIDIEN TERMINÉ ===");
            
        } catch (Exception e) {
        	System.err.println("Erreur lors du nettoyage quotidien"+ e);
        }
    }
    
    /**
     * Vérification rapide des alertes critiques
     * S'exécute toutes les 15 minutes pendant les heures d'ouverture
     */
    @Scheduled(cron = "0 */15 8-18 * * MON-FRI")
    public void verificationRapide() {
        if (!alerteConfig.isActiverNotifications()) {
            return;
        }
        
        System.out.println("Vérification rapide des stocks critiques");
        
        try {
            // Vérifier seulement les stocks critiques pour les alertes urgentes
            stockCritiqueService.detecterStocksCritiques();
            
        } catch (Exception e) {
        	System.err.println("Erreur lors de la vérification rapide"+ e);
        }
    }
    
    /**
     * Méthode pour exécuter manuellement toutes les vérifications
     * Utile pour les tests ou les exécutions à la demande
     */
    public void executerVerificationManuelle() {
    	System.out.println("=== EXÉCUTION MANUELLE DES VÉRIFICATIONS ===");
        
        try {
            stockCritiqueService.detecterStocksCritiques();
           
            clientInactifService.detecterClientsInactifs();
            anomalieVenteService.detecterAnomaliesVente();
            
            System.out.println("=== EXÉCUTION MANUELLE TERMINÉE ===");
            
        } catch (Exception e) {
        	System.err.println("Erreur lors de l'exécution manuelle"+ e);
        }
    }
    
    /**
     * Récupère un résumé de l'état des alertes
     */
    public Map<String, Object> getResumeAlertes() {
        Map<String, Object> resume = new HashMap<>();
        
        try {
            Map<String, Object> stats = alerteService.getStatistiquesAlertes();
            List<Alerte> alertesRecentes = alerteService.getAlertesActives()
                .stream()
                .limit(10)
                .collect(Collectors.toList());
            
            resume.put("statistiques", stats);
            resume.put("alertesRecentes", alertesRecentes);
            resume.put("derniereMiseAJour", LocalDateTime.now());
            
        } catch (Exception e) {
        	System.err.println("Erreur lors de la récupération du résumé"+ e);
            resume.put("erreur", e.getMessage());
        }
        
        return resume;
    }
}
