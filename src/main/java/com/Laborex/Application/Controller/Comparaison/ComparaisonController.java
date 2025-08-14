package com.Laborex.Application.Controller.Comparaison;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Laborex.Application.ComparaisonDTO.ComparaisonDTO;
import com.Laborex.Application.ComparaisonDTO.SimulationDTO;
import com.Laborex.Application.Service.Comparaison.PriceAnalysisService;


@RestController
@RequestMapping("/api/comparaisons")
@CrossOrigin(origins = "*")
public class ComparaisonController {

    @Autowired
    private PriceAnalysisService priceAnalysisService;

    /**
     * Endpoint pour obtenir l'analyse complète des prix avec toutes les métriques
     * GET /api/comparaisons/analyse-complete
     */
    @GetMapping("/analyse-complete")
    public ResponseEntity<List<ComparaisonDTO>> obtenirAnalyseComplete() {
        try {
            List<ComparaisonDTO> analyse = priceAnalysisService.calculerAnalyseComplete();
            return ResponseEntity.ok(analyse);
        } catch (Exception e) {
            System.err.println("Erreur dans obtenirAnalyseComplete: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour l'analyse d'un article spécifique avec métriques avancées
     * GET /api/comparaisons/analyse/{codeArticle}
     */
    @GetMapping("/analyse/{codeArticle}")
    public ResponseEntity<List<ComparaisonDTO>> obtenirAnalysePourArticle(@PathVariable String codeArticle) {
        try {
            List<ComparaisonDTO> analyse = priceAnalysisService.calculerAnalysePourArticle(codeArticle);
            
            if (analyse.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(analyse);
        } catch (Exception e) {
            System.err.println("Erreur dans obtenirAnalysePourArticle: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour simuler un nouveau prix et voir l'impact
     * POST /api/comparaisons/simuler/{codeArticle}?nouveauPrix=150.5
     */
    @PostMapping("/simuler/{codeArticle}")
    public ResponseEntity<SimulationDTO> simulerNouveauPrix(
            @PathVariable String codeArticle,
            @RequestParam Double nouveauPrix) {
        try {
            if (nouveauPrix <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            SimulationDTO simulation = priceAnalysisService.simulerNouveauPrix(codeArticle, nouveauPrix);
            return ResponseEntity.ok(simulation);
        } catch (Exception e) {
            System.err.println("Erreur dans simulerNouveauPrix: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    

   
}