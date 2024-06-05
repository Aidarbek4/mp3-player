package com.example.mp3player;

import com.example.mp3player.models.Song;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MP3Player extends Application {

    private MediaPlayer mediaPlayer;
    private Button playButton;
    private Label songTitle;
    private Label artistName;
    private Label timeLabel;
    private ImageView albumArtView;
    private int currentSongIndex = -1;
    private List<Song> songs = new ArrayList<>();
    private Slider slider;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/musicDB";
    private static final String DB_USER = "root";  // Replace with your DB user
    private static final String DB_PASSWORD = "aidarbek2004";  // Replace with your DB password

    @Override
    public void start(Stage stage) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            playButton = new Button("▶");
            playButton.setStyle("-fx-background-color: #00ff00; -fx-text-fill: white; -fx-font-size: 16px;");
            playButton.setOnAction(event -> togglePlayPause());

            Button prevButton = new Button("⏮");
            prevButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;");
            prevButton.setOnAction(event -> playPreviousTrack());

            Button nextButton = new Button("⏭");
            nextButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;");
            nextButton.setOnAction(event -> playNextTrack());

            albumArtView = new ImageView();
            albumArtView.setFitHeight(300);
            albumArtView.setFitWidth(300);

            songTitle = new Label();
            songTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
            artistName = new Label();
            artistName.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");

            VBox infoBox = new VBox(5, songTitle, artistName);
            infoBox.setAlignment(Pos.CENTER);

            HBox controlBar = new HBox(10, prevButton, playButton, nextButton);
            controlBar.setAlignment(Pos.CENTER);
            controlBar.setPadding(new Insets(10));

            VBox leftBox = new VBox(10, albumArtView, infoBox, controlBar);
            leftBox.setAlignment(Pos.CENTER);
            leftBox.setPadding(new Insets(10));
            leftBox.setStyle("-fx-background-color: #000000;");

            slider = new Slider();
            slider.setMin(0);
            slider.setPrefWidth(300);

            timeLabel = new Label("0:00 / 0:00");
            timeLabel.setStyle("-fx-text-fill: white;");

            slider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (mediaPlayer != null && slider.isValueChanging()) {
                    mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
                }
            });

            HBox sliderBox = new HBox(10, slider, timeLabel);
            sliderBox.setAlignment(Pos.CENTER);
            sliderBox.setPadding(new Insets(10));
            sliderBox.setStyle("-fx-background-color: #000000;");

            leftBox.getChildren().add(sliderBox);

            TabPane tabPane = new TabPane();
            tabPane.setStyle("-fx-background-color: #000000;");

            Tab songsTab = new Tab("Songs");
            ListView<HBox> songsListView = createSongsListView(connection);
            songsTab.setContent(songsListView);
            songsTab.setClosable(false);

            Tab albumsTab = new Tab("Albums");
            albumsTab.setContent(createAlbumsListView(connection));
            albumsTab.setClosable(false);

            Tab artistsTab = new Tab("Artists");
            artistsTab.setContent(createArtistsListView(connection));
            artistsTab.setClosable(false);

            tabPane.getTabs().addAll(songsTab, albumsTab, artistsTab);

            tabPane.getStylesheets().add("data:,"
                    + ".tab-pane .tab-header-area .tab-header-background { -fx-background-color: #000000; }"
                    + ".tab-pane .tab-header-area .tab { -fx-background-color: #000000; -fx-text-fill: white; }"
                    + ".tab-pane .tab-header-area .tab:selected { -fx-background-color: #000000; -fx-text-fill: #00ff00; }");

            BorderPane root = new BorderPane();
            root.setLeft(leftBox);
            root.setCenter(tabPane);
            root.setStyle("-fx-background-color: #000000;");

            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.setTitle("MP3 Player");
            stage.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ListView<HBox> createSongsListView(Connection connection) {
        ListView<HBox> songList = new ListView<>();
        String query = "SELECT s.song_id, s.title, a.nickname AS artist, s.source_url, s.image_url, " +
                "GROUP_CONCAT(feat.nickname SEPARATOR ', ') AS featuring " +
                "FROM song s " +
                "JOIN artist a ON s.artist_id = a.artist_id " +
                "LEFT JOIN featuring f ON s.song_id = f.song_id " +
                "LEFT JOIN artist feat ON f.artist_id = feat.artist_id " +
                "GROUP BY s.song_id";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int songId = resultSet.getInt("song_id");
                String title = resultSet.getString("title");
                String artist = resultSet.getString("artist");
                String featuring = resultSet.getString("featuring");
                String sourceUrl = resultSet.getString("source_url");
                String imageUrl = resultSet.getString("image_url");

                Song song = new Song(songId, title, artist, featuring, sourceUrl, imageUrl);
                songs.add(song);

                HBox songItem = new HBox(10);
                songItem.setStyle("-fx-background-color: #000000;");
                ImageView songImageView = new ImageView(new Image(imageUrl));
                songImageView.setFitHeight(50);
                songImageView.setFitWidth(50);
                Label songLabel = new Label(title);
                songLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                Label artistLabel = new Label(featuring != null ? artist + " feat. " + featuring : artist);
                artistLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
                VBox songInfo = new VBox(5, songLabel, artistLabel);
                Button playButtonList = new Button("▶");
                playButtonList.setStyle("-fx-background-color: #00ff00; -fx-text-fill: white;");
                playButtonList.setOnAction(event -> playSong(song));
                songItem.getChildren().addAll(songImageView, songInfo, playButtonList);
                songItem.setAlignment(Pos.CENTER_LEFT);
                songList.getItems().add(songItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return songList;
    }

    private ListView<HBox> createAlbumsListView(Connection connection) {
        ListView<HBox> albumList = new ListView<>();
        String query = "SELECT album_id, title, image_url FROM album";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String imageUrl = resultSet.getString("image_url");

                HBox albumItem = new HBox(10);
                albumItem.setStyle("-fx-background-color: #000000;");
                ImageView albumImageView = new ImageView(new Image(imageUrl));
                albumImageView.setFitHeight(50);
                albumImageView.setFitWidth(50);
                Label albumLabel = new Label(title);
                albumLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                albumItem.getChildren().addAll(albumImageView, albumLabel);
                albumItem.setAlignment(Pos.CENTER_LEFT);
                albumList.getItems().add(albumItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return albumList;
    }

    private ListView<HBox> createArtistsListView(Connection connection) {
        ListView<HBox> artistList = new ListView<>();
        String query = "SELECT artist_id, nickname, image_url FROM artist";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String nickname = resultSet.getString("nickname");
                String imageUrl = resultSet.getString("image_url");

                HBox artistItem = new HBox(10);
                artistItem.setStyle("-fx-background-color: #000000;");
                ImageView artistImageView = new ImageView(new Image(imageUrl));
                artistImageView.setFitHeight(50);
                artistImageView.setFitWidth(50);
                Label artistLabel = new Label(nickname);
                artistLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                artistItem.getChildren().addAll(artistImageView, artistLabel);
                artistItem.setAlignment(Pos.CENTER_LEFT);
                artistList.getItems().add(artistItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return artistList;
    }

    private void playSong(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer = new MediaPlayer(new Media(song.getSourceUrl()));
        mediaPlayer.play();
        playButton.setText("⏸");
        songTitle.setText(song.getTitle());
        artistName.setText(song.getFeaturing() != null ? song.getArtist() + " feat. " + song.getFeaturing() : song.getArtist());
        albumArtView.setImage(new Image(song.getImageUrl()));
        currentSongIndex = songs.indexOf(song);

        // Set the slider's maximum value to the track's total duration when the media is ready
        mediaPlayer.setOnReady(() -> {
            slider.setMax(mediaPlayer.getTotalDuration().toSeconds());
        });

        // Bind the slider's value to the current time property of the media player
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!slider.isValueChanging()) {
                slider.setValue(newValue.toSeconds());
            }
            String time = String.format("%d:%02d / %d:%02d",
                    (int) newValue.toMinutes(), (int) newValue.toSeconds() % 60,
                    (int) mediaPlayer.getTotalDuration().toMinutes(), (int) mediaPlayer.getTotalDuration().toSeconds() % 60);
            timeLabel.setText(time);
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playButton.setText("▶");
            } else {
                mediaPlayer.play();
                playButton.setText("⏸");
            }
        }
    }

    private void playPreviousTrack() {
        if (currentSongIndex > 0) {
            playSong(songs.get(currentSongIndex - 1));
        } else {
            playSong(songs.get(songs.size() - 1));
        }
    }

    private void playNextTrack() {
        if (currentSongIndex < songs.size() - 1) {
            playSong(songs.get(currentSongIndex + 1));
        } else {
            playSong(songs.get(0));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
