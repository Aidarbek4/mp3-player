package com.example.mp3player.models;


public class Song {
    private int id;
    private String title;
    private String artist;
    private String featuring;
    private String sourceUrl;
    private String imageUrl;
    private boolean isAlbum;

    public Song(int id, String title, String artist, String featuring, String sourceUrl, String imageUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.featuring = featuring;
        this.sourceUrl = sourceUrl;
        this.imageUrl = imageUrl;
    }

    public Song(int id, String title, String artist, String featuring, String sourceUrl, String imageUrl, boolean isAlbum) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.featuring = featuring;
        this.sourceUrl = sourceUrl;
        this.imageUrl = imageUrl;
        this.isAlbum = isAlbum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getFeaturing() {
        return featuring;
    }

    public void setFeaturing(String featuring) {
        this.featuring = featuring;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAlbum() {
        return isAlbum;
    }

    public void setAlbum(boolean album) {
        isAlbum = album;
    }
}