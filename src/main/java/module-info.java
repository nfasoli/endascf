module org.nfasoli {
    requires  javafx.controls;
    requires  javafx.fxml;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.logging.log4j;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;

    opens org.nfasoli to javafx.fxml, com.fasterxml.jackson.dataformat.xml, com.fasterxml.jackson.databind;
    exports org.nfasoli;
}
