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
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.Laborex.Application.Repository.TvaRepository;
import com.Laborex.Application.Repository.VenteRepository;


@Service
public class VenteExcelImporter implements ExcelImportation {
    
    @Autowired
    private DateRepository dateRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private LaboRepository laboRepository;
    @Autowired
    private PromotionRepository promoRepository;
    @Autowired
    private VenteRepository venteRepository;
    @Autowired
    private TvaRepository tvaRepository;
    @Autowired
    private VenteBatchService venteBatchService;

    // Classe utilitaire pour la clé composite
    private static class VenteKey {
        String codeVente; // Numéro de facture
        String codeArticle;
        String codeCli;
        String codeDate;
        String codePromo;
        
        // Constructeur qui prend toutes les clés
        public VenteKey(String codeVente, String codeArticle, String codeCli, String codeDate, String codePromo) {
            this.codeVente = codeVente;
            this.codeArticle = codeArticle;
            this.codeCli = codeCli;
            this.codeDate = codeDate;
            this.codePromo = codePromo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VenteKey venteKey = (VenteKey) o;
            return Objects.equals(codeVente, venteKey.codeVente) &&
                   Objects.equals(codeArticle, venteKey.codeArticle) &&
                   Objects.equals(codeCli, venteKey.codeCli) &&
                   Objects.equals(codeDate, venteKey.codeDate) &&
                   Objects.equals(codePromo, venteKey.codePromo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(codeVente, codeArticle, codeCli, codeDate, codePromo);
        }
    }

    @Override
    public boolean supports(String filename) {
        return filename.toLowerCase().contains("base");
    }

