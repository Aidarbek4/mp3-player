module com.example.mp3player {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires com.zaxxer.hikari;


    opens com.example.mp3player to javafx.fxml;
    exports com.example.mp3player;
}