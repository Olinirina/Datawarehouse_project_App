package com.Laborex.Application.Service.Importation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
		return filename.toLowerCase().contains("com");
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
            String libelleArticle= getStringValue(row.getCell(columnIndexMap.get("LIBELLE")));
            Double prixVente= getNumericValue(row.getCell(columnIndexMap.get("PRIXACTUEL")));
			//Gestion des articles
            Article article = articleRepository.findById(codeArticle)
            	    .map(existingArticle -> {
            	        existingArticle.setLibelle(libelleArticle);  // mise à jour du libellé
            	        existingArticle.setPrixVente(prixVente);     // mise à jour du prix
            	        return articleRepository.save(existingArticle);
            	    })
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

                // Vérifie si la cellule est vide ou non numérique
                if (prixConcurrent == 0) {
                    continue; // ignore les valeurs nulles ou invalides
                }

                Concurrent concurrent = concurrentRepository.findById(nomConcurrent)
                    .orElseGet(() -> {
                        Concurrent newConcurrent = new Concurrent();
                        newConcurrent.setCodeConcurrent(nomConcurrent);
                        newConcurrent.setNomConcurrent(nomConcurrent);
                        return concurrentRepository.save(newConcurrent);
                    });

                // Rechercher une comparaison existante entre cet article et ce concurrent
                Optional<Comparaison> existing = comparaisonRepository
                    .findByArticleAndConcurrent(article, concurrent);

                if (existing.isPresent()) {
                    Comparaison c = existing.get();
                    c.setPrixConcurrent(prixConcurrent);
                    comparaisonRepository.save(c);
                } else {
                    Comparaison c = new Comparaison();
                    c.setArticle(article);
                    c.setConcurrent(concurrent);
                    c.setPrixConcurrent(prixConcurrent);
                    comparaisonRepository.save(c);
                }

            }

	        rowIndex++;			
		}
  
		
	}
	
	// Méthodes utilitaires
	public double getNumericValue(Cell cell) {
	    if (cell == null) return 0;
	    
	    try {
	        switch (cell.getCellType()) {
	            case NUMERIC:
	                return cell.getNumericCellValue();
	                
	            case FORMULA:
	                // Évaluer le type de résultat de la formule
	                switch (cell.getCachedFormulaResultType()) {
	                    case NUMERIC:
	                        return cell.getNumericCellValue();
	                    case STRING:
	                        String stringResult = cell.getStringCellValue();
	                        // Si la formule retourne une chaîne vide, retourner 0
	                        if (stringResult == null || stringResult.trim().isEmpty()) {
	                            return 0;
	                        }
	                        // Tenter de convertir la chaîne en nombre
	                        try {
	                            return Double.parseDouble(stringResult.replace(",", "."));
	                        } catch (NumberFormatException e) {
	                            return 0;
	                        }
	                    case ERROR:
	                        return 0;
	                    default:
	                        return 0;
	                }
	                
	            case STRING:
	                String stringValue = cell.getStringCellValue();
	                if (stringValue == null || stringValue.trim().isEmpty()) {
	                    return 0;
	                }
	                try {
	                    return Double.parseDouble(stringValue.replace(",", "."));
	                } catch (NumberFormatException e) {
	                    return 0;
	                }
	                
	            default:
	                return 0;
	        }
	    } catch (Exception e) {
	        // En cas d'erreur inattendue, retourner 0
	        System.err.println("Erreur lors de la lecture de la cellule: " + e.getMessage());
	        return 0;
	    }
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
