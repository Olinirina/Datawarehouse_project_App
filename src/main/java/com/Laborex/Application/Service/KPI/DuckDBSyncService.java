package com.Laborex.Application.Service.KPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.type.classreading.ConcurrentReferenceCachingMetadataReaderFactory;
import org.springframework.stereotype.Service;

import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.Model.Article;
import com.Laborex.Application.Model.Client;
import com.Laborex.Application.Model.DatePerso;
import com.Laborex.Application.Model.Promotion;
import com.Laborex.Application.Model.Vente;
import com.Laborex.Application.Repository.ArticleRepository;
import com.Laborex.Application.Repository.ClientRepository;
import com.Laborex.Application.Repository.ComparaisonRepository;
import com.Laborex.Application.Repository.ConcurrentRepository;
import com.Laborex.Application.Repository.DateRepository;
import com.Laborex.Application.Repository.LaboRepository;
import com.Laborex.Application.Repository.PromotionRepository;
import com.Laborex.Application.Repository.StockRepository;
import com.Laborex.Application.Repository.TvaRepository;
import com.Laborex.Application.Repository.VenteRepository;

import jakarta.annotation.PostConstruct;

@Service
public class DuckDBSyncService {
    
    @Autowired
    private VenteRepository venteRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private DateRepository dateRepository;
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private TvaRepository tvaRepository;
    @Autowired
    private LaboRepository laboRepository;
    @Autowired
    private ConcurrentRepository concurrentRepository;
    @Autowired 
    private ComparaisonRepository comparaisonRepository;
    //Executer automatiquement une seule fois
    @PostConstruct
    public void synchroniserToutesLesDonnees() {
        System.out.println("Début de la synchronisation DuckDB...");
        try {
            // D'abord, recréer les tables proprement
            recreerTables();
            
            // Puis synchroniser les données
            synchroniserClients();
            synchroniserArticles();
            synchroniserDates();
            synchroniserPromotions();
            synchroniserVentes();
            
            System.out.println("Synchronisation DuckDB terminée avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur lors de la synchronisation : " + e.getMessage());
            e.printStackTrace();
        }
    }
    /*	Partir d'une base propres
    	Creer les tables avec des schemas definies comme dans la base PostgreSQL*/
    private void recreerTables() throws SQLException {
        try (Connection conn = DuckDBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Supprimer toutes les tables dans l'ordre
            System.out.println("Suppression des anciennes tables...");
            stmt.execute("DROP TABLE IF EXISTS VENTE");
            stmt.execute("DROP TABLE IF EXISTS PROMOTION");
            stmt.execute("DROP TABLE IF EXISTS DATE_PERSO");
            stmt.execute("DROP TABLE IF EXISTS ARTICLE");
            stmt.execute("DROP TABLE IF EXISTS CLIENT");
            
            // Recréer les tables avec les bonnes contraintes
            System.out.println("Création des nouvelles tables...");
            
            stmt.execute("""
                CREATE TABLE CLIENT (
                    CodeCli VARCHAR PRIMARY KEY,
                    NomCli VARCHAR
                )
                """);
            
            stmt.execute("""
                CREATE TABLE ARTICLE (
                    CodeArticle VARCHAR PRIMARY KEY,
                    LibArticle VARCHAR,
                    CodeLabo VARCHAR,
                    CodeTva VARCHAR
                )
                """);
            
            stmt.execute("""
                CREATE TABLE DATE_PERSO (
                    CodeDate VARCHAR PRIMARY KEY,
                    DateValue VARCHAR,
                    Jour INT,
                    Mois INT,
                    Annee INT
                )
                """);
            
            stmt.execute("""
                CREATE TABLE PROMOTION (
                    CodePromo VARCHAR PRIMARY KEY,
                    NomPromo VARCHAR,
                    TypePromo VARCHAR,
                    UgLivre INT
                )
                """);
            
            stmt.execute("""
                CREATE TABLE VENTE (
                    CodeVente VARCHAR PRIMARY KEY,
                    Quantite_Vendu INT,
                    Montant_Vente DOUBLE,
                    CodeClient VARCHAR,
                    CodeArticle VARCHAR,
                    CodeDate VARCHAR,
                    CodePromo VARCHAR
                )
                """);
            
            System.out.println("Tables créées avec succès");
        }
    }
    /** Debut methode de synchronisation***/
    public void synchroniserClients() throws SQLException {
    	//Recuperer tous les donnees dans notre base postgresql
        List<Client> clients = clientRepository.findAll();
        if (clients.isEmpty()) {
            System.out.println("Aucun client trouvé dans PostgreSQL");
            return;
        }
        
        // Utiliser une seule requête avec VALUES multiples
        StringBuilder sql = new StringBuilder("INSERT INTO CLIENT (CodeCli, NomCli) VALUES ");
        
        try (Connection conn = DuckDBConnection.getConnection()) {
            
            // Traiter par lots de 1000
            int batchSize = 1000;
            int totalProcessed = 0;
            
            for (int i = 0; i < clients.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, clients.size());
                List<Client> batch = clients.subList(i, endIndex);
                
                // Construire la requête VALUES
                //Inserer les lots en une seule requete
                StringBuilder batchSql = new StringBuilder("INSERT INTO CLIENT (CodeCli, NomCli) VALUES ");
                for (int j = 0; j < batch.size(); j++) {
                    if (j > 0) batchSql.append(", ");
                    batchSql.append("(?, ?)");
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(batchSql.toString())) {
                    int paramIndex = 1;
                    for (Client c : batch) {
                        stmt.setString(paramIndex++, c.getCodeCli());
                        stmt.setString(paramIndex++, c.getNomCli());
                    }
                    stmt.executeUpdate();
                    totalProcessed += batch.size();
                }
            }
            
            System.out.println("Synchronisé " + totalProcessed + " clients");
        }
    }
    
    public void synchroniserArticles() throws SQLException {
        List<Article> articles = articleRepository.findAll();
        if (articles.isEmpty()) {
            System.out.println("Aucun article trouvé dans PostgreSQL");
            return;
        }
        
        try (Connection conn = DuckDBConnection.getConnection()) {
            
            int batchSize = 1000;
            int totalProcessed = 0;
            
            for (int i = 0; i < articles.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, articles.size());
                List<Article> batch = articles.subList(i, endIndex);
                
                StringBuilder batchSql = new StringBuilder("INSERT INTO ARTICLE (CodeArticle, LibArticle, CodeLabo, CodeTva) VALUES ");
                for (int j = 0; j < batch.size(); j++) {
                    if (j > 0) batchSql.append(", ");
                    batchSql.append("(?, ?, ?, ?)");
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(batchSql.toString())) {
                    int paramIndex = 1;
                    for (Article a : batch) {
                        stmt.setString(paramIndex++, a.getCodeArticle());
                        stmt.setString(paramIndex++, a.getLibelle());
                        //Gestion des valeurs nulles
                        stmt.setString(paramIndex++, a.getLabo() != null ? a.getLabo().getCodeLabo() : null);
                        stmt.setString(paramIndex++, a.getTva() != null ? a.getTva().getCodeTva() : null);
                    }
                    stmt.executeUpdate();
                    totalProcessed += batch.size();
                }
            }
            
            System.out.println("Synchronisé " + totalProcessed + " articles");
        }
    }
    
    public void synchroniserDates() throws SQLException {
        List<DatePerso> dates = dateRepository.findAll();
        if (dates.isEmpty()) {
            System.out.println("Aucune date trouvée dans PostgreSQL");
            return;
        }
        
        try (Connection conn = DuckDBConnection.getConnection()) {
            
            int batchSize = 1000;
            int totalProcessed = 0;
            
            for (int i = 0; i < dates.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, dates.size());
                List<DatePerso> batch = dates.subList(i, endIndex);
                
                StringBuilder batchSql = new StringBuilder("INSERT INTO DATE_PERSO (CodeDate, DateValue, Jour, Mois, Annee) VALUES ");
                for (int j = 0; j < batch.size(); j++) {
                    if (j > 0) batchSql.append(", ");
                    batchSql.append("(?, ?, ?, ?, ?)");
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(batchSql.toString())) {
                    int paramIndex = 1;
                    for (DatePerso d : batch) {
                        stmt.setString(paramIndex++, d.getCodeDate());
                        // Convertir la date en string au format ISO
                        stmt.setString(paramIndex++, d.getDate() != null ? d.getDate().toString() : null);
                        stmt.setInt(paramIndex++, d.getJour());
                        stmt.setInt(paramIndex++, d.getMois());
                        stmt.setInt(paramIndex++, d.getAnnee());
                    }
                    stmt.executeUpdate();
                    totalProcessed += batch.size();
                }
            }
            
            System.out.println("Synchronisé " + totalProcessed + " dates");
        }
    }
    
    public void synchroniserPromotions() throws SQLException {
        List<Promotion> promotions = promotionRepository.findAll();
        if (promotions.isEmpty()) {
            System.out.println("Aucune promotion trouvée dans PostgreSQL");
            return;
        }
        
        try (Connection conn = DuckDBConnection.getConnection()) {
            
            int batchSize = 1000;
            int totalProcessed = 0;
            
            for (int i = 0; i < promotions.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, promotions.size());
                List<Promotion> batch = promotions.subList(i, endIndex);
                
                StringBuilder batchSql = new StringBuilder("INSERT INTO PROMOTION (CodePromo, NomPromo, TypePromo, UgLivre) VALUES ");
                for (int j = 0; j < batch.size(); j++) {
                    if (j > 0) batchSql.append(", ");
                    batchSql.append("(?, ?, ?, ?)");
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(batchSql.toString())) {
                    int paramIndex = 1;
                    for (Promotion p : batch) {
                        stmt.setString(paramIndex++, p.getCodePromo());
                        stmt.setString(paramIndex++, p.getNomPromo());
                        stmt.setString(paramIndex++, p.getTypePromo());
                        stmt.setInt(paramIndex++, p.getUgLivre());
                    }
                    stmt.executeUpdate();
                    totalProcessed += batch.size();
                }
            }
            
            System.out.println("Synchronisé " + totalProcessed + " promotions");
        }
    }
    
    public void synchroniserVentes() throws SQLException {
        List<Vente> ventes = venteRepository.findAll();
        if (ventes.isEmpty()) {
            System.out.println("⚠️ Aucune vente trouvée dans PostgreSQL");
            return;
        }
        
        try (Connection conn = DuckDBConnection.getConnection()) {
            
            int batchSize = 1000;
            int totalProcessed = 0;
            
            for (int i = 0; i < ventes.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, ventes.size());
                List<Vente> batch = ventes.subList(i, endIndex);
                
                StringBuilder batchSql = new StringBuilder("INSERT INTO VENTE (CodeVente, Quantite_Vendu, Montant_Vente, CodeClient, CodeArticle, CodeDate, CodePromo) VALUES ");
                for (int j = 0; j < batch.size(); j++) {
                    if (j > 0) batchSql.append(", ");
                    batchSql.append("(?, ?, ?, ?, ?, ?, ?)");
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(batchSql.toString())) {
                    int paramIndex = 1;
                    for (Vente v : batch) {
                        try {
                            stmt.setString(paramIndex++, v.getCodeVente());
                            stmt.setInt(paramIndex++, v.getQuantiteVendu());
                            stmt.setDouble(paramIndex++, v.getMontantVente());
                            stmt.setString(paramIndex++, v.getClient() != null ? v.getClient().getCodeCli() : null);
                            stmt.setString(paramIndex++, v.getArticle() != null ? v.getArticle().getCodeArticle() : null);
                            stmt.setString(paramIndex++, v.getDate() != null ? v.getDate().getCodeDate() : null);
                            stmt.setString(paramIndex++, v.getPromotion() != null ? v.getPromotion().getCodePromo() : null);
                        } catch (Exception e) {
                            System.err.println("⚠️ Erreur avec la vente " + v.getCodeVente() + ": " + e.getMessage());
                            // Continuer avec des valeurs par défaut
                            stmt.setString(paramIndex++, v.getCodeVente());
                            stmt.setInt(paramIndex++, 0);
                            stmt.setDouble(paramIndex++, 0.0);
                            stmt.setString(paramIndex++, null);
                            stmt.setString(paramIndex++, null);
                            stmt.setString(paramIndex++, null);
                            stmt.setString(paramIndex++, null);
                        }
                    }
                    
                    stmt.executeUpdate();
                    totalProcessed += batch.size();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de l'insertion du batch: " + e.getMessage());
                    // Essayer les insertions une par une pour ce batch
                    for (Vente v : batch) {
                        try {
                            String singleSql = "INSERT INTO VENTE (CodeVente, Quantite_Vendu, Montant_Vente, CodeClient, CodeArticle, CodeDate, CodePromo) VALUES (?, ?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement singleStmt = conn.prepareStatement(singleSql)) {
                                singleStmt.setString(1, v.getCodeVente());
                                singleStmt.setInt(2, v.getQuantiteVendu());
                                singleStmt.setDouble(3, v.getMontantVente());
                                singleStmt.setString(4, v.getClient() != null ? v.getClient().getCodeCli() : null);
                                singleStmt.setString(5, v.getArticle() != null ? v.getArticle().getCodeArticle() : null);
                                singleStmt.setString(6, v.getDate() != null ? v.getDate().getCodeDate() : null);
                                singleStmt.setString(7, v.getPromotion() != null ? v.getPromotion().getCodePromo() : null);
                                singleStmt.executeUpdate();
                                totalProcessed++;
                            }
                        } catch (SQLException se) {
                            System.err.println("Impossible d'insérer la vente " + v.getCodeVente() + ": " + se.getMessage());
                        }
                    }
                }
            }
            
            System.out.println("Synchronisé " + totalProcessed + " ventes sur " + ventes.size());
        }
    }
    /** Fin methode de synchronisation***/
    
    //Lancer manuellement la synchronisation
    public void forcerResynchronisation() {
        System.out.println("Resynchronisation forcée demandée...");
        synchroniserToutesLesDonnees();
    }
    
    // Méthode de debogage pour vérifier le contenu
    public Map<String, Integer> getStatistiques() {
        Map<String, Integer> stats = new HashMap<>();
        
        try (Connection conn = DuckDBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            String[] tables = {"CLIENT", "ARTICLE", "DATE_PERSO", "PROMOTION", "VENTE"};
            
            for (String table : tables) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    if (rs.next()) {
                        stats.put(table.toLowerCase(), rs.getInt(1));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul des statistiques: " + e.getMessage());
        }
        
        return stats;
    }
}