module pto.calculator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires transitive javafx.graphics;
    requires transitive com.calendarfx.view;
    requires java.sql;

    exports gui;
    exports model;
    exports utilities;
}