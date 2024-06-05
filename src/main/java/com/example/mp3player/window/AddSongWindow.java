package com.example.mp3player.window;

import com.example.mp3player.models.Song;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddSongWindow {

    private Stage stage;
    private TextField titleField;
    private ComboBox<String> artistComboBox;
    private TextField imageUrlField;
    private String sourceUrl;
    private CheckBox albumCheckBox;

    public AddSongWindow(Stage owner, Connection connection, ListView<HBox> songsListView, Runnable onSongAdded) {
        stage = new Stage();
        stage.setTitle("Add Song");
        stage.initOwner(owner);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        Label titleLabel = new Label("Title:");
        grid.add(titleLabel, 0, 0);
        titleField = new TextField();
        grid.add(titleField, 1, 0);

        Label artistLabel = new Label("Artist:");
        grid.add(artistLabel, 0, 1);
        artistComboBox = new ComboBox<>(FXCollections.observableArrayList(getArtists(connection)));
        grid.add(artistComboBox, 1, 1);

        Label imageUrlLabel = new Label("Image URL:");
        grid.add(imageUrlLabel, 0, 2);
        imageUrlField = new TextField();
        grid.add(imageUrlField, 1, 2);

        Label albumLabel = new Label("Album:");
        grid.add(albumLabel, 0, 3);
        albumCheckBox = new CheckBox("Is this song part of an album?");
        grid.add(albumCheckBox, 1, 3);

        Button chooseFileButton = new Button("Choose MP3 File");
        grid.add(chooseFileButton, 0, 4);
        chooseFileButton.setOnAction(event -> chooseFile());

        Button addButton = new Button("Add");
        grid.add(addButton, 1, 5);
        addButton.setOnAction(event -> addSong(connection, songsListView, onSongAdded));

        Scene scene = new Scene(grid, 400, 300);
        stage.setScene(scene);
    }

    public void show() {
        stage.show();
    }

    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            sourceUrl = file.toURI().toString();
        }
    }

    private List<String> getArtists(Connection connection) {
        List<String> artists = new ArrayList<>();
        String query = "SELECT nickname FROM artist";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                artists.add(resultSet.getString("nickname"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return artists;
    }

    private void addSong(Connection connection, ListView<HBox> songsListView, Runnable onSongAdded) {
        String title = titleField.getText();
        String artist = artistComboBox.getValue();
        String imageUrl = imageUrlField.getText();
        boolean isAlbum = albumCheckBox.isSelected();

        // Retrieve artist_id based on the selected artist name
        int artistId = getArtistId(connection, artist);

        String query = "INSERT INTO song (title, artist_id, source_url, image_url, is_album) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, title);
            statement.setInt(2, artistId);
            statement.setString(3, sourceUrl);
            statement.setString(4, imageUrl);
            statement.setBoolean(5, isAlbum);
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int songId = generatedKeys.getInt(1);

                Song song = new Song(songId, title, artist, null, sourceUrl, imageUrl, isAlbum);
                HBox songItem = createSongItem(song);
                songsListView.getItems().add(songItem);

                onSongAdded.run();
                stage.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getArtistId(Connection connection, String artist) {
        String query = "SELECT artist_id FROM artist WHERE nickname = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, artist);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("artist_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return an invalid id if artist not found
    }

    private HBox createSongItem(Song song) {
        HBox songItem = new HBox(10);
        songItem.setStyle("-fx-background-color: #000000;");
        ImageView songImageView = new ImageView(new Image(song.getImageUrl()));
        songImageView.setFitHeight(50);
        songImageView.setFitWidth(50);
        Label songLabel = new Label(song.getTitle());
        songLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        Label artistLabel = new Label(song.getArtist());
        artistLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
        VBox songInfo = new VBox(5, songLabel, artistLabel);
        Button playButtonList = new Button("▶");
        playButtonList.setStyle("-fx-background-color: #00ff00; -fx-text-fill: white;");
        playButtonList.setOnAction(event -> playSong(song));
        songItem.getChildren().addAll(songImageView, songInfo, playButtonList);
        songItem.setAlignment(Pos.CENTER_LEFT);
        return songItem;
    }

    private void playSong(Song song) {
        // Implement play song logic
    }
}
