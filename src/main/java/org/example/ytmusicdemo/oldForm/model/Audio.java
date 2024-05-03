package org.example.ytmusicdemo.oldForm.model;

import java.util.Objects;

public class Audio {

    private String id;;
    private String title;
    private String url;
    private String duration;

    public Audio(String id, String title, String url, String duration) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.duration = duration;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Audio audio = (Audio) o;
        return Objects.equals(id, audio.id) && Objects.equals(title, audio.title) && Objects.equals(url, audio.url) && Objects.equals(duration, audio.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, url, duration);
    }

    @Override
    public String toString() {
        return "Audio{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
