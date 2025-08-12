package com.Laborex.Application.Service.KPI;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.KPI.DTO.CaVenteDTO;
import com.Laborex.Application.KPI.DTO.FrequenceVenteDTO;
import com.Laborex.Application.KPI.DTO.ImpactPromotionDTO;
import com.Laborex.Application.KPI.DTO.PerformanceClientDTO;
import com.Laborex.Application.KPI.DTO.PerformanceLaboDTO;
import com.Laborex.Application.KPI.DTO.SaisonaliteDTO;
import com.Laborex.Application.KPI.DTO.CA.CaParArticleDTO;
import com.Laborex.Application.KPI.DTO.CA.CaParClientDTO;
import com.Laborex.Application.KPI.DTO.CA.CaParLaboDTO;
import com.Laborex.Application.KPI.DTO.CA.CaParPeriodeDTO;
//Contient la logique de calcul des Kpi
@Service
public class KpiService {

	//CA par client
	public List<CaParClientDTO> getCaParClient() {
	    List<CaParClientDTO> result = new ArrayList<>();
	    String sql = """
	        SELECT c.NomCli, SUM(v.Montant_Vente) AS CA
	        FROM VENTE v
	        JOIN CLIENT c ON v.CodeClient = c.CodeCli
	        GROUP BY c.NomCli
	        ORDER BY CA DESC
	    """;
	    try (Connection conn = DuckDBConnection.getConnection();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {
	        while (rs.next()) {
	            result.add(new CaParClientDTO(rs.getString("NomCli"), rs.getDouble("CA")));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return result;
	}
	
	//Interpretation CA par Client
	public String getInterpretationCaParClient() {
	    List<CaParClientDTO> caParClient = new ArrayList<>(); 
	    // ... (code pour exécuter la requête et remplir la liste)
	    String sql="""
				SELECT c.NomCli, SUM(v.Montant_Vente) AS CA
				FROM VENTE v
				JOIN CLIENT c ON v.CodeClient = c.CodeCli
				GROUP BY c.NomCli
				ORDER BY CA
				""";
		try (Connection conn = DuckDBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)){
			 while (rs.next()) {
				 caParClient.add(new CaParClientDTO(rs.getString("NomCli"),rs.getDouble("CA")));
			 }
			 }catch (SQLException e) {
			        e.printStackTrace();
			    }

	    if (caParClient.isEmpty()) {
	        return "Aucune donnée de clients pour le moment.";
	    }

	   //Trouver le plus performant
	    CaParClientDTO meilleurClient = caParClient.stream()
	            .max(Comparator.comparing(CaParClientDTO::getChiffreAffaires))
	            .orElse(null);

	    if (meilleurClient != null) {
	        String nomCli = meilleurClient.getNomClient();
	        return "🏆 Le client le plus performant est **" + nomCli + "  avec un CA de " + String.format("%.2f", meilleurClient.getChiffreAffaires()) + " Ar.";
	    }
	    return "Interprétation non disponible.";
	}
	
	//CA par article
	public List<CaParArticleDTO> getCaParArticle(){
		List<CaParArticleDTO> articles= new ArrayList<>();
		String sql="""
				SELECT a.LibArticle, SUM(v.Montant_Vente) AS CA
				FROM Vente v
				JOIN ARTICLE a ON v.CodeArticle = a.CodeArticle
				GROUP BY a.LibArticle
				ORDER BY CA DESC
				""";
		try (Connection conn = DuckDBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)){
			 while (rs.next()) {
				 articles.add(new CaParArticleDTO(rs.getString("LibArticle"), rs.getDouble("CA")));
			 }
			 }catch (SQLException e) {
			        e.printStackTrace();
			    }
		return articles;
			 
		}
	//Interpretation CA par Articles
		public String getInterpretationCaParArticle() {
		    List<CaParArticleDTO> caParArticle = new ArrayList<>(); 
		    String sql="""
					SELECT a.LibArticle, SUM(v.Montant_Vente) AS CA
					FROM VENTE v
					JOIN ARTICLE a ON v.CodeArticle = a.CodeArticle
					GROUP BY a.LibArticle
					ORDER BY CA
					""";
			try (Connection conn = DuckDBConnection.getConnection();
					Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery(sql)){
				 while (rs.next()) {
					 caParArticle.add(new CaParArticleDTO(rs.getString("LibArticle"),rs.getDouble("CA")));
				 }
				 }catch (SQLException e) {
				        e.printStackTrace();
				    }

		    if (caParArticle.isEmpty()) {
		        return "Aucune donnée d'articles pour le moment.";
		    }
		    CaParArticleDTO meilleurArticle = caParArticle.stream()
		            .max(Comparator.comparing(CaParArticleDTO::getChiffreAffaires))
		            .orElse(null);

		    if (meilleurArticle != null) {
		        String nomArt = meilleurArticle.getLibelleArticle();
		        return "🏆 L'article le plus performant est **" + nomArt + "  avec un CA de " + String.format("%.2f", meilleurArticle.getChiffreAffaires()) + " Ar.";
		    }
		    return "Interprétation non disponible.";
		}
	
	
	//CA par labo
		public List<CaParLaboDTO> getCaParLabo(){
			List<CaParLaboDTO> labos= new ArrayList<>();
			String sql="""
					SELECT l.NomLabo, SUM(v.Montant_Vente) AS CA
					FROM VENTE v
					JOIN ARTICLE a ON v.CodeArticle = a.CodeArticle
					JOIN LABO l on a.CodeLabo= l.CodeLabo
					GROUP BY l.NomLabo
					ORDER BY CA
					""";
			try (Connection conn = DuckDBConnection.getConnection();
					Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery(sql)){
				 while (rs.next()) {
					 labos.add(new CaParLaboDTO(rs.getString("NomLabo"), rs.getDouble("CA")));
				 }
				 }catch (SQLException e) {
				        e.printStackTrace();
				    }
			return labos;
				 
			}
		
		//Interpretation CA par LAbo
				public String getInterpretationCaParLabo() {
				    List<CaParLaboDTO> caParLabo = new ArrayList<>(); 
				    
				    String sql="""
							SELECT l.NomLabo, SUM(v.Montant_Vente) AS CA
							FROM VENTE v
							JOIN ARTICLE a ON v.CodeArticle = a.CodeArticle
							JOIN LABO l ON a.CodeLabo = l.CodeLabo
							GROUP BY l.NomLabo
							ORDER BY CA
							""";
					try (Connection conn = DuckDBConnection.getConnection();
							Statement stmt = conn.createStatement();
							ResultSet rs = stmt.executeQuery(sql)){
						 while (rs.next()) {
							 caParLabo.add(new CaParLaboDTO(rs.getString("NomLabo"),rs.getDouble("CA")));
						 }
						 }catch (SQLException e) {
						        e.printStackTrace();
						    }

				    if (caParLabo.isEmpty()) {
				        return "Aucune donnée de labos pour le moment.";
				    }
				    CaParLaboDTO meilleurLabo = caParLabo.stream()
				            .max(Comparator.comparing(CaParLaboDTO::getChiffreAffaires))
				            .orElse(null);

				    if (meilleurLabo != null) {
				        String nomLabo = meilleurLabo.getNomLabo();
				        return "🏆 Le laboratoire le plus performant est **" + nomLabo + "  avec un CA de " + String.format("%.2f", meilleurLabo.getChiffreAffaires()) + " Ar.";
				    }
				    return "Interprétation non disponible.";
				}
		
		//CA par periode
				public List<CaParPeriodeDTO> getCaParPeriode(){
					List<CaParPeriodeDTO> periodes= new ArrayList<>();
					String sql="""
							SELECT d.Annee, d.Mois, d.Jour, SUM(v.Montant_Vente) AS CA
							FROM VENTE v
							JOIN DATE_PERSO d ON v.CodeDate = d.CodeDate
							GROUP BY d.Annee, d.Mois, d.Jour
							ORDER BY CA DESC
							""";
					
					try (Connection conn = DuckDBConnection.getConnection();
							Statement stmt = conn.createStatement();
							ResultSet rs = stmt.executeQuery(sql)){
						 while (rs.next()) {
							 periodes.add(new CaParPeriodeDTO(rs.getInt("Annee"), rs.getInt("Mois"), rs.getInt("Jour"),rs.getDouble("CA")));
						 }
						
						 }catch (SQLException e) {
						        e.printStackTrace();
						    }
					return periodes;
						 
					}
				
				//CA Global
				public CaVenteDTO getCaDashboardData() {
			        //Calculer le CA global
			        double caGlobal = 0.0;
			        String sqlCaGlobal = "SELECT SUM(Montant_Vente) AS caGlobal FROM VENTE";
			        try (Connection conn = DuckDBConnection.getConnection();
			             Statement stmt = conn.createStatement();
			             ResultSet rs = stmt.executeQuery(sqlCaGlobal)) {
			            if (rs.next()) {
			                caGlobal = rs.getDouble("caGlobal");
			            }
			        } catch (SQLException e) {
			            e.printStackTrace();
			        }
			        

			        //Appeler les méthodes de calcul par dimension
			        List<CaParClientDTO> caParClient = getCaParClient();
			        List<CaParArticleDTO> caParArticle = getCaParArticle();
			        List<CaParLaboDTO> caParLabo = getCaParLabo();
			        List<CaParPeriodeDTO> caParPeriode = getCaParPeriode();

			        //Regrouper tous les résultats dans le DTO combiné
			        return new CaVenteDTO(
			            caGlobal,
			            caParClient,
			            caParArticle,
			            caParLabo,
			            caParPeriode
			        );
			    }
				//Interpretation CA par periode
				public String getInterpretationCaParPeriode() {
				    // Liste des DTO de CA par période
				    List<CaParPeriodeDTO> caParMois = new ArrayList<>(); 
				    // ... (code pour exécuter la requête et remplir la liste)
				    String sql="""
							SELECT d.Annee, d.Mois, d.Jour, SUM(v.Montant_Vente) AS CA
							FROM VENTE v
							JOIN DATE_PERSO d ON v.CodeDate = d.CodeDate
							GROUP BY d.Annee, d.Mois, d.Jour
							ORDER BY CA
							""";
					try (Connection conn = DuckDBConnection.getConnection();
							Statement stmt = conn.createStatement();
							ResultSet rs = stmt.executeQuery(sql)){
						 while (rs.next()) {
							 caParMois.add(new CaParPeriodeDTO(rs.getInt("Annee"), rs.getInt("Mois"), rs.getInt("Jour"),rs.getDouble("CA")));
						 }
						 }catch (SQLException e) {
						        e.printStackTrace();
						    }

				    if (caParMois.isEmpty()) {
				        return "Aucune donnée de vente pour le moment.";
				    }

				   
				    CaParPeriodeDTO meilleurMois = caParMois.stream()
				            .max(Comparator.comparing(CaParPeriodeDTO::getChiffreAffaires))
				            .orElse(null);

				    if (meilleurMois != null) {
				        String nomMois = obtenirNomMois(meilleurMois.getMois()); // Fonction utilitaire à créer
				        return "🏆 Le mois le plus performant est **" + nomMois + " " + meilleurMois.getAnnee() + "** avec un CA de " + String.format("%.2f", meilleurMois.getChiffreAffaires()) + " Ar.";
				    }
				    return "Interprétation non disponible.";
				}

				// Fonction utilitaire pour obtenir le nom du mois
				private String obtenirNomMois(int mois) {
				    String[] nomsMois = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
				    if (mois >= 1 && mois <= 12) {
				        return nomsMois[mois - 1];
				    }
				    return "Mois inconnu";
				}
				
				
				// **** ROTATION ****
				public List<FrequenceVenteDTO> getFrequenceDeVenteParArticle() {
				    List<FrequenceVenteDTO> rotation = new ArrayList<>();
				    
				    String sql = """
				            SELECT a.LibArticle, COUNT(v.CodeVente) AS nombreDeVentes
				            FROM VENTE v
				            JOIN ARTICLE a ON v.CodeArticle = a.CodeArticle
				            GROUP BY a.LibArticle
				            ORDER BY nombreDeVentes DESC
				            """;
				            
				    try (Connection conn = DuckDBConnection.getConnection();
				         Statement stmt = conn.createStatement();
				         ResultSet rs = stmt.executeQuery(sql)) {
				        
				        // Remplir la liste avec les données brutes
				        while (rs.next()) {
				            rotation.add(new FrequenceVenteDTO(
				                rs.getString("LibArticle"),
				                rs.getLong("nombreDeVentes"),
				                null 
				            ));
				        }
				        
				        // Calculer et ajouter l'interprétation si la liste n'est pas vide
				        if (!rotation.isEmpty()) {
				            long maxVentes = rotation.get(0).getNombreDeVentes();
				            System.out.println("--- Interprétation de la rotation des articles ---");
				            
				            for (FrequenceVenteDTO dto : rotation) {
				                long nombreDeVentes = dto.getNombreDeVentes();
				                
				                // Catégorisation basée sur un ratio par rapport au top article
				                double ratio = (double) nombreDeVentes / maxVentes;
				                
				                String commentaire;
				                if (ratio >= 0.75) {
				                    commentaire = "🟢 Article à rotation élevée : Cet article est très populaire et se vend fréquemment. Il est essentiel de s'assurer de sa disponibilité.";
				                } else if (ratio >= 0.25) {
				                    commentaire = "🟡 Article à rotation moyenne : Cet article a une demande stable. Il est important de maintenir un stock suffisant pour répondre à la demande régulière.";
				                } else {
				                    commentaire = "🔴 Article à faible rotation : Cet article se vend rarement. Il pourrait être judicieux de revoir son stock, de lancer des promotions ou d'envisager son retrait pour éviter les coûts de stockage.";
				                }
				                
				                dto.setInterpretation(commentaire);			               
				            }
				        } else {
				            System.out.println("Aucune donnée de vente disponible pour l'interprétation.");
				        }
				        
				    } catch (SQLException e) {
				        e.printStackTrace();
				    }
				    return rotation;
				}
				
				//********PERFORMANCE CLIENT
				public List<PerformanceClientDTO> getPerformanceClients() {
				    List<PerformanceClientDTO> performanceClients = new ArrayList<>();
				    
				    String sql = """
				        SELECT c.NomCli, SUM(v.Montant_Vente) AS ChiffreAffaires,
				        COUNT(v.CodeVente) AS FrequenceAchat,
				        MAX(d.DateValue) AS DerniereCommande
				        FROM Vente v
				        JOIN CLIENT c ON v.CodeClient = c.CodeCli
				        JOIN DATE_PERSO d ON v.CodeDate = d.CodeDate
				        GROUP BY c.NomCli
				        ORDER BY ChiffreAffaires DESC
				        """;
				    
				    try (Connection conn = DuckDBConnection.getConnection();
				         Statement stmt = conn.createStatement();
				         ResultSet rs = stmt.executeQuery(sql)) {
				         
				        //Remplir la liste avec les données brutes
				        while (rs.next()) {
				            java.sql.Date sqlDate = rs.getDate("DerniereCommande");
				            LocalDate derniereCommande = (sqlDate != null) ? sqlDate.toLocalDate() : null;

				            performanceClients.add(new PerformanceClientDTO(
				                rs.getString("NomCli"),
				                rs.getDouble("ChiffreAffaires"),
				                rs.getLong("FrequenceAchat"),
				                derniereCommande,
				                null 
				            ));
				        }
				        
				        // Analyser et ajouter l'interprétation pour chaque client
				        if (!performanceClients.isEmpty()) {
				            double maxChiffreAffaires = performanceClients.get(0).getChiffreAffaires();
				            System.out.println("--- Analyse de la performance des clients ---");
				            
				            for (PerformanceClientDTO client : performanceClients) {
				                double chiffreAffaires = client.getChiffreAffaires();
				                long frequenceAchat = client.getFrequenceAchat();
				                
				                // Calcul de la pertinence basé sur le chiffre d'affaires par rapport au meilleur client
				                double ratioCA = chiffreAffaires / maxChiffreAffaires;
				                
				                String commentaire;
				                if (ratioCA >= 0.75 && frequenceAchat > 5) {
				                    commentaire = "🌟 **Client VIP** : Chiffre d'affaires très élevé et achats fréquents. C'est un client essentiel à fidéliser.";
				                } else if (ratioCA >= 0.25 || frequenceAchat > 2) {
				                    commentaire = "📈 **Client régulier** : Ce client a une contribution notable et effectue des achats fréquents. Il a un fort potentiel de croissance.";
				                } else {
				                    commentaire = "👤 **Client occasionnel** : Chiffre d'affaires faible ou achats peu fréquents. Il pourrait bénéficier d'offres ciblées pour augmenter sa valeur.";
				                }
				                
				                client.setInterpretation(commentaire);			                
				            }
				        } else {
				            System.out.println("Aucune donnée de performance client disponible pour l'analyse.");
				        }
				    } catch (SQLException e) {
				        e.printStackTrace();
				    }
				    return performanceClients;
				}
				
				public List<PerformanceLaboDTO> getPerformanceLabo() {
				    List<PerformanceLaboDTO> performanceLabo = new ArrayList<>();

				    String sql = """
				            SELECT 
				                l.NomLabo,
				                SUM(v.Montant_Vente) AS ChiffreAffaires,
				                COUNT(DISTINCT a.CodeArticle) AS NombreArticles
				            FROM VENTE v
				            JOIN ARTICLE a ON v.CodeArticle = a.CodeArticle
				            JOIN LABO l ON a.CodeLabo = l.CodeLabo
				            GROUP BY l.NomLabo
				            ORDER BY ChiffreAffaires DESC;
				            """;
				    
				    try (Connection conn = DuckDBConnection.getConnection();
				         Statement stmt = conn.createStatement();
				         ResultSet rs = stmt.executeQuery(sql)) {

				        // Étape 1 : Remplir la liste avec les données brutes
				        while (rs.next()) {
				            performanceLabo.add(new PerformanceLaboDTO(
				                rs.getString("NomLabo"),
				                rs.getDouble("ChiffreAffaires"),
				                rs.getLong("NombreArticles"),
				                null // L'interprétation sera ajoutée à l'étape 2
				            ));
				        }

				        // Étape 2 : Calculer et ajouter l'interprétation pour chaque laboratoire
				        if (!performanceLabo.isEmpty()) {
				            double maxChiffreAffaires = performanceLabo.get(0).getChiffreAffaire();
				            long maxArticles = performanceLabo.stream()
				                                            .mapToLong(PerformanceLaboDTO::getNombreArticle)
				                                            .max()
				                                            .orElse(0);

				            System.out.println("--- Analyse de la performance des laboratoires ---");

				            for (PerformanceLaboDTO labo : performanceLabo) {
				                double chiffreAffaires = labo.getChiffreAffaire();
				                long nombreArticles = labo.getNombreArticle();
				                
				                // Calcul des ratios pour une évaluation relative
				                double ratioCA = chiffreAffaires / maxChiffreAffaires;
				                double ratioArticles = (double) nombreArticles / maxArticles;

				                String commentaire;
				                if (ratioCA >= 0.75 && ratioArticles >= 0.5) {
				                    commentaire = "🏆 **Partenaire stratégique** : Ce laboratoire génère un chiffre d'affaires très élevé avec une offre de produits diversifiée. Il est un partenaire clé.";
				                } else if (ratioCA >= 0.25 && ratioArticles >= 0.25) {
				                    commentaire = "🤝 **Partenaire fiable** : Ce laboratoire a une contribution solide au chiffre d'affaires et une gamme de produits raisonnable. Un partenaire sur lequel on peut compter.";
				                } else {
				                    commentaire = "🧪 **Partenaire émergent ou de niche** : Ce laboratoire a une contribution modérée. Il peut s'agir d'un nouveau partenaire à développer ou d'un acteur de niche.";
				                }

				                labo.setInterpretation(commentaire);
				            }
				        } else {
				            System.out.println("Aucune donnée de performance de laboratoire disponible pour l'analyse.");
				        }

				    } catch (SQLException e) {
				        e.printStackTrace();
				    }
				    return performanceLabo;
				}
				
				//********SAISONALITE DES VENTE
				public List<SaisonaliteDTO> getTendancesMesuelles() {
				    List<SaisonaliteDTO> tendanceVentes = new ArrayList<>();

				    String sql = """
				            SELECT
				                d.Annee,
				                d.Mois,
				                SUM(v.Montant_Vente) AS ChiffreAffaires
				            FROM VENTE v
				            JOIN DATE_PERSO d ON v.CodeDate = d.CodeDate
				            GROUP BY
				                d.Annee,
				                d.Mois
				            ORDER BY
				                d.Annee ASC,
				                d.Mois ASC;
				            """;

				    try (Connection conn = DuckDBConnection.getConnection();
				         Statement stmt = conn.createStatement();
				         ResultSet rs = stmt.executeQuery(sql)) {

				        // Étape 1 : Remplir la liste avec les données brutes
				        while (rs.next()) {
				            tendanceVentes.add(new SaisonaliteDTO(
				                rs.getInt("Annee"),
				                rs.getInt("Mois"),
				                rs.getDouble("ChiffreAffaires"),
				                null // L'interprétation sera ajoutée à l'étape 2
				            ));
				        }

				        // Étape 2 : Analyser et ajouter l'interprétation
				        if (!tendanceVentes.isEmpty()) {
				            // Calculer le chiffre d'affaires moyen sur l'ensemble de la période
				            double chiffreAffairesTotal = tendanceVentes.stream()
				                                                      .mapToDouble(SaisonaliteDTO::getChiffreAffaire)
				                                                      .sum();
				            double chiffreAffairesMoyen = chiffreAffairesTotal / tendanceVentes.size();

				            System.out.println("--- Analyse de la saisonnalité et des tendances de vente ---");

				            for (SaisonaliteDTO dto : tendanceVentes) {
				                double chiffreAffaires = dto.getChiffreAffaire();
				                String commentaire;

				                if (chiffreAffaires > chiffreAffairesMoyen * 1.2) { // 20% au-dessus de la moyenne
				                    commentaire = "🚀 **Période de forte croissance** : Les ventes sont nettement au-dessus de la moyenne. C'est un mois ou une période de haute saison. Il est crucial d'anticiper la demande et de maximiser les stocks et les efforts marketing.";
				                } else if (chiffreAffaires < chiffreAffairesMoyen * 0.8) { // 20% en dessous de la moyenne
				                    commentaire = "📉 **Période de faible activité** : Les ventes sont en-dessous de la moyenne. C'est un mois de basse saison. Cela pourrait être le moment idéal pour lancer des promotions ciblées ou faire des liquidations.";
				                } else {
				                    commentaire = "📊 **Période stable** : Les ventes sont conformes à la moyenne. C'est une période de performance standard.";
				                }

				                dto.setInterpretation(commentaire);

				               	}
				        } else {
				            System.out.println("Aucune donnée de vente disponible pour l'analyse de saisonnalité.");
				        }

				    } catch (SQLException e) {
				        e.printStackTrace();
				    }
				    return tendanceVentes;
				}
				
				//******IMPACT PROMOTION SUR LES VENTES
				public List<ImpactPromotionDTO> getImpactPromotions() {
				    List<ImpactPromotionDTO> result = new ArrayList<>();
				    
				    String sql = """
				        SELECT
				            p.NomPromo,
				            SUM(p.UgLivre) AS ugLivre,
				            SUM(v.Montant_Vente) AS ChiffreAffaires
				        FROM VENTE v
				        JOIN PROMOTION p ON v.CodePromo = p.CodePromo
				        GROUP BY p.NomPromo
				        ORDER BY ChiffreAffaires DESC
				    """;

				    try (Connection conn = DuckDBConnection.getConnection();
				         Statement stmt = conn.createStatement();
				         ResultSet rs = stmt.executeQuery(sql)) {
				        
				        // Étape 1 : Remplir la liste avec les données brutes
				        while (rs.next()) {
				            result.add(new ImpactPromotionDTO(
				                rs.getString("NomPromo"),
				                rs.getLong("ugLivre"),
				                rs.getDouble("ChiffreAffaires"),
				                null // L'interprétation sera ajoutée à l'étape 2
				            ));
				        }

				        // Étape 2 : Analyser et ajouter l'interprétation
				        if (!result.isEmpty()) {
				            double maxChiffreAffaires = result.get(0).getChiffreAffaires();
				            long maxUgLivre = result.get(0).getUgLivre();
				            
				            System.out.println("--- Analyse de l'impact des promotions ---");

				            for (ImpactPromotionDTO promo : result) {
				                String commentaire;
				                double ratioCA = promo.getChiffreAffaires() / maxChiffreAffaires;
				                double ratioUg = (double) promo.getUgLivre() / maxUgLivre;

				                if (ratioCA >= 0.75 && ratioUg >= 0.75) {
				                    commentaire = "🎉 **Promotion à succès** : Chiffre d'affaires et volume de ventes très élevés. C'est une promotion très performante à renouveler.";
				                } else if (ratioCA >= 0.5 && ratioUg < 0.5) {
				                    commentaire = "💰 **Promotion à forte marge** : Génère un bon chiffre d'affaires malgré un volume de ventes modéré. Cela indique une bonne rentabilité des produits en promotion.";
				                } else if (ratioCA < 0.5 && ratioUg >= 0.5) {
				                    commentaire = "📦 **Promotion volumineuse** : Génère un grand volume de ventes mais un chiffre d'affaires plus faible. Efficace pour le déstockage ou l'attraction de nouveaux clients.";
				                } else {
				                    commentaire = "💡 **Promotion à optimiser** : Chiffre d'affaires et volume de ventes modérés. Cette promotion pourrait être améliorée ou remplacée.";
				                }
				                
				                promo.setInterpretation(commentaire);			             
				            }
				        } else {
				            System.out.println("Aucune donnée de promotion disponible pour l'analyse.");
				        }

				    } catch (SQLException e) {
				        e.printStackTrace();
				    }
				    
				    return result;
				}
	
	
}

