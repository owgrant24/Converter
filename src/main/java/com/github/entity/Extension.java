package com.github.entity;

public enum Extension {
    MP4("mp4"),
    MKV("mkv"),
    AVI("avi"),
    MPG("mpg"),
    M2V("m2v"),
    MP3("mp3"),
    WAV("wav"),
    OGG("ogg");

    private String label;

    Extension(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
