module multiverse {
    requires javafx.controls;
    requires javafx.fxml;
    //requires javafx.web;

    requires jdk.crypto.ec;
    requires java.desktop;

    requires com.google.gson;

    opens multiverse.json to com.google.gson;
    exports multiverse.json;
    exports multiverse.managers;
    opens multiverse.managers to javafx.fxml;
    exports multiverse;
    opens multiverse to javafx.fxml;
}