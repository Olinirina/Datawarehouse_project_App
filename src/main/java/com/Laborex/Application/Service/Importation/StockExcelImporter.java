package com.Laborex.Application.Service.Importation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Laborex.Application.Model.Article;
import com.Laborex.Application.Model.Stock;
import com.Laborex.Application.Repository.ArticleRepository;
import com.Laborex.Application.Repository.StockRepository;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;

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
        System.out.println("--- Début de l'importation de Stock par lot ---");

        // Phase 1 : Pré-chargement des données de référence
        System.out.println("1/ Pré-chargement des articles existants et de leur stock...");
        Map<String, Article> articlesExistants = articleRepository.findAll().stream()
                .collect(Collectors.toMap(Article::getCodeArticle, Function.identity()));
        Map<Object, Stock> stocksExistants = stockRepository.findAll().stream()
                .collect(Collectors.toMap(stock -> stock.getArticle().getCodeArticle(), Function.identity()));
        System.out.println("Pré-chargement terminé. " + articlesExistants.size() + " articles trouvés.");

        // Phase 2 : Collecte des entités à créer ou à mettre à jour
        System.out.println("2/ Lecture du fichier et collecte des données...");
        List<Article> articlesAMettreAJour = new ArrayList<>();
        List<Stock> stocksAMettreAJourOuACreer = new ArrayList<>();

        Row headerRow = null;
        int rowIndex = 0;
        Map<String, Integer> columnIndexMap = new HashMap<>();

        for (Row row : sheet) {
            if (rowIndex == 0) {
                headerRow = row;
                for (Cell cell : headerRow) {
                    columnIndexMap.put(cell.getStringCellValue().trim().toUpperCase(), cell.getColumnIndex());
                }
                rowIndex++;
                continue;
            }

            if (row == null || getStringValue(row.getCell(columnIndexMap.getOrDefault("NOART", -1))).isEmpty()) {
                rowIndex++;
                continue;
            }

            // Lecture des valeurs
            String codeArticle = getStringValue(row.getCell(columnIndexMap.getOrDefault("NOART", -1)));
            String libelleArticle = getStringValue(row.getCell(columnIndexMap.getOrDefault("LIBART", -1)));
            Integer quantiteStocke = (int) getNumericValue(row.getCell(columnIndexMap.getOrDefault("STOCK", -1)));
            Integer coursRoute = (int) getNumericValue(row.getCell(columnIndexMap.getOrDefault("CROUTE", -1)));

            // Vérification de la clé étrangère (l'Article doit exister)
            Article article = articlesExistants.get(codeArticle);
            if (article == null) {
                System.err.println("Ligne " + (rowIndex + 1) + ": Article avec le code '" + codeArticle + "' non trouvé. Le stock ne sera pas importé.");
                rowIndex++;
                continue;
            }

            // Mise à jour de l'article si nécessaire
            if (!libelleArticle.equals(article.getLibelle())) {
                article.setLibelle(libelleArticle);
                articlesAMettreAJour.add(article);
            }

            // Vérification et mise à jour/création du stock
            Stock stock = stocksExistants.get(article.getCodeArticle());
            if (stock == null) {
                // Création d'un nouveau stock
                stock = new Stock();
                stock.setArticle(article);
            }
            stock.setQuantiteStocke(quantiteStocke);
            stock.setCoursRoute(coursRoute);
            stocksAMettreAJourOuACreer.add(stock);

            rowIndex++;
        }

        // Phase 3 : Insertion/mise à jour finale par lots
        System.out.println("3/ Début de l'insertion et mise à jour finale par lots...");
        
        // Mise à jour des articles existants (si le libellé a changé)
        articleRepository.saveAll(articlesAMettreAJour);
        
        // Mise à jour ou création des stocks
        stockRepository.saveAll(stocksAMettreAJourOuACreer);
        
        System.out.println("--- Import de stocks terminé. " + stocksAMettreAJourOuACreer.size() + " stocks mis à jour/créés. ---");
    }

    // Méthodes utilitaires (inchangées)
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
        return 1;
    }
}