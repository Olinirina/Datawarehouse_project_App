package com.Laborex.Application.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Laborex.Application.Model.Client;



@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

}
