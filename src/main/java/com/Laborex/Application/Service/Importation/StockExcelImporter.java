package com.Laborex.Application.Service.Importation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Laborex.Application.Model.Article;
import com.Laborex.Application.Model.Stock;
import com.Laborex.Application.Repository.ArticleRepository;
import com.Laborex.Application.Repository.StockRepository;

@Service
public class StockExcelImporter implements ExcelImportation {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private StockRepository stockRepository;

    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().contains("stock");
    }


    @Override
    public void importData(Sheet sheet) throws IOException {
        Row headerRow = null;
        int rowIndex = 0;
        Map<String, Integer> columnIndexMap = new HashMap<>();

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

            // Lecture des valeurs
            String codeArticle = getStringValue(row.getCell(columnIndexMap.getOrDefault("NOART", -1)));
            String libelleArticle = getStringValue(row.getCell(columnIndexMap.getOrDefault("LIBART", -1)));
            Integer stockDispo = (int) getNumericValue(row.getCell(columnIndexMap.getOrDefault("STOCK", -1)));
            Integer stockCours = (int) getNumericValue(row.getCell(columnIndexMap.getOrDefault("CROUTE", -1)));

            if (codeArticle.isEmpty()) {
                rowIndex++;
                continue;
            }

            // Récupération ou création de l'article
            Article article;
            Optional<Article> articleOpt= articleRepository.findById(codeArticle);
            if(articleOpt.isPresent()) {
                article= articleOpt.get();
                // Mise à jour
                article.setLibelle(libelleArticle);
            } else {
                article= new Article();
                article.setCodeArticle(codeArticle);
                article.setLibelle(libelleArticle);
            }
            
            articleRepository.save(article);

            // Mise à jour ou création du stock
            Stock stock = stockRepository.findByArticle(article)
                    .orElseGet(() -> {
                        Stock newStock = new Stock();
                        newStock.setArticle(article);
                        return newStock;
                    });

            stock.setQuantiteStocke(stockDispo);
            stock.setCoursRoute(stockCours);

            stockRepository.save(stock);

            rowIndex++;
        }
    }


    // Méthodes utilitaires
    public double getNumericValue(Cell cell) {
        if (cell == null) return 0;

        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue().replace(",", "."));
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }

    public String getStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }
    
    @Override
    public int getPriorite() {
        return 1;
    }
}

