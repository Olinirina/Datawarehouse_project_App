package com.Laborex.Application.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Laborex.Application.Model.Tva;

@Repository
public interface TvaRepository extends JpaRepository<Tva, String> {

}