    @Override
    public void importData(Sheet sheet) throws IOException {
        System.out.println("--- Début de l'importation par lot ---");

        // --- Phase 1: Pré-chargement des données de référence en mémoire ---
        System.out.println("1/ Pré-chargement des données existantes...");
        Map<String, Client> clientsExistants = clientRepository.findAll().stream()
                .collect(Collectors.toMap(Client::getCodeCli, Function.identity()));
        Map<String, Labo> labosExistants = laboRepository.findAll().stream()
                .collect(Collectors.toMap(Labo::getCodeLabo, Function.identity()));
        Map<String, Tva> tvasExistantes = tvaRepository.findAll().stream()
                .collect(Collectors.toMap(Tva::getCodeTva, Function.identity()));
        Map<String, Promotion> promosExistantes = promoRepository.findAll().stream()
                .collect(Collectors.toMap(Promotion::getCodePromo, Function.identity()));
        Map<String, DatePerso> datesExistantes = dateRepository.findAll().stream()
                .collect(Collectors.toMap(DatePerso::getCodeDate, Function.identity()));
        Map<String, Article> articlesExistants = articleRepository.findAll().stream()
                .collect(Collectors.toMap(Article::getCodeArticle, Function.identity()));
        
        // CORRECTION : Nouvelle Map pour vérifier l'existence des ventes avec une clé composite complète
     // CORRECTION : Utiliser une fonction de fusion pour gérer les clés en double
        Map<VenteKey, Vente> ventesExistantes = venteRepository.findAll().stream()
            .collect(Collectors.toMap(
                v -> new VenteKey(
                    v.getCodeVente(), 
                    v.getArticle().getCodeArticle(), 
                    v.getClient().getCodeCli(), 
                    v.getDate().getCodeDate(), 
                    v.getPromotion().getCodePromo()
                ),
                Function.identity(),
                (existing, replacement) -> existing // Conserver la valeur existante et ignorer la nouvelle
            ));

        System.out.println("Pré-chargement terminé.");
        
        // --- Phase 2: Collecte des nouvelles entités et des ventes ---
        System.out.println("2/ Lecture du fichier et collecte des données...");
        List<Vente> ventes = new ArrayList<>();
        List<Client> nouveauxClients = new ArrayList<>();
        List<Labo> nouveauxLabos = new ArrayList<>();
        List<Tva> nouvellesTvas = new ArrayList<>();
        List<Promotion> nouvellesPromos = new ArrayList<>();
        List<DatePerso> nouvellesDates = new ArrayList<>();
        List<Article> articlesAMettreAJour = new ArrayList<>();

        Map<String, Client> nouveauxClientsCache = new HashMap<>();
        Map<String, Labo> nouveauxLabosCache = new HashMap<>();
        Map<String, Tva> nouvellesTvasCache = new HashMap<>();
        Map<String, Promotion> nouvellesPromosCache = new HashMap<>();
        Map<String, DatePerso> nouvellesDatesCache = new HashMap<>();

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

            if (row == null || row.getCell(0) == null) {
                rowIndex++;
                continue;
            }

            // Lecture des valeurs
            String codeClient = getStringValue(row.getCell(columnIndexMap.get("NOCLI")));
            String nomClient = getStringValue(row.getCell(columnIndexMap.get("NOMCLI")));
            String codeArticle = getStringValue(row.getCell(columnIndexMap.get("NOART")));
            String codeLabo = getStringValue(row.getCell(columnIndexMap.get("LABO")));
            String nomLabo = getStringValue(row.getCell(columnIndexMap.get("NOMLAB")));
            String codeTva = getStringValue(row.getCell(columnIndexMap.get("CODTVA"))).trim();
            String codePromo = getStringValue(row.getCell(columnIndexMap.get("NOPRM")));
            String nomPromo = getStringValue(row.getCell(columnIndexMap.get("NOMPRM")));
            String typePromo = getStringValue(row.getCell(columnIndexMap.get("TYPPRM")));
            int ug = (int) getNumericValue(row.getCell(columnIndexMap.get("UGLIV")));
            String codeVente = getStringValue(row.getCell(columnIndexMap.get("NOFACL")));
            int quantiteVendu = (int) getNumericValue(row.getCell(columnIndexMap.get("QTLVCL")));
            double montantVente = getNumericValue(row.getCell(columnIndexMap.get("MTLIG")));
            int jour = (int) getNumericValue(row.getCell(columnIndexMap.get("JJFACL")));
            int mois = (int) getNumericValue(row.getCell(columnIndexMap.get("MMFACL")));
            int annee = (int) getNumericValue(row.getCell(columnIndexMap.get("AAFACL")));
            
            // Gestion de la date
            LocalDate localDate;
            try {
                localDate = LocalDate.of(annee, mois, jour);
            } catch (DateTimeException e) {
                System.err.println("Date invalide à la ligne " + (rowIndex + 1) + ": " + e.getMessage());
                rowIndex++;
                continue;
            }
            DatePerso date = getOrCreateTemps(localDate, datesExistantes, nouvellesDates, nouvellesDatesCache);

            // Gestion du Client
            Client client = getOrCreateClient(codeClient, nomClient, clientsExistants, nouveauxClients, nouveauxClientsCache);

            // Gestion du Labo
            Labo labo = getOrCreateLabo(codeLabo, nomLabo, labosExistants, nouveauxLabos, nouveauxLabosCache);

            // Gestion de la TVA
            Tva tva = getOrCreateTva(codeTva, tvasExistantes, nouvellesTvas, nouvellesTvasCache);

            // Gestion de la Promotion
            Promotion promotion = getOrCreatePromotion(codePromo, nomPromo, typePromo, ug, promosExistantes, nouvellesPromos, nouvellesPromosCache);

            // Gestion de l'Article et mise à jour
            Article article = articlesExistants.get(codeArticle);
            if (article == null) {
                System.err.println("Produit inexistant, l'article avec le code " + codeArticle + " à la ligne " + (rowIndex + 1) + " ne sera pas importé.");
                rowIndex++;
                continue;
            } else {
                if (article.getLabo() == null || !article.getLabo().equals(labo) || article.getTva() == null || !article.getTva().equals(tva)) {
                    article.setLabo(labo);
                    article.setTva(tva);
                    articlesAMettreAJour.add(article);
                }
            }
            
            // --- CORRECTION : Vérification de l'existence de la Vente avec la clé composite complète ---
            VenteKey venteKey = new VenteKey(codeVente, article.getCodeArticle(), client.getCodeCli(), date.getCodeDate(), promotion.getCodePromo());
            if (ventesExistantes.containsKey(venteKey)) {
                System.err.println("Ligne " + (rowIndex + 1) + ": Vente (facture " + codeVente + ", article " + codeArticle + ") existe déjà. La ligne sera ignorée.");
                rowIndex++;
                continue;
            }
            
            // Création de l'objet Vente
            Vente vente = new Vente();
            vente.setCodeVente(codeVente);
            vente.setQuantiteVendu(quantiteVendu);
            vente.setMontantVente(montantVente);
            vente.setClient(client);
            vente.setArticle(article);
            vente.setDate(date);
            vente.setPromotion(promotion);
            ventes.add(vente);
            
            rowIndex++;
        }

        // --- Phase 3: Insertion finale par lots dans la base de données ---
        System.out.println("3/ Début de l'insertion finale par lots...");
        
        venteBatchService.insertInBatch(nouveauxLabos);
        venteBatchService.insertInBatch(nouvellesTvas);
        venteBatchService.insertInBatch(nouveauxClients);
        venteBatchService.insertInBatch(nouvellesPromos);
        venteBatchService.insertInBatch(nouvellesDates);
        
