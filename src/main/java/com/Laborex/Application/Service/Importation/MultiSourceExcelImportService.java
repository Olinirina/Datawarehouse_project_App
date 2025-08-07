package com.Laborex.Application.Service.Importation;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.monitorjbl.xlsx.StreamingReader;

@Service
public class MultiSourceExcelImportService {

    @Autowired
    public List<ExcelImportation> importers;

    public void importerFichiers(List<MultipartFile> fichiers) throws IOException {
        // 🧠 Préparer une liste des fichiers avec leurs importers associés
        List<ImporterFichierPair> fichiersAvecImporters = new ArrayList<>();

        for (MultipartFile file : fichiers) {
            String fileName = file.getOriginalFilename();

            ExcelImportation importer = importers.stream()
                    .filter(i -> i.supports(fileName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Aucun importer ne supporte le fichier : " + fileName));

            fichiersAvecImporters.add(new ImporterFichierPair(file, importer));
        }

        // 📊 Trier par priorité
        fichiersAvecImporters.sort(Comparator.comparingInt(pair -> pair.importer.getPriorite()));

        // 🔁 Importer chaque fichier dans l’ordre
        for (ImporterFichierPair pair : fichiersAvecImporters) {
            MultipartFile file = pair.file;
            ExcelImportation importer = pair.importer;

            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)
                    .bufferSize(4096)
                    .open(file.getInputStream());

            Sheet sheet;
            if (importer instanceof ComparaisonExcelImporter) {
                sheet = workbook.getSheet("Feuil2");
                if (sheet == null) {
                    throw new IllegalArgumentException("La feuille 'Feuil2' est introuvable dans le fichier : " + file.getOriginalFilename());
                }
            } else {
                sheet = workbook.getSheetAt(0);
            }
            

            importer.importData(sheet);
            workbook.close();
        }
    }

    // 💡 Classe interne utilitaire
    private static class ImporterFichierPair {
        MultipartFile file;
        ExcelImportation importer;

        ImporterFichierPair(MultipartFile file, ExcelImportation importer) {
            this.file = file;
            this.importer = importer;
        }
    }
}
