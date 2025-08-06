package com.Laborex.Application.Controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Laborex.Application.Service.Importation.MultiSourceExcelImportService;



@RestController
@RequestMapping("/api/import-excel")
public class ExcelImportController {

    @Autowired
    private MultiSourceExcelImportService importService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importerFichiers(@RequestParam("fichiers") List<MultipartFile> fichiers) {
        try {
            for (MultipartFile fichier : fichiers) {
                String nomFichier = fichier.getOriginalFilename();
                if (nomFichier == null || (!nomFichier.endsWith(".xls") && !nomFichier.endsWith(".xlsx"))) {
                    throw new IllegalArgumentException("Seuls les fichiers Excel (.xls ou .xlsx) sont autorisés. Fichier invalide : " + nomFichier);
                }
            }

            importService.importerFichiers(fichiers);
            return ResponseEntity.ok("Importation réussie !");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la lecture du fichier !");
        }
    }
}

