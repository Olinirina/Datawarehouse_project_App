package com.Laborex.Application.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.Laborex.Application.Model.Article;
import com.Laborex.Application.Model.Comparaison;
import com.Laborex.Application.Model.Concurrent;

@Repository
public interface ComparaisonRepository extends JpaRepository<Comparaison, Long> {
	
	Optional<Comparaison> findByArticleAndConcurrent(Article article, Concurrent concurrent);


}
