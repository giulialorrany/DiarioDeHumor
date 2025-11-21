package org.example.diariodehumor.model;

public class HumorDTO {
    private String date;
    private String mood;
    private String note;

    // Getters
    public String getDate() {
        return date;
    }
    public String getMood() {
        return mood;
    }
    public String getNote() {
        return note;
    }
    // Setters
    public void setDate(String date) {this.date = date;}
    public void setMood(String mood) {
        this.mood = mood;
    }
    public void setNote(String note) {
        this.note = note;
    }
}
