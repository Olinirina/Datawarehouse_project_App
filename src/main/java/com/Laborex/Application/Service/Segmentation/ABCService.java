package com.Laborex.Application.Service.Segmentation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.SegmentationDTO.ClassificationACBDTO;
import com.Laborex.Application.SegmentationDTO.ClientRFMDTO;

@Service
public class ABCService {
	public List<ClassificationACBDTO> getAbcAnalyseParArticle() {
	    List<ClassificationACBDTO> articles = new ArrayList<>();
	    
	    // 1. Exécuter la requête SQL pour obtenir le CA par article
	    String sql = """
	        SELECT
	            a.LibArticle,
	            SUM(v.Montant_Vente) AS ChiffreAffaires
	        FROM VENTE v
	        JOIN ARTICLE a ON v.CodeArticle = a.CodeArticle
	        GROUP BY a.LibArticle
	        ORDER BY ChiffreAffaires DESC
	    """;

	    try (Connection conn = DuckDBConnection.getConnection();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        while (rs.next()) {
	            articles.add(new ClassificationACBDTO(
	                rs.getString("LibArticle"),
	                rs.getDouble("ChiffreAffaires"),
	                null, // La classe sera définie plus tard
	                null  // L'interprétation sera ajoutée plus tard
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return new ArrayList<>();
	    }

	    if (articles.isEmpty()) {
	        return new ArrayList<>();
	    }

	    // 2. Calculer le CA total et appliquer la classification ABC
	    double caTotal = articles.stream().mapToDouble(ClassificationACBDTO::getChiffreAffaires).sum();
	    double caCumule = 0;
	    
	    System.out.println("--- Classification ABC et interprétation des articles ---");
	    
	    for (ClassificationACBDTO item : articles) {
	        caCumule += item.getChiffreAffaires();
	        double pourcentageCumule = (caCumule / caTotal) * 100;
	        String classe;
	        String interpretation;
	        
	        if (pourcentageCumule <= 80) {
	            classe = "A";
	            interpretation = "⭐ Article 'A' : Représente une part significative du chiffre d'affaires (jusqu'à 80%). Ce sont vos produits phares. Gérez leur stock de manière très rigoureuse et surveillez-les de près.";
	        } else if (pourcentageCumule <= 95) {
	            classe = "B";
	            interpretation = "📈 Article 'B' : Contribue de manière intermédiaire au chiffre d'affaires (entre 80% et 95% cumulé). Ces produits ont un potentiel de croissance. Maintenez un niveau de service élevé mais avec une gestion des stocks moins intensive.";
	        } else {
	            classe = "C";
	            interpretation = "📦 Article 'C' : Apporte une faible contribution au chiffre d'affaires (après 95% cumulé). Ces produits nécessitent une gestion simplifiée et moins de ressources. Envisagez un stock de sécurité minimal ou des promotions pour les écouler.";
	        }
	        
	        item.setClasse(classe);
	        item.setInterpretation(interpretation);

	        // Affichage dans la console
	        System.out.println("Article : " + item.getNomElement() +
	                           " | CA : " + String.format("%.2f", item.getChiffreAffaires()) +
	                           " | Classe : " + classe +
	                           " | " + interpretation);
	    }
	    
	    return articles;
	}
	
	//CLASSIFICATION PAR CLIENT
	public List<ClassificationACBDTO> getAbcAnalyseParClient() {
	    List<ClassificationACBDTO> clients = new ArrayList<>();
	    
	    String sql = """
	        SELECT
	            c.NomCli,
	            SUM(v.Montant_Vente) AS ChiffreAffaires
	        FROM VENTE v
	        JOIN CLIENT c ON v.CodeClient = c.CodeCli
	        GROUP BY c.NomCli
	        ORDER BY ChiffreAffaires DESC
	    """;

	    try (Connection conn = DuckDBConnection.getConnection();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        while (rs.next()) {
	            clients.add(new ClassificationACBDTO(
	                rs.getString("NomCli"),
	                rs.getDouble("ChiffreAffaires"),
	                null, // La classe sera définie plus tard
	                null  // L'interprétation sera ajoutée plus tard
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return new ArrayList<>();
	    }

	    if (clients.isEmpty()) {
	        return new ArrayList<>();
	    }

	    // 2. Calculer le CA total et appliquer la classification ABC
	    double caTotal = clients.stream().mapToDouble(ClassificationACBDTO::getChiffreAffaires).sum();
	    double caCumule = 0;
	    
	    System.out.println("--- Classification ABC et interprétation des clients ---");
	    
	    for (ClassificationACBDTO item : clients) {
	        caCumule += item.getChiffreAffaires();
	        double pourcentageCumule = (caCumule / caTotal) * 100;
	        String classe;
	        String interpretation;
	        
	        if (pourcentageCumule <= 80) {
	            classe = "A";
	            interpretation = "👑 Client 'A' : Ce sont vos clients les plus importants, générant la majorité de votre chiffre d'affaires. Ils méritent une attention particulière et des efforts de fidélisation maximaux.";
	        } else if (pourcentageCumule <= 95) {
	            classe = "B";
	            interpretation = "💰 Client 'B' : Ces clients contribuent de manière significative mais moindre. Ils représentent un potentiel de croissance. Visez à les faire passer dans la catégorie 'A' par des offres ciblées.";
	        } else {
	            classe = "C";
	            interpretation= "👥 Client 'C' : Ces clients sont nombreux mais ne contribuent que faiblement au chiffre d'affaires. Une gestion automatisée ou des promotions de masse peuvent être suffisantes.";
	        }
	        
	        item.setClasse(classe);
	        item.setInterpretation(interpretation);
	    }
	    
	    return clients;
	}
		
		//CLASSIFICATION PAR LABO
	public List<ClassificationACBDTO> getAbcAnalyseParLabo() {
	    List<ClassificationACBDTO> labo = new ArrayList<>();
	    
	    String sql = """
	        SELECT
	            l.NomLabo,
	            SUM(v.Montant_Vente) AS ChiffreAffaires
	        FROM VENTE v
	        JOIN ARTICLE a ON v.CodeArticle = a.CodeArticle
	        JOIN LABO l ON a.CodeLabo = l.CodeLabo
	        GROUP BY l.NomLabo
	        ORDER BY ChiffreAffaires DESC
	    """;

	    try (Connection conn = DuckDBConnection.getConnection();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        while (rs.next()) {
	            labo.add(new ClassificationACBDTO(
	                rs.getString("NomLabo"),
	                rs.getDouble("ChiffreAffaires"),
	                null, // La classe sera définie plus tard
	                null  // L'interprétation sera ajoutée plus tard
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return new ArrayList<>();
	    }

	    if (labo.isEmpty()) {
	        return new ArrayList<>();
	    }

	    // 2. Calculer le CA total et appliquer la classification ABC
	    double caTotal = labo.stream().mapToDouble(ClassificationACBDTO::getChiffreAffaires).sum();
	    double caCumule = 0;
	    
	    System.out.println("--- Classification ABC et interprétation des laboratoires ---");
	    
	    for (ClassificationACBDTO item : labo) {
	        caCumule += item.getChiffreAffaires();
	        double pourcentageCumule = (caCumule / caTotal) * 100;
	        String classe;
	        String interpretation;
	        
	        if (pourcentageCumule <= 80) {
	            classe = "A";
	            interpretation = "💎 Labo 'A' : Partenaire stratégique qui génère la majeure partie de vos revenus. Ce sont les laboratoires avec lesquels vous devriez négocier les meilleures conditions d'achat.";
	        } else if (pourcentageCumule <= 95) {
	            classe = "B";
	            interpretation= "📈 Labo 'B' : Partenaire important avec un fort potentiel de croissance. Maintenez une bonne relation et cherchez des opportunités de développer leur gamme de produits.";
	        } else {
	            classe = "C";
	            interpretation= "🤝 Labo 'C' : Fournisseur de produits de niche ou à faible impact. La gestion de ces fournisseurs peut être plus légère, axée sur des commandes ponctuelles.";
	        }
	        
	        item.setClasse(classe);
	        item.setInterpretation(interpretation);
	    }
	    
	    return labo;
	}
	
	//SEGMENTATION RFM CLIENT
	public List<ClientRFMDTO> getRFMSegmentation() {
	    List<ClientRFMDTO> clientsRFM = new ArrayList<>();
	    
	    // 1. Requête SQL pour récupérer les données R, F et M brutes
	    String sql = """
	        SELECT
	            c.NomCli,
	            MAX(d.DateValue) AS DerniereCommande,
	            COUNT(DISTINCT v.CodeVente) AS FrequenceAchat,
	            SUM(v.Montant_Vente) AS MontantTotal
	        FROM Vente v
	        JOIN CLIENT c ON v.CodeClient = c.CodeCli
	        JOIN DATE_PERSO d ON v.CodeDate = d.CodeDate
	        GROUP BY c.NomCli
	        ORDER BY MontantTotal DESC
	    """;
	    
	    try (Connection conn = DuckDBConnection.getConnection();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        LocalDate aujourdHui = LocalDate.now();

	        while (rs.next()) {
	            java.sql.Date sqlDate = rs.getDate("DerniereCommande");
	            LocalDate derniereCommande = (sqlDate != null) ? sqlDate.toLocalDate() : null;

	            long recence = (derniereCommande != null) ? ChronoUnit.DAYS.between(derniereCommande, aujourdHui) : -1;
	            long frequence = rs.getLong("FrequenceAchat");
	            double montant = rs.getDouble("MontantTotal");

	            clientsRFM.add(new ClientRFMDTO(rs.getString("NomCli"), recence, frequence, montant, 0, 0, 0, null, null));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return new ArrayList<>();
	    }
	    
	    if (clientsRFM.isEmpty()) {
	        return new ArrayList<>();
	    }

	    // 2. Calculer les scores RFM (de 1 à 4)
	    calculerScoresRFM(clientsRFM);

	    // 3. Segmenter les clients et ajouter une interprétation
	    segmenterEtInterpreterClients(clientsRFM);

	    return clientsRFM;
	}

	
	//METHODES UTILITAIRES POUR LE RFM
	private void calculerScoresRFM(List<ClientRFMDTO> clients) {
	    int nombreClients = clients.size();
	    
	    // Attribution des scores de Récence (R)
	    // Les clients les plus récents (récence la plus faible) obtiennent le score le plus élevé (4).
	    clients.sort(Comparator.comparing(ClientRFMDTO::getRecence));
	    for (int i = 0; i < nombreClients; i++) {
	        int score = 4 - (int) Math.floor((double) i / nombreClients * 4);
	        clients.get(i).setScoreR(score);
	    }
	    
	    // Attribution des scores de Fréquence (F)
	    // Les clients les plus fréquents (fréquence la plus élevée) obtiennent le score le plus élevé (4).
	    clients.sort(Comparator.comparing(ClientRFMDTO::getFrequence).reversed());
	    for (int i = 0; i < nombreClients; i++) {
	        int score = (int) Math.floor((double) i / nombreClients * 4) + 1;
	        clients.get(i).setScoreF(score);
	    }
	    
	    // Attribution des scores de Montant (M)
	    // Les clients les plus dépensiers (montant le plus élevé) obtiennent le score le plus élevé (4).
	    clients.sort(Comparator.comparing(ClientRFMDTO::getMontant).reversed());
	    for (int i = 0; i < nombreClients; i++) {
	        int score = (int) Math.floor((double) i / nombreClients * 4) + 1;
	        clients.get(i).setScoreM(score);
	    }
	}
	
	private void segmenterEtInterpreterClients(List<ClientRFMDTO> clients) {
	    for (ClientRFMDTO client : clients) {
	        int r = client.getScoreR();
	        int f = client.getScoreF();
	        int m = client.getScoreM();
	        
	        String segment;
	        String interpretation;

	        if (r >= 4 && f >= 4 && m >= 4) {
	            segment = "Champions";
	            interpretation = "🚀 Clients très récents, fréquents et qui dépensent beaucoup. Ce sont vos meilleurs clients. Fédérez-les et offrez-leur des récompenses exclusives.";
	        } else if (r >= 4 && f >= 4) {
	            segment = "Clients fidèles";
	            interpretation = "🏆 Clients très fidèles qui achètent souvent et récemment. Ils sont la base de votre business. Assurez-vous de leur offrir une excellente expérience.";
	        } else if (r >= 4 && m >= 4) {
	            segment = "Nouveaux clients";
	            interpretation = "🌟 Clients récents qui ont beaucoup dépensé. Ils ont un fort potentiel de devenir des champions. Encouragez-les à revenir en leur proposant des offres pertinentes.";
	        } else if (r <= 2 && f >= 3 && m >= 3) {
	            segment = "À risque";
	            interpretation = "🚨 Clients qui étaient fréquents et dépensiers, mais qui n'ont pas acheté récemment. Ils sont en danger de désaffection. Tentez de les réactiver avec des offres spéciales.";
	        } else if (f <= 2 && m <= 2) {
	            segment = "Clients endormis";
	            interpretation = "😴 Clients qui n'ont pas acheté depuis longtemps et qui n'étaient ni fréquents ni dépensiers. Il est très difficile de les réactiver. Ciblez-les avec des promotions de masse ou laissez-les de côté.";
	        } else {
	            segment = "Autres";
	            interpretation = "Ce client ne correspond pas à un segment majeur. Son comportement d'achat est plus difficile à catégoriser. Une analyse plus fine est nécessaire.";
	        }

	        client.setSegmentRFM(segment);
	        client.setInterpretation(interpretation);
	    }
	}
}
