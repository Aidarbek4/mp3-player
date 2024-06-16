package com.example.mp3player.windows;

import com.example.mp3player.DatabaseConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddSongWindow {

    public AddSongWindow() {
        Stage addSongStage = new Stage();
        addSongStage.setTitle("Add New Song");

        VBox addSongLayout = new VBox(10);
        addSongLayout.setPadding(new Insets(10));
        addSongLayout.setAlignment(Pos.CENTER);

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField artistIdField = new TextField();
        artistIdField.setPromptText("Artist ID");

        TextField sourceUrlField = new TextField();
        sourceUrlField.setPromptText("Source URL");

        TextField imageUrlField = new TextField();
        imageUrlField.setPromptText("Image URL");

        CheckBox albumCheckBox = new CheckBox("Album");

        Button submitButton = new Button("Add Song");
        submitButton.setOnAction(event -> {
            addNewSong(titleField.getText(), artistIdField.getText(), sourceUrlField.getText(), imageUrlField.getText(), albumCheckBox.isSelected());
            addSongStage.close();
            //onSongAdded.run();
        });

        addSongLayout.getChildren().addAll(new Label("Title:"), titleField, new Label("Artist ID:"), artistIdField, new Label("Source URL:"), sourceUrlField, new Label("Image URL:"), imageUrlField, albumCheckBox, submitButton);

        Scene scene = new Scene(addSongLayout, 300, 400);
        addSongStage.setScene(scene);
        addSongStage.show();
    }

    private void addNewSong(String title, String artistId, String sourceUrl, String imageUrl, boolean album) {
        String query = "INSERT INTO song (title, artist_id, source_url, image_url, album) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, title);
            statement.setInt(2, Integer.parseInt(artistId));
            statement.setString(3, sourceUrl);
            statement.setString(4, imageUrl);
            statement.setBoolean(5, album);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
