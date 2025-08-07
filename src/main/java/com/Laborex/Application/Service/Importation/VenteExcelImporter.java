package com.Laborex.Application.Service.Importation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Laborex.Application.Dao.DuckDBConnection;
import com.Laborex.Application.Dao.VenteBatchService;
import com.Laborex.Application.Model.Article;
import com.Laborex.Application.Model.Client;
import com.Laborex.Application.Model.DatePerso;
import com.Laborex.Application.Model.Labo;
import com.Laborex.Application.Model.Promotion;
import com.Laborex.Application.Model.Tva;
import com.Laborex.Application.Model.Vente;
import com.Laborex.Application.Repository.ArticleRepository;
import com.Laborex.Application.Repository.ClientRepository;
import com.Laborex.Application.Repository.DateRepository;
import com.Laborex.Application.Repository.LaboRepository;
import com.Laborex.Application.Repository.PromotionRepository;
import com.Laborex.Application.Repository.RegionRepository;
import com.Laborex.Application.Repository.TvaRepository;
import com.Laborex.Application.Repository.VenteRepository;


@Service
public class VenteExcelImporter implements ExcelImportation {
	@Autowired
	public RegionRepository regionRepository;
	@Autowired
	public DateRepository dateRepository;
	@Autowired
	public ClientRepository clientRepository;
	@Autowired
	public ArticleRepository articleRepository;
	@Autowired
	public LaboRepository laboRepository;
	@Autowired
	public PromotionRepository promoRepository;
	@Autowired
	public VenteRepository venteRepository;
	@Autowired
	public TvaRepository tvaRepository;
	@Autowired
	public VenteBatchService venteBatchService;

	@Override
	public boolean supports(String filename) {	
		return filename.toLowerCase().contains("essai");
	}

	@Override
	public void importData(Sheet sheet) throws IOException {
		Row headerRow = null;
        int rowIndex = 0;
        Map<String, Integer> columnIndexMap = new HashMap<>();		
		//Pour l'insertion
		List<Vente> ventesBatch = new ArrayList<>();
		
		
		
		Map<String, Labo> laboCache = new HashMap<>();
		Map<String, Client> clientCache = new HashMap<>();
		
		Set<String> ventesDejaAjoutees = new HashSet<>(); // à mettre avant la boucle
		
		for (Row row : sheet) {
            if (rowIndex == 0) {
                headerRow = row;
                // Construction de la map des colonnes
                for (Cell cell : headerRow) {
                    columnIndexMap.put(cell.getStringCellValue().trim().toUpperCase(), cell.getColumnIndex());
                }
                rowIndex++;
                continue;
            }
            
            if (row == null) {
                rowIndex++;
                continue;
            }
			
			//Lecture des valeurs
			//Client
			String codeClient= getStringValue(row.getCell(columnIndexMap.get("NOCLI")));
			String nomClient= getStringValue(row.getCell(columnIndexMap.get("NOMCLI")));
			//Article
			String codeArticle= getStringValue(row.getCell(columnIndexMap.get("NOART")));
			String LibArticle= getStringValue(row.getCell(columnIndexMap.get("LIBART")));
			//Labo
			String codeLabo= getStringValue(row.getCell(columnIndexMap.get("LABO")));
			String nomLabo= getStringValue(row.getCell(columnIndexMap.get("NOMLAB")));
			//TVA
			String codeTva= getStringValue(row.getCell(columnIndexMap.get("CODTVA"))).trim();
			//Promotion
			String codePromo= getStringValue(row.getCell(columnIndexMap.get("NOPRM")));
			String nomPromo= getStringValue(row.getCell(columnIndexMap.get("NOMPRM")));
			String typePromo= getStringValue(row.getCell(columnIndexMap.get("TYPPRM")));
			int ug=(int) getNumericValue(row.getCell(columnIndexMap.get("UGLIV")));
			//vente
			String codeVente= getStringValue(row.getCell(columnIndexMap.get("NOFACL")));
			int quantiteVendu= (int) getNumericValue(row.getCell(columnIndexMap.get("QTLVCL")));
			double montantVente= getNumericValue(row.getCell(columnIndexMap.get("MTLIG")));
			//Date
			int jour= (int) getNumericValue(row.getCell(columnIndexMap.get("JJFACL")));
			int mois= (int) getNumericValue(row.getCell(columnIndexMap.get("MMFACL")));
			int annee= (int) getNumericValue(row.getCell(columnIndexMap.get("AAFACL")));
			
			//Gestion de la date
			// Construire la date
			LocalDate localDate;
			try {
			    localDate = LocalDate.of(annee, mois, jour);
			} catch (DateTimeException e) {
			    System.err.println("Date invalide à la ligne " +e.getMessage());
			    continue; // ou throw une exception si tu préfères
			}

			// Créer ou récupérer la ligne Temps correspondante
			DatePerso date = getOrCreateTemps(localDate);
						
			
			// Client avec cache	        
	        Client client = clientCache.get(codeClient);
	        if (client == null) {
	            client = clientRepository.findById(codeClient).orElseGet(() -> {
	                Client c = new Client();
	                c.setCodeCli(codeClient);
	                c.setNomCli(nomClient);
	                return clientRepository.save(c);
	            });
	            clientCache.put(codeClient, client);
	            
	        }
	        

	        // Labo avec cache
	        Labo labo = laboCache.get(codeLabo);
	        if (labo == null) {
	            labo = laboRepository.findById(codeLabo).orElseGet(() -> {
	                Labo c = new Labo();
	                c.setCodeLabo(codeLabo);
	                c.setNomLabo(nomLabo);
	                return laboRepository.save(c);
	            });
	            laboCache.put(codeLabo, labo);
	        }
			
			
			//Gestion des tva
			Tva tva;
			    Optional<Tva> tvaOpt = tvaRepository.findById(codeTva);
			    
			    if (tvaOpt.isPresent()) {
			        tva = tvaOpt.get(); // déjà existante
			    } else {
			    	tva = new Tva();
			        tva.setCodeTva(codeTva);
			        
			        // Ajout des données spécifiques selon le code
			        if (codeTva.equals("7")) {
			            tva.setTaux(20.0);
			            tva.setNature("Complément alimentaire");
			        } else {
			            // Valeurs par défaut si d'autres codes apparaissent 
			            tva.setTaux(0.0);
			            tva.setNature("Non défini");
			        }
			        
			        tva = tvaRepository.save(tva);
			    }
			    
			
			// Gestion des article 
			    Optional<Article> articleOpt= articleRepository.findById(codeArticle);
	            if(!articleOpt.isPresent()) {
	            	System.err.println("Produit inexistant : " + codeArticle);
				    continue; // Ignore la ligne
	            }
	            Article produit= articleOpt.get();  
						
				
				    //Gestion des promotions(presence facultatif)
				    Promotion promotion;
				    Optional<Promotion> promoOpt= promoRepository.findById(codePromo);
				    if(promoOpt.isPresent()) {
				    	promotion= promoOpt.get();
				    	//Mise a jour
				    	promotion.setNomPromo(nomPromo);
				    	promotion.setTypePromo(typePromo);
				    	promotion.setUgLivre(ug);
				    	promoRepository.save(promotion);
				    }else {
				    	promotion= new Promotion();
				    	promotion.setCodePromo(codePromo);
				    	promotion.setNomPromo(nomPromo);
				    	promotion.setTypePromo(typePromo);
				    	promotion.setUgLivre(ug);
				    	promotion= promoRepository.save(promotion);
				    }
				    
				    //Suite Article- mise a jour
				    produit.setLabo(labo);
				    produit.setTva(tva);
				    articleRepository.save(produit);
				    
				    
				    //Vente
				    if (venteRepository.existsById(codeVente) || ventesDejaAjoutees.contains(codeVente)) {
				        continue; // vente déjà existante, on ignore cette ligne
				    }
				    
				    Vente vente=new Vente();
			    	vente.setCodeVente(codeVente);
			    	vente.setQuantiteVendu(quantiteVendu);
			    	vente.setMontantVente(montantVente);
			    	vente.setClient(client);
			    	vente.setArticle(produit);
			    	vente.setDate(date);
			    	vente.setPromotion(promotion);
				 
				    	
			    	// Ajouter à la liste
			        ventesBatch.add(vente);				     
			        ventesDejaAjoutees.add(codeVente);
			        rowIndex++;
			        
		}
		// Insérer en batch à la fin
	    venteBatchService.insertInBatch(ventesBatch);
	    System.out.println("Import terminé. " + ventesBatch.size() + " ventes importées.");
	    System.out.println("Les données seront synchronisées vers DuckDB au prochain redémarrage ou via /api/kpi/sync-force");
	}
	
	
	// Méthodes utilitaires
    public double getNumericValue(Cell cell) {
        if (cell == null) return 0;

        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().replace(",", "."));
            } catch (NumberFormatException e) {
                return 0; // ou logger une erreur
            }
        }
        return 0;
    }

    public String getStringValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return "";
    }
    //Methode pour gerer le temps dynamiquements
    public DatePerso getOrCreateTemps(LocalDate localDate) {
        String codeDate = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return dateRepository.findById(codeDate)
            .orElseGet(() -> {
                DatePerso temps = new DatePerso();
                temps.setCodeDate(codeDate);
                temps.setDate(java.sql.Date.valueOf(localDate));
                temps.setJour(localDate.getDayOfMonth());
                temps.setMois(localDate.getMonthValue());
                temps.setAnnee(localDate.getYear());        

                return dateRepository.save(temps);
            });
    }
    @Override
    public int getPriorite() {
        return 2;
    }
    
   /* public void synchroniserVersDuckDB(List<Vente> ventes) {
        if (ventes.isEmpty()) return;
        
        String sql = "INSERT OR REPLACE INTO VENTE VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DuckDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Vente v : ventes) {
                stmt.setString(1, v.getCodeVente());
                stmt.setInt(2, v.getQuantiteVendu());
                stmt.setDouble(3, v.getMontantVente());                
                stmt.setString(4, v.getClient().getCodeCli());
                stmt.setString(5, v.getArticle().getCodeArticle());
                stmt.setString(6, v.getDate().getCodeDate());
                stmt.setString(7, v.getPromotion() != null ? v.getPromotion().getCodePromo() : null);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            System.out.println("Synchronisé " + results.length + " ventes vers DuckDB");
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la synchronisation des ventes: " + e.getMessage());
            e.printStackTrace();
        }
    }*/

   

}
