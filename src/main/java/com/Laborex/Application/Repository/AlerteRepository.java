package com.Laborex.Application.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.Laborex.Application.Model.Alerte.Alerte;
import com.Laborex.Application.Model.Alerte.TypeAlerte;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {
    
    // Trouver les alertes actives par type
    List<Alerte> findByTypeAndActiveTrue(TypeAlerte type);
    
    // Trouver les alertes actives
    List<Alerte> findByActiveTrueOrderByDateCreationDesc();
    
    // Trouver une alerte active par type et référence (éviter les doublons)
    Optional<Alerte> findByTypeAndCodeReferenceAndActiveTrue(TypeAlerte type, String codeReference);
    
    // Compter les alertes par niveau de sévérité
    @Query("SELECT a.severite, COUNT(a) FROM Alerte a WHERE a.active = true GROUP BY a.severite")
    List<Object[]> countAlertesBySeverite();
}