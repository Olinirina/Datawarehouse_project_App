package com.Laborex.Application.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Laborex.Application.Model.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String>{

}
