package com.Laborex.Application.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Laborex.Application.Model.DatePerso;

@Repository
public interface DateRepository extends JpaRepository<DatePerso, String> {

}
