package com.Laborex.Application.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Laborex.Application.Model.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {

}
