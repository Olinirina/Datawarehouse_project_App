package com.Laborex.Application.Service.ALERTE;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Laborex.Application.Model.Alerte.Alerte;
import com.Laborex.Application.Model.Alerte.NiveauSeverite;
import com.Laborex.Application.Model.Alerte.TypeAlerte;
import com.Laborex.Application.Repository.AlerteRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AlerteService {
    

    
    @Autowired
    private AlerteRepository alerteRepository;
    
    /**
     * Crée une nouvelle alerte ou met à jour une existante
     */
    public void creerOuMettreAJourAlerte(TypeAlerte type, String titre, String description,
                                        String codeReference, Double valeur, NiveauSeverite severite) {
        
        // Vérifier si une alerte similaire existe déjà
        Optional<Alerte> alerteExistante = alerteRepository
            .findByTypeAndCodeReferenceAndActiveTrue(type, codeReference);
        
        if (alerteExistante.isPresent()) {
            // Mettre à jour l'alerte existante si la valeur a changé
            Alerte alerte = alerteExistante.get();
            if (!Objects.equals(alerte.getValeur(), valeur) || !Objects.equals(alerte.getSeverite(), severite)) {
                alerte.setValeur(valeur);
                alerte.setSeverite(severite);
                alerte.setDescription(description);
                alerte.setDateCreation(LocalDateTime.now()); // Actualiser la date
                alerteRepository.save(alerte);               
            }
        } else {
            // Créer une nouvelle alerte
            Alerte nouvelleAlerte = new Alerte(type, titre, description, codeReference, valeur, severite, null, null, null);
            alerteRepository.save(nouvelleAlerte);           
        }
    }
    
    /**
     * Résout une alerte (la marque comme inactive)
     */
    public void resoudreAlerte(Long alerteId) {
        Optional<Alerte> alerteOpt = alerteRepository.findById(alerteId);
        if (alerteOpt.isPresent()) {
            Alerte alerte = alerteOpt.get();
            alerte.setActive(false);
            alerte.setDateResolution(LocalDateTime.now());
            alerteRepository.save(alerte);
            
        }
    }
    
    /**
     * Nettoie les alertes obsolètes (plus de 30 jours)
     */
    public void nettoyerAlertesObsoletes() {
        LocalDateTime dateLimit = LocalDateTime.now().minusDays(30);
        
        // Trouver les alertes à nettoyer
        List<Alerte> alertesObsoletes = alerteRepository.findAll().stream()
            .filter(a -> !a.getActive() && a.getDateResolution() != null && 
                        a.getDateResolution().isBefore(dateLimit))
            .collect(Collectors.toList());
        
        if (!alertesObsoletes.isEmpty()) {
            alerteRepository.deleteAll(alertesObsoletes);
            
        }
    }
    
    /**
     * Récupère toutes les alertes actives
     */
    public List<Alerte> getAlertesActives() {
        return alerteRepository.findByActiveTrueOrderByDateCreationDesc();
    }
    
    /**
     * Récupère les statistiques des alertes
     */
    public Map<String, Object> getStatistiquesAlertes() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Object[]> resultats = alerteRepository.countAlertesBySeverite();
        Map<NiveauSeverite, Long> compteurs = new HashMap<>();
        
        // Initialiser tous les niveaux à 0
        for (NiveauSeverite niveau : NiveauSeverite.values()) {
            compteurs.put(niveau, 0L);
        }
        
        // Remplir avec les vrais compteurs
        for (Object[] resultat : resultats) {
            NiveauSeverite niveau = (NiveauSeverite) resultat[0];
            Long count = (Long) resultat[1];
            compteurs.put(niveau, count);
        }
        
        stats.put("parSeverite", compteurs);
        stats.put("total", compteurs.values().stream().mapToLong(Long::longValue).sum());
        
        return stats;
    }
}