        articleRepository.saveAll(articlesAMettreAJour);
        
        venteBatchService.insertInBatch(ventes);

        System.out.println("--- Import terminé. " + ventes.size() + " ventes importées. ---");
    }

    // --- Méthodes utilitaires (inchangées) ---
    private Tva getOrCreateTva(String codeTva, Map<String, Tva> tvasExistantes, List<Tva> nouvellesTvas, Map<String, Tva> nouvellesTvasCache) {
        Tva tva = tvasExistantes.get(codeTva);
        if (tva == null) {
            tva = nouvellesTvasCache.get(codeTva);
            if (tva == null) {
                Tva nouvelleTva = new Tva();
                nouvelleTva.setCodeTva(codeTva);
                if (codeTva.equals("7")) {
                    nouvelleTva.setTaux(20.0);
                    nouvelleTva.setNature("Complément alimentaire");
                } else {
                    nouvelleTva.setTaux(0.0);
                    nouvelleTva.setNature("Non défini");
                }
                nouvellesTvas.add(nouvelleTva);
                nouvellesTvasCache.put(codeTva, nouvelleTva);
                return nouvelleTva;
            }
        }
        return tva;
    }

    private Client getOrCreateClient(String codeClient, String nomClient, Map<String, Client> clientsExistants, List<Client> nouveauxClients, Map<String, Client> nouveauxClientsCache) {
        Client client = clientsExistants.get(codeClient);
        if (client == null) {
            client = nouveauxClientsCache.get(codeClient);
            if (client == null) {
                Client nouveauClient = new Client();
                nouveauClient.setCodeCli(codeClient);
                nouveauClient.setNomCli(nomClient);
                nouveauxClients.add(nouveauClient);
                nouveauxClientsCache.put(codeClient, nouveauClient);
                return nouveauClient;
            }
        }
        return client;
    }

    private Labo getOrCreateLabo(String codeLabo, String nomLabo, Map<String, Labo> labosExistants, List<Labo> nouveauxLabos, Map<String, Labo> nouveauxLabosCache) {
        Labo labo = labosExistants.get(codeLabo);
        if (labo == null) {
            labo = nouveauxLabosCache.get(codeLabo);
            if (labo == null) {
                Labo nouveauLabo = new Labo();
                nouveauLabo.setCodeLabo(codeLabo);
                nouveauLabo.setNomLabo(nomLabo);
                nouveauxLabos.add(nouveauLabo);
                nouveauxLabosCache.put(codeLabo, nouveauLabo);
                return nouveauLabo;
            }
        }
        return labo;
    }

    private Promotion getOrCreatePromotion(String codePromo, String nomPromo, String typePromo, int ug, Map<String, Promotion> promosExistantes, List<Promotion> nouvellesPromos, Map<String, Promotion> nouvellesPromosCache) {
        Promotion promotion = promosExistantes.get(codePromo);
        if (promotion == null) {
            promotion = nouvellesPromosCache.get(codePromo);
            if (promotion == null) {
                Promotion nouvellePromo = new Promotion();
                nouvellePromo.setCodePromo(codePromo);
                nouvellePromo.setNomPromo(nomPromo);
                nouvellePromo.setTypePromo(typePromo);
                nouvellePromo.setUgLivre(ug);
                nouvellesPromos.add(nouvellePromo);
                nouvellesPromosCache.put(codePromo, nouvellePromo);
                return nouvellePromo;
            }
        }
        return promotion;
    }

    private DatePerso getOrCreateTemps(LocalDate localDate, Map<String, DatePerso> datesExistantes, List<DatePerso> nouvellesDates, Map<String, DatePerso> nouvellesDatesCache) {
        String codeDate = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        DatePerso date = datesExistantes.get(codeDate);
        if (date == null) {
            date = nouvellesDatesCache.get(codeDate);
            if (date == null) {
                DatePerso nouvelleDate = new DatePerso();
                nouvelleDate.setCodeDate(codeDate);
                nouvelleDate.setDate(java.sql.Date.valueOf(localDate));
                nouvelleDate.setJour(localDate.getDayOfMonth());
                nouvelleDate.setMois(localDate.getMonthValue());
                nouvelleDate.setAnnee(localDate.getYear());
                nouvellesDates.add(nouvelleDate);
                nouvellesDatesCache.put(codeDate, nouvelleDate);
                return nouvelleDate;
            }
        }
        return date;
    }

    public double getNumericValue(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().replace(",", "."));
            } catch (NumberFormatException e) {
                return 0;
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
        return 2;
    }
}