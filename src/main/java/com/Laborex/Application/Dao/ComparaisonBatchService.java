package com.Laborex.Application.Dao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ComparaisonBatchService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public <T> void insertInBatch(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        int batchSize = 50000; // Customizable batch size
        for (int i = 0; i < entities.size(); i++) {
            // Use merge() instead of persist() to handle both new and existing entities
            entityManager.merge(entities.get(i)); 
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }
}