package com.Laborex.Application.Dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Laborex.Application.Model.Vente;

import java.util.List;

@Service
public class VenteBatchService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void insertInBatch(List<Vente> ventes) {
        int batchSize = 500;
        for (int i = 0; i < ventes.size(); i++) {
            entityManager.persist(ventes.get(i));
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush(); // pour les derniers éléments
        entityManager.clear();
    }

}
