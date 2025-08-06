package com.Laborex.Application.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Laborex.Application.Model.Vente;

@Repository
public interface VenteRepository extends JpaRepository<Vente, String> {

}
