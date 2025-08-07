package com.Laborex.Application.Service.KPI;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
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
		
		//CA par periode
				public List<CaParPeriodeDTO> getCaParPeriode(){
					List<CaParPeriodeDTO> periodes= new ArrayList<>();
					String sql="""
							SELECT d.Mois, d.Annee, SUM(v.Montant_Vente) AS CA
							FROM VENTE v
							JOIN DATE_PERSO d ON v.CodeDate = d.CodeDate
							GROUP BY d.Mois, d.Annee
							ORDER BY CA
							""";
					try (Connection conn = DuckDBConnection.getConnection();
							Statement stmt = conn.createStatement();
							ResultSet rs = stmt.executeQuery(sql)){
						 while (rs.next()) {
							 periodes.add(new CaParPeriodeDTO(rs.getInt("Annee"), rs.getInt("Mois"),rs.getDouble("CA")));
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
				
				//****ROTATION***********
				public List<FrequenceVenteDTO> getFrequenceDeVenteParArticle(){
					List<FrequenceVenteDTO> rotation= new ArrayList<>();
					
					String sql= """
							SELECT a.LibArticle, COUNT(v.CodeVente) AS nombreDeVentes
							FROM VENTE v
							JOIN ARTICLE a ON v.CodeArticle = a.CodeArticle
							GROUP BY a.LibArticle
							ORDER BY nombreDeVentes DESC
							
							""";
					try (Connection conn = DuckDBConnection.getConnection();
				             Statement stmt = conn.createStatement();
				             ResultSet rs = stmt.executeQuery(sql)) {
						while (rs.next()) {
				            rotation.add(new FrequenceVenteDTO(
				                rs.getString("LibArticle"),
				                rs.getLong("nombreDeVentes")
				            ));
				        }
				        } catch (SQLException e) {
				            e.printStackTrace();
				        }
					return rotation;
				}
				
				//********PERFORMANCE CLIENT
				public List<PerformanceClientDTO> getPerformanceClients(){
					List<PerformanceClientDTO> performanceClients= new ArrayList<>();
					
					String sql="""
							SELECT c.NomCli, SUM(v.Montant_Vente) AS ChiffreAffaires,
							COUNT(v.CodeVente) AS FrequenceAchat,
							MAX(d.DateValue) AS DerniereCommande
							FROM Vente v
							JOIN CLIENT c ON v.CodeClient= c.CodeCli
							JOIN DATE_PERSO d ON v.CodeDate=d.CodeDate
							GROUP BY c.NomCli
							ORDER BY ChiffreAffaires DESC
							""";
					
					try (Connection conn = DuckDBConnection.getConnection();
				             Statement stmt = conn.createStatement();
				             ResultSet rs = stmt.executeQuery(sql)) {
						while (rs.next()) {
							// Convertir java.sql.Date en java.time.LocalDate
				            Date sqlDate = rs.getDate("DerniereCommande");
				            LocalDate derniereCommande = (sqlDate != null) ? sqlDate.toLocalDate() : null;

				            performanceClients.add(new PerformanceClientDTO(
				                rs.getString("NomCli"),
				                rs.getDouble("ChiffreAffaires"),
				                rs.getLong("FrequenceAchat"),
				                derniereCommande
				                ));
				        }
				        } catch (SQLException e) {
				            e.printStackTrace();
				        }
					return performanceClients;
							
				}
				
				//*******PERFORMANCE LABO
				public List<PerformanceLaboDTO> getPerformanceLabo(){
					List<PerformanceLaboDTO> performanceLabo = new ArrayList<>();
					//DISTINCT pour ne pas compter plusieurs fois le même article si plusieurs ventes ont été enregistrées pour celui-ci.
					String sql="""
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
						while (rs.next()) {
							performanceLabo.add(new PerformanceLaboDTO(
				                rs.getString("NomLabo"),
				                rs.getDouble("ChiffreAffaires"),
				                rs.getLong("NombreArticles")
				            ));
				        }
				        } catch (SQLException e) {
				            e.printStackTrace();
				        }
					return performanceLabo;
				}
				
				//********SAISONALITE DES VENTE
				public List<SaisonaliteDTO> getTendancesMesuelles(){
					List<SaisonaliteDTO> tendanceVentes= new ArrayList<>();
					
					String sql="""
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
						while (rs.next()) {
							tendanceVentes.add(new SaisonaliteDTO(
									rs.getInt("Annee"),
									rs.getInt("Mois"),
									rs.getDouble("ChiffreAffaires")
				                
				                ));
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
				        
				        while (rs.next()) {
				            result.add(new ImpactPromotionDTO(
				                rs.getString("NomPromo"),
				                rs.getLong("ugLivre"),
				                rs.getDouble("ChiffreAffaires")
				            ));
				        }
				    } catch (SQLException e) {
				        e.printStackTrace();
				    }
				    
				    return result;
				}
	
	
}

