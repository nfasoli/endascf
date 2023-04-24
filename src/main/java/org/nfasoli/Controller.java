package org.nfasoli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class Controller implements Initializable {
    private static Logger log = LogManager.getLogger(Controller.class.getName());

    private Map<Integer, String> nomi_maschili = new HashMap<Integer, String>();
    private Map<Integer, String> nomi_femminili = new HashMap<Integer, String>();
    private Map<Integer, String> cognomi = new HashMap<Integer, String>();
    private Map<String, String> birthPlace = new HashMap<String, String>();
    private values values = null;
    private File fileName = null;
    private final static int cf_len = 16;
    Default d = new Default();

    @FXML
    private Button salva;
    @FXML
    private VBox d1;
    @FXML
    private ComboBox<String> sesso, nome, cognome;

    @FXML
    private TextField codiceFiscale, indirizzo, CAP, comune, provincia, email, telefono, cellulare;
    @FXML
    private Label comuneDiNascita, selFile;
    @FXML
    private DatePicker dataDiNascita;

    private String consonants(String s) {
        // return s.replace(/[^BCDFGHJKLMNPQRSTVWXYZ] / gi, '');
        // return Regex.Replace(s, @"[^BCDFGHJKLMNPQRSTVWXYZ]*", "");
        return s.replaceAll("[^BCDFGHJKLMNPQRSTVWXYZ]*", "");
    }

    String vowels(String s) {
        // return Regex.Replace(s, @"[^AEIOU]*", "");
        return s.replaceAll("[^AEIOU]*", "");
        // return s.replace(/[^AEIOU] / gi, '');
    }

    private String lastnameCode(String cognome) {
        String lastname = "";
        lastname = consonants(cognome);
        lastname += vowels(cognome);
        lastname += "XXX";
        lastname = lastname.substring(0, 3);

        return lastname.toUpperCase();
    }

    private String firstnameCode(String nome) {
        String name = consonants(nome);
        if (name.length() >= 4) {
            char[] c = { name.charAt(0), name.charAt(2), name.charAt(3) };
            name = new String(c);
        } else {
            name += vowels(nome);
            name += "XXX";
            name = name.substring(0, 3);
        }

        return name.toUpperCase();

    }

    private char controlChar(String lastname, String name, String date, String comune) {
        int i = 0,
                val = 0,
                maxChar = 15;
        String indexChar = "",
                lastnameCode = lastname,
                nameCode = name,
                dateCode = date,
                communeCode = comune,
                partialTaxCode = "";

        // Get the partial tax code
        partialTaxCode = lastnameCode + nameCode + dateCode + communeCode;

        for (i = 0; i < maxChar; i++) {
            indexChar = String.valueOf(partialTaxCode.charAt(i));
            if ((i % 2) != 0) {
                val += values.even.get(indexChar);
            } else {
                val += values.odd.get(indexChar);
            }
        }

        val = val % values.controlChars.length();

        return values.controlChars.charAt(val);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        {
            assert (d1 != null);
            assert (codiceFiscale != null);
            log.debug("initialize()");
            codiceFiscale.prefHeightProperty().bind(d1.heightProperty().multiply(0.8));
            codiceFiscale.textProperty()
                    .addListener((obs, oldText, newText) -> codiceFiscaleOnAction(obs, oldText, newText));
            dataDiNascita.setValue(LocalDate.now());
            values = new values();
            codiceFiscale.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > cf_len) {
                    newValue = newValue.substring(0, cf_len);
                    codiceFiscale.setText(newValue);
                }
            });
            // leggo i tre files con all'interno i nomi e cognomi
            try {
                Scanner scanner = new Scanner(new File(System.getProperty("user.dir"), "nomi_maschi.csv"));

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] values = line.split(";");
                    nomi_maschili.put(Integer.parseInt(values[0]), values[1].trim().toUpperCase());
                }

                scanner.close();
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Eccezione");
                alert.setHeaderText("Eccezione non gestita");
                alert.setContentText(e.toString());

                alert.showAndWait();

                Platform.exit();
                System.exit(2);
            }

            try {
                Scanner scanner = new Scanner(new File(System.getProperty("user.dir"), "nomi_femmine.csv"));

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] values = line.split(";");
                    nomi_femminili.put(Integer.parseInt(values[0]), values[1].trim().toUpperCase());
                }

                scanner.close();
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Eccezione");
                alert.setHeaderText("Eccezione non gestita");
                alert.setContentText(e.toString());

                alert.showAndWait();

                Platform.exit();
                System.exit(2);
            }

            try {
                Scanner scanner = new Scanner(new File(System.getProperty("user.dir"), "cognomi.csv"));

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] values = line.split(";");
                    cognomi.put(Integer.parseInt(values[0]), values[1].trim().toUpperCase());
                }

                scanner.close();
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Eccezione");
                alert.setHeaderText("Eccezione non gestita");
                alert.setContentText(e.toString());

                alert.showAndWait();

                Platform.exit();
                System.exit(2);
            }

            try {
                Scanner scanner = new Scanner(new File(System.getProperty("user.dir"), "paesi.csv"));

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] values = line.split(",");
                    birthPlace.put(values[0].trim(), values[1].trim());
                }

                scanner.close();
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Eccezione");
                alert.setHeaderText("Eccezione non gestita");
                alert.setContentText(e.toString());

                alert.showAndWait();

                Platform.exit();
                System.exit(2);
            }

            XmlMapper mapper = new XmlMapper();
            
            try {
                d = mapper.readValue(new File("./default.xml"), Default.class);
            } catch (IOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Eccezione");
                alert.setHeaderText("Eccezione non gestita");
                alert.setContentText(e.toString());

                alert.showAndWait();

            }
        }
    }

    private void createHeader(Workbook wb, Sheet sheet) {
        Row r = sheet.createRow(0);
        r.createCell(0).setCellValue("Cognome");
        r.createCell(1).setCellValue("Nome");
        r.createCell(2).setCellValue("DataNascita");
        r.createCell(3).setCellValue("Sesso");
        r.createCell(4).setCellValue("LuogoNascita");
        r.createCell(5).setCellValue("ProvinciaNascita");
        r.createCell(6).setCellValue("NumeroTessera");
        r.createCell(7).setCellValue("Indirizzo");
        r.createCell(8).setCellValue("CAP");
        r.createCell(9).setCellValue("Citta");
        r.createCell(10).setCellValue("Provincia");
        r.createCell(11).setCellValue("Email");
        r.createCell(12).setCellValue("Telefono");
        r.createCell(13).setCellValue("Cellulare");
        r.createCell(14).setCellValue("CodiceFiscale");

        DataFormat fmt = wb.createDataFormat();
        CellStyle textStyle = wb.createCellStyle();
        textStyle.setDataFormat(fmt.getFormat("@"));

        CellStyle numericStyle = wb.createCellStyle();
        numericStyle.setDataFormat(fmt.getFormat("0"));

        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(fmt.getFormat("d-mmm-yy"));
        sheet.setDefaultColumnStyle(0, textStyle);
        sheet.setDefaultColumnStyle(1, textStyle);
        sheet.setDefaultColumnStyle(2, dateStyle);
        sheet.setDefaultColumnStyle(3, textStyle);
        sheet.setDefaultColumnStyle(4, textStyle);
        sheet.setDefaultColumnStyle(5, textStyle);
        sheet.setDefaultColumnStyle(6, textStyle);
        sheet.setDefaultColumnStyle(7, textStyle);
        sheet.setDefaultColumnStyle(8, numericStyle);
        sheet.setDefaultColumnStyle(9, textStyle);
        sheet.setDefaultColumnStyle(10, textStyle);
        sheet.setDefaultColumnStyle(11, textStyle);
        sheet.setDefaultColumnStyle(12, textStyle);
        sheet.setDefaultColumnStyle(13, textStyle);
        sheet.setDefaultColumnStyle(14, textStyle);
    }

    private void addRowToSheet(Sheet sheet) {
        Row r = sheet.createRow(sheet.getLastRowNum() + 1);
        r.createCell(0).setCellValue(cognome.getValue());
        r.createCell(1).setCellValue(nome.getValue());
        r.createCell(2).setCellValue(dataDiNascita.getValue());
        r.createCell(3).setCellValue((String) sesso.getValue());
        r.createCell(4).setCellValue(comuneDiNascita.getText());
        r.createCell(5).setCellValue("");
        r.createCell(6).setCellValue(""); // tessera.getText());
        r.createCell(7).setCellValue(indirizzo.getText());
        r.createCell(8).setCellValue(CAP.getText());
        r.createCell(9).setCellValue(comune.getText());
        r.createCell(10).setCellValue(provincia.getText());
        r.createCell(11).setCellValue(email.getText());
        r.createCell(12).setCellValue(telefono.getText());
        r.createCell(13).setCellValue(cellulare.getText());
        r.createCell(14).setCellValue(codiceFiscale.getText());
    }

    @FXML
    private void onOpen(Event e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona ");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Excel Files", "*.xlsx"));
        fileName = fileChooser.showSaveDialog(((Node) e.getTarget()).getScene().getWindow());
        if (fileName != null)
            selFile.setText(fileName.getAbsolutePath());
        manageEnableDisable();
    }

    @FXML
    private void onSave(Event event) {
        Workbook wb = null; // new XSSFWorkbook();
        // prima lo apro
        // se esiste controllo la testata
        try {
            FileInputStream inputStream = new FileInputStream(fileName);

            wb = new XSSFWorkbook(inputStream);
            Sheet sheet = wb.getSheetAt(0);
            int firstRow = sheet.getFirstRowNum();
            int lastRow = sheet.getLastRowNum();

            // allora il foglio esiste ma è vuoto
            if (firstRow >= lastRow) {
                // aggiungo la testata
                createHeader(wb, sheet);
            }
            addRowToSheet(sheet);
            inputStream.close();

            FileOutputStream outputStream = new FileOutputStream(fileName);
            wb.write(outputStream);
            outputStream.close();
        } catch (FileNotFoundException ex) {
            log.info(ex);
        } catch (IOException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Eccezione");
            alert.setHeaderText("Eccezione non gestita");
            alert.setContentText(ex.toString());

            alert.showAndWait();

            log.info(ex);
        } finally {
            manageEnableDisable();
            try {
                if (wb != null) {
                    wb.close();
                    return;
                }
            } catch (IOException e) {
                log.info(e);
            }
        }
        // non esiste, quindi lo creo exnovo
        try {
            wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("Foglio 1");
            createHeader(wb, sheet);
            addRowToSheet(sheet);

            FileOutputStream outputStream = new FileOutputStream(fileName);
            wb.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Eccezione");
            alert.setHeaderText("Eccezione non gestita");
            alert.setContentText(e.toString());

            alert.showAndWait();
            log.info(e);
        } finally {
            manageEnableDisable();

            try {
                if (wb != null) {
                    wb.close();
                    return;
                }
            } catch (IOException e) {
                log.info(e);
            }
        }
    }

    private void clearText() {
        // tessera.setText("");
        indirizzo.setText("");
        CAP.setText("");
        comune.setText("");
        provincia.setText("");
        email.setText("");
        telefono.setText("");
        cellulare.setText("");
        nome.setValue("");
        nome.setItems(null);
        cognome.setValue("");
        cognome.setItems(null);
    }

    private void manageEnableDisable() {

        String r = codiceFiscale.getText().replaceAll(
                "[A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][0-9][0-9][A-Z][0-9][0-9][A-Z][0-9][0-9][0-9][A-Z0-9]",
                "");

        if (fileName != null && codiceFiscale.getText().length() > 0 && r.length() == 0) {
            nome.setDisable(false);
            cognome.setDisable(false);
            salva.setDisable(false);
            // tessera.setDisable(false);
            indirizzo.setDisable(false);
            CAP.setDisable(false);
            comune.setDisable(false);
            provincia.setDisable(false);
            email.setDisable(false);
            telefono.setDisable(false);
            cellulare.setDisable(false);
        } else if (fileName != null && r.length() > 0) {
            nome.setDisable(true);
            cognome.setDisable(true);
            salva.setDisable(true);
            // tessera.setDisable(true);
            indirizzo.setDisable(true);
            CAP.setDisable(true);
            comune.setDisable(true);
            provincia.setDisable(true);
            email.setDisable(true);
            telefono.setDisable(true);
            cellulare.setDisable(true);
        } else if (fileName == null && r.length() == 0) {
            nome.setDisable(true);
            cognome.setDisable(true);
            salva.setDisable(true);
            // tessera.setDisable(true);
            indirizzo.setDisable(true);
            CAP.setDisable(true);
            comune.setDisable(true);
            provincia.setDisable(true);
            email.setDisable(true);
            telefono.setDisable(true);
            cellulare.setDisable(true);
        } else if (fileName == null && r.length() > 0) {
            nome.setDisable(true);
            cognome.setDisable(true);
            salva.setDisable(true);
            // tessera.setDisable(true);
            indirizzo.setDisable(true);
            CAP.setDisable(true);
            comune.setDisable(true);
            provincia.setDisable(true);
            email.setDisable(true);
            telefono.setDisable(true);
            cellulare.setDisable(true);
        }
    }

    private void codiceFiscaleOnAction(ObservableValue<? extends String> obs, String oldText, String newText) {
        log.info(codiceFiscale.getText());
        // se CF ok allora:
        // 1 pulisci campi
        // 2 attiva salva
        // altrimenti
        // 2 disattiva salva
        String cf = codiceFiscale.getText().toUpperCase();
        String r = cf.replaceAll("[A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][0-9][0-9][A-Z][0-9][0-9][A-Z][0-9][0-9][0-9][A-Z0-9]",
                "");
        if (r.length() > 0) {
            manageEnableDisable();
            return;
        }

        codiceFiscale.setText(cf);
        manageEnableDisable();
        clearText();

        var day = Integer.parseInt(cf.substring(9, 11));
        var genere = 0;
        if (day >= 40) {
            genere = 1;
            day -= 40;
            sesso.getSelectionModel().select(genere);
        } else {
            genere = 0;
            sesso.getSelectionModel().select(genere);
        }

        Boolean found = false;
        ObservableList<String> lista_cognomi = FXCollections.observableArrayList();
        for (String c : cognomi.values()) {
            if (cf.substring(0, 3).compareTo(lastnameCode(c)) == 0) {
                // cognome.setText(c);
                found = true;
                lista_cognomi.add(c);
            }
        }
        if (found == false)
            cognome.setValue(cf.substring(0, 3));
        else {
            nome.setValue(lista_cognomi.get(0));
            nome.setItems(lista_cognomi);
        }
        found = false;
        ObservableList<String> lista_nomi = FXCollections.observableArrayList();
        if (genere == 0) {
            for (String c : nomi_maschili.values()) {
                log.info(firstnameCode(c));
                log.info(cf.substring(3, 6));
                if (cf.substring(3, 6).compareTo(firstnameCode(c)) == 0) {
                    found = true;
                    lista_nomi.add(c);
                }
            }
        } else {
            for (String c : nomi_femminili.values()) {
                if (cf.substring(3, 6).compareTo(firstnameCode(c)) == 0) {
                    found = true;
                    lista_nomi.add(c);
                }
            }
        }

        if (found == false)
            nome.setValue(cf.substring(3, 6));
        else {
            nome.setValue(lista_nomi.get(0));
            nome.setItems(lista_nomi);
        }
        dataDiNascita.setValue(LocalDate.of(1900 + Integer.parseInt(cf.substring(6, 8)),
                values.monthCodes.get(cf.substring(8, 9)), day));
        comuneDiNascita.setText(birthPlace.get(cf.substring(11, 15)));
        indirizzo.setText(d.indirizzo);
        CAP.setText(d.CAP);
        comune.setText(d.comune);
        provincia.setText(d.provincia);
        email.setText(d.email);
        telefono.setText(d.telefono);
        cellulare.setText(d.cellulare);

        // provinciaDiNascita.Text = "";

    }
}
