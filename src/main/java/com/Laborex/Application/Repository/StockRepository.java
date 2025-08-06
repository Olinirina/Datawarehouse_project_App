package com.Laborex.Application.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Laborex.Application.Model.Article;
import com.Laborex.Application.Model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long>{

	@Query("SELECT s FROM Stock s WHERE s.article = :article")
	Optional<Stock> findByArticle(@Param("article") Article article);

}
