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
import com.Laborex.Application.Model.Comparaison;
import com.Laborex.Application.Model.Concurrent;
import com.Laborex.Application.Model.DatePerso;
import com.Laborex.Application.Model.Labo;
import com.Laborex.Application.Model.Promotion;
import com.Laborex.Application.Model.Stock;
import com.Laborex.Application.Model.Tva;
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
            synchroniserConcurrent();
            synchroniserLabo();
            synchroniserTva();
            synchroniserArticles();
            synchroniserDates();
            synchroniserPromotions();
            synchroniserStock();
            synchroniserVentes();
            synchroniserComparaison();
            
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
            stmt.execute("DROP TABLE IF EXISTS CONCURRENT");
            stmt.execute("DROP TABLE IF EXISTS LABO");
            stmt.execute("DROP TABLE IF EXISTS CLIENT");
            stmt.execute("DROP TABLE IF EXISTS TVA");
            stmt.execute("DROP TABLE IF EXISTS STOCK");
            stmt.execute("DROP TABLE IF EXISTS COMPARAISON");
            
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
            	        PrixVente DOUBLE,
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
            			idVente BIGINT PRIMARY KEY,
            	        CodeVente VARCHAR,
            	        Quantite_Vendu INT,
            	        Montant_Vente DOUBLE,
            	        CodeClient VARCHAR,
            	        CodeArticle VARCHAR,
            	        CodeDate VARCHAR,
            	        CodePromo VARCHAR
            	    )
            	    """);

            	stmt.execute("""
            	    CREATE TABLE STOCK (
            	        CodeStock BIGINT PRIMARY KEY,
            	        QuantiteStocke INT,
            	        CoursRoute INT,
            	        CodeArticle VARCHAR
            	    )
            	    """);

            	stmt.execute("""
            	    CREATE TABLE COMPARAISON (
            	        CodeComparaison BIGINT PRIMARY KEY,
            	        PrixConcurrent DOUBLE,
            	        Variation DOUBLE,
            	        nouveauPrix DOUBLE,
            	        CodeConcurrent VARCHAR,
            	        CodeArticle VARCHAR
            	    )
            	    """);

            	stmt.execute("""
            	    CREATE TABLE CONCURRENT (
            	        CodeConcurrent VARCHAR PRIMARY KEY,
            	        NomConcurrent VARCHAR
            	    )
            	    """);

            	stmt.execute("""
            	    CREATE TABLE LABO (
            	        CodeLabo VARCHAR PRIMARY KEY,
            	        NomLabo VARCHAR
            	    )
            	    """);

            	stmt.execute("""
            	    CREATE TABLE TVA (
            	        CodeTva VARCHAR PRIMARY KEY,
            	        Taux DOUBLE,
            	        Nature VARCHAR
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
            System.out.println("Aucune vente trouvée dans PostgreSQL");
            return;
        }
        
        try (Connection conn = DuckDBConnection.getConnection()) {
            
            int batchSize = 1000;
            int totalProcessed = 0;
            
            for (int i = 0; i < ventes.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, ventes.size());
                List<Vente> batch = ventes.subList(i, endIndex);
                
                StringBuilder batchSql = new StringBuilder("INSERT INTO VENTE (idVente,CodeVente, Quantite_Vendu, Montant_Vente, CodeClient, CodeArticle, CodeDate, CodePromo) VALUES ");
                for (int j = 0; j < batch.size(); j++) {
                    if (j > 0) batchSql.append(", ");
                    batchSql.append("(?,?, ?, ?, ?, ?, ?, ?)");
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(batchSql.toString())) {
                    int paramIndex = 1;
                    for (Vente v : batch) {
                        try {
                        	stmt.setLong(paramIndex++, v.getIdVente());
                            stmt.setString(paramIndex++, v.getCodeVente());
                            stmt.setInt(paramIndex++, v.getQuantiteVendu());
                            stmt.setDouble(paramIndex++, v.getMontantVente());
                            stmt.setString(paramIndex++, v.getClient() != null ? v.getClient().getCodeCli() : null);
                            stmt.setString(paramIndex++, v.getArticle() != null ? v.getArticle().getCodeArticle() : null);
                            stmt.setString(paramIndex++, v.getDate() != null ? v.getDate().getCodeDate() : null);
                            stmt.setString(paramIndex++, v.getPromotion() != null ? v.getPromotion().getCodePromo() : null);
                        } catch (Exception e) {
                            System.err.println("⚠️ Erreur avec la vente " + v.getCodeVente() + ": " + e.getMessage());
                            
                        }
                    }
                    
                    stmt.executeUpdate();
                    totalProcessed += batch.size();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de l'insertion du batch: " + e.getMessage());                   
                }
            }
            
            System.out.println("Synchronisé " + totalProcessed + " ventes sur " + ventes.size());
        }
    }
    
    //Synchronisation stock
    public void synchroniserStock() throws SQLException{
    	List<Stock> stocks= stockRepository.findAll();
    	if(stocks.isEmpty()) {
    		System.out.println("Aucune stock trouvée dans la PostgreSQL");
    		return;
    	}
    	try(Connection conn = DuckDBConnection.getConnection()){
    		int batchSize = 1000;
    		int totalProcessed= 0;
    		
    		for(int i=0; i<stocks.size();i += batchSize) {
    			int endIndex= Math.min(i + batchSize,  stocks.size());
    			List<Stock> batch= stocks.subList(i, endIndex);
    			
    			StringBuilder batchSql= new StringBuilder("INSERT INTO STOCK (CodeStock,QuantiteStocke,CoursRoute,CodeArticle) VALUES ");
    			for (int j =0; j < batch.size(); j++) {
    				if(j >0) batchSql.append(", ");
    				batchSql.append("(?, ?, ?, ?)");
    			}
    			try (PreparedStatement stmt = conn.prepareStatement(batchSql.toString())){
    				int paramIndex= 1;
    				for(Stock s : batch) {
    					stmt.setLong(paramIndex++, s.getCodeStock());
    					stmt.setInt(paramIndex++, s.getQuantiteStocke());
    					stmt.setInt(paramIndex++, s.getCoursRoute());
    					stmt.setString(paramIndex++, s.getArticle() != null ? s.getArticle().getCodeArticle() : null);
    				}
    				stmt.executeUpdate();
    				totalProcessed += batch.size();
    			}
    		}
    		System.out.println("Synchronisé " + totalProcessed + " stocks");  	
    	}
    }
    //Synchronisation Concurrent
    public void synchroniserConcurrent() throws SQLException{
    	List<Concurrent> concurrents= concurrentRepository.findAll();
    	if(concurrents.isEmpty()) {
    		System.out.println("Aucune concurrent trouvée dans la PostgreSQL");
    		return;
    	}
    	try(Connection conn = DuckDBConnection.getConnection()){
    		int batchSize = 1000;
    		int totalProcessed= 0;
    		
    		for(int i=0; i<concurrents.size();i += batchSize) {
    			int endIndex= Math.min(i + batchSize,  concurrents.size());
    			List<Concurrent> batch= concurrents.subList(i, endIndex);
    			
    			StringBuilder batchSql= new StringBuilder("INSERT INTO CONCURRENT (CodeConcurrent, NomConcurrent) VALUES ");
    			for (int j =0; j < batch.size(); j++) {
    				if(j >0) batchSql.append(", ");
    				batchSql.append("(?, ?)");
    			}
    			try (PreparedStatement stmt = conn.prepareStatement(batchSql.toString())){
    				int paramIndex= 1;
    				for(Concurrent c : batch) {
    					stmt.setString(paramIndex++, c.getCodeConcurrent());
    					stmt.setString(paramIndex++, c.getNomConcurrent());
    				}
    				stmt.executeUpdate();
    				totalProcessed += batch.size();
    			}
    		}
    		System.out.println("Synchronisé" + totalProcessed + "concurrents");    	
    	}
    }
    
  //Synchronisation Labo
    public void synchroniserLabo() throws SQLException{
    	List<Labo> labos= laboRepository.findAll();
    	if(labos.isEmpty()) {
    		System.out.println("Aucun labo trouvé dans la PostgreSQL");
    		return;
    	}
    	try(Connection conn = DuckDBConnection.getConnection()){
    		int batchSize = 1000;
    		int totalProcessed= 0;
    		
    		for(int i=0; i<labos.size();i += batchSize) {
    			int endIndex= Math.min(i + batchSize,  labos.size());
    			List<Labo> batch= labos.subList(i, endIndex);
    			
    			StringBuilder batchSql= new StringBuilder("INSERT INTO LABO (CodeLabo, NomLabo) VALUES ");
    			for (int j =0; j < batch.size(); j++) {
    				if(j >0) batchSql.append(", ");
    				batchSql.append("(?, ?)");
    			}
    			try (PreparedStatement stmt = conn.prepareStatement(batchSql.toString())){
    				int paramIndex= 1;
    				for(Labo l : batch) {
    					stmt.setString(paramIndex++, l.getCodeLabo());
    					stmt.setString(paramIndex++, l.getNomLabo());
    				}
    				stmt.executeUpdate();
    				totalProcessed += batch.size();
    			}
    		}
    		System.out.println("Synchronisé " + totalProcessed + " labos");    	
    	}
    }
    
  //Synchronisation TVA
    public void synchroniserTva() throws SQLException{
    	List<Tva> tvas= tvaRepository.findAll();
    	if(tvas.isEmpty()) {
    		System.out.println("Aucune TVA trouvée dans la PostgreSQL");
    		return;
    	}
    	try(Connection conn = DuckDBConnection.getConnection()){
    		int batchSize = 1000;
    		int totalProcessed= 0;
    		
    		for(int i=0; i<tvas.size();i += batchSize) {
    			int endIndex= Math.min(i + batchSize,  tvas.size());
    			List<Tva> batch= tvas.subList(i, endIndex);
    			
    			StringBuilder batchSql= new StringBuilder("INSERT INTO TVA (CodeTva, Taux, Nature) VALUES ");
    			for (int j =0; j < batch.size(); j++) {
    				if(j >0) batchSql.append(", ");
    				batchSql.append("(?, ?,?)");
    			}
    			try (PreparedStatement stmt = conn.prepareStatement(batchSql.toString())){
    				int paramIndex= 1;
    				for(Tva t : batch) {
    					stmt.setString(paramIndex++, t.getCodeTva());
    					stmt.setDouble(paramIndex++, t.getTaux());
    					stmt.setString(paramIndex++, t.getNature());
    				}
    				stmt.executeUpdate();
    				totalProcessed += batch.size();
    			}
    		}
    		System.out.println("Synchronisé " + totalProcessed + " tvas");   	
    	}
    }
    
  //Synchronisation Comparaison
 // Code corrigé
    public void synchroniserComparaison() throws SQLException {
        List<Comparaison> compars = comparaisonRepository.findAll();
        if (compars.isEmpty()) {
            System.out.println("Aucune comparaison trouvée dans la PostgreSQL");
            return;
        }
        try (Connection conn = DuckDBConnection.getConnection()) {
            int batchSize = 1000;
            int totalProcessed = 0;

            for (int i = 0; i < compars.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, compars.size());
                List<Comparaison> batch = compars.subList(i, endIndex);

                StringBuilder batchSql = new StringBuilder("INSERT INTO COMPARAISON (CodeComparaison, PrixConcurrent, Variation, NouveauPrix, CodeConcurrent, CodeArticle) VALUES ");
                for (int j = 0; j < batch.size(); j++) {
                    if (j > 0) batchSql.append(", ");
                    batchSql.append("(?, ?, ?, ?, ?, ?)");
                }

                try (PreparedStatement stmt = conn.prepareStatement(batchSql.toString())) {
                    int paramIndex = 1;
                    for (Comparaison c : batch) {
                        stmt.setLong(paramIndex++, c.getCodeComparaison());
                        stmt.setDouble(paramIndex++, c.getPrixConcurrent());
                        stmt.setDouble(paramIndex++, c.getVariation() != null ? c.getVariation() : 0.0);
                        stmt.setDouble(paramIndex++, c.getNouveauPrix() !=null ? c.getNouveauPrix() : 0);
                        stmt.setString(paramIndex++, c.getConcurrent() != null ? c.getConcurrent().getCodeConcurrent() : null);
                        stmt.setString(paramIndex++, c.getArticle() != null ? c.getArticle().getCodeArticle() : null);
                    }
                    stmt.executeUpdate();
                    totalProcessed += batch.size();
                }
            }
            System.out.println("Synchronisé " + totalProcessed + " comparaisons"); // Correction du message
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