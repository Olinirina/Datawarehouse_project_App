package com.Laborex.Application.Service.Importation;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
			
			//Gestion des clients (Presence facultatif)
			/*Client client;
			Optional<Client> clientOpt= clientRepository.findById(codeClient);
			if(clientOpt.isPresent()) {
				client= clientOpt.get();
				//Mise a jour
				client.setCodeCli(codeClient);
				client.setNomCli(nomClient);
				clientRepository.save(client);
				
			}else {
				client= new Client();
				client.setCodeCli(codeClient);
				client.setNomCli(nomClient);
				client= clientRepository.save(client);
			}*/
			
			
			// ✅ Client avec cache	        
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

	        // ✅ Labo avec cache
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

			
			//Gestion des labo
			/*Labo labo;
			Optional<Labo> laboOpt= laboRepository.findById(codeLabo);
			if(laboOpt.isPresent()) {
				labo= laboOpt.get();
				//Mise a jour
				labo.setCodeLabo(codeLabo);
				labo.setNomLabo(nomLabo);
				laboRepository.save(labo);
			}else {
				labo= new Labo();
				labo.setCodeLabo(codeLabo);
				labo.setNomLabo(nomLabo);
				labo= laboRepository.save(labo);
			}*/
			
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
			            // Valeurs par défaut si d'autres codes apparaissent (à ajuster si nécessaire)
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
				    /*Vente vente;
				    Optional<Vente> venteOpt= venteRepository.findById(codeVente);
				    if(venteOpt.isPresent()) {
				    	vente= venteOpt.get();
				    	//Mise ajour
				    	vente.setArticle(produit);
				    	vente.setClient(client);
				    	vente.setDate(date);
				    	vente.setMontantVente(montantVente);
				    	vente.setPromotion(promotion);
				    	vente.setQuantiteVendu(quantiteVendu);
				    	
				    }else {
				    	vente=new Vente();
				    	vente.setCodeVente(codeVente);
				    	vente.setQuantiteVendu(quantiteVendu);
				    	vente.setMontantVente(montantVente);
				    	vente.setClient(client);
				    	vente.setArticle(produit);
				    	vente.setDate(date);
				    	vente.setPromotion(promotion);*/
				    	
			    	// Ajouter à la liste
			        ventesBatch.add(vente);				     
			        ventesDejaAjoutees.add(codeVente);
			        rowIndex++;
		}
		// Insérer en batch à la fin
	    venteBatchService.insertInBatch(ventesBatch);
		
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


}
