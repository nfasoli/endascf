module org.nfasoli {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.logging.log4j;

    opens org.nfasoli to javafx.fxml;
    exports org.nfasoli;
}
