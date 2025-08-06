package com.Laborex.Application.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Laborex.Application.Model.Comparaison;

@Repository
public interface ComparaisonRepository extends JpaRepository<Comparaison, Long> {

}
