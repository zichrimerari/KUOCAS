module book.javafx.kenyattacatsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.desktop;
    requires org.json;
    requires org.apache.pdfbox;

    opens book.javafx.kenyattacatsystem to javafx.fxml;
    opens book.javafx.kenyattacatsystem.controllers to javafx.fxml;
    
    exports book.javafx.kenyattacatsystem;
    exports book.javafx.kenyattacatsystem.controllers;
    exports book.javafx.kenyattacatsystem.models;
}