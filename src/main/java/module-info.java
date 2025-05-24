module com.mnour.jfxmaze {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.prefs;
    requires javafx.graphics;

    opens com.mnour.jfxmaze to javafx.fxml;
    exports com.mnour.jfxmaze;
}