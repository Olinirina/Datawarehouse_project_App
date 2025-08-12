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

	    /**
	     * Insère une liste d'entités en utilisant le mode batch.
	     * @param entities La liste des entités à insérer.
	     */
	    @Transactional
	    public <T> void insertInBatch(List<T> entities) {
	        if (entities == null || entities.isEmpty()) {
	            return;
	        }

	        int batchSize = 50000;
	        for (int i = 0; i < entities.size(); i++) {
	            entityManager.persist(entities.get(i));
	            if (i > 0 && i % batchSize == 0) {
	                entityManager.flush();
	                entityManager.clear();
	            }
	        }
	        entityManager.flush();
	        entityManager.clear();
	    }

}
