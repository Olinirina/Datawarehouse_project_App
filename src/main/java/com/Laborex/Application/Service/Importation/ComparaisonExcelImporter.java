package com.Laborex.Application.Service.Importation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Laborex.Application.Model.Article;
import com.Laborex.Application.Model.Comparaison;
import com.Laborex.Application.Model.Concurrent;
import com.Laborex.Application.Repository.ArticleRepository;
import com.Laborex.Application.Repository.ComparaisonRepository;
import com.Laborex.Application.Repository.ConcurrentRepository;

@Service
public class ComparaisonExcelImporter implements ExcelImportation {
	@Autowired
	public ArticleRepository articleRepository;
	@Autowired
	public ConcurrentRepository concurrentRepository;
	@Autowired
	public ComparaisonRepository comparaisonRepository;
	@Override
	public boolean supports(String filename) {
		return filename.toLowerCase().contains("compar");
	}

	@Override
	public void importData(Sheet sheet) throws IOException {
		Row headerRow = null;
        int rowIndex = 0;
        Map<String, Integer> columnIndexMap = new HashMap<>();		
		
		// Liste des concurrents (noms des colonnes)
	    List<String> concurrents = List.of("SOMAPHAR", "DROGEMAD", "COFARMA","OPHAM","INTERPHARMA","MEDICO","UBI","SOPHASU","PHARMALIFE");
		
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
			
			//Articles
            String codeArticle= getStringValue(row.getCell(columnIndexMap.get("CODE")));
            String libelleArticle= getStringValue(row.getCell(columnIndexMap.get("LIBELLÉ")));
            double prixVente= getNumericValue(row.getCell(columnIndexMap.get("PRIX DE VENTE ACTUEL")));
			//Gestion des articles
			Article article = articleRepository.findById(codeArticle)
		            .orElseGet(() -> {
		                Article newArticle = new Article();
		                newArticle.setCodeArticle(codeArticle);
		                newArticle.setLibelle(libelleArticle);
		                newArticle.setPrixVente(prixVente);
		                return articleRepository.save(newArticle);
		            });
			
			// Pour chaque concurrent
			for (String nomConcurrent : concurrents) {
			    String nomCol = nomConcurrent.trim().toUpperCase();
			    if (!columnIndexMap.containsKey(nomCol)) {
			        continue;
			    }

			    Integer colIndex = columnIndexMap.get(nomCol);
			    double prixConcurrent = getNumericValue(row.getCell(colIndex));

			    Concurrent concurrent = concurrentRepository.findById(nomConcurrent)
			        .orElseGet(() -> {
			            Concurrent newConcurrent = new Concurrent();
			            newConcurrent.setCodeConcurrent(nomConcurrent);
			            newConcurrent.setNomConcurrent(nomConcurrent);
			            return concurrentRepository.save(newConcurrent);
			        });

			    Comparaison comparaison = new Comparaison();
			    comparaison.setArticle(article);
			    comparaison.setConcurrent(concurrent);
			    comparaison.setPrixConcurrent(prixConcurrent);	            

			    comparaisonRepository.save(comparaison);
			}
	        rowIndex++;
			
			
		}
	        
	     
		
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
    
    @Override
    public int getPriorite() {
        return 3;
    }


}
