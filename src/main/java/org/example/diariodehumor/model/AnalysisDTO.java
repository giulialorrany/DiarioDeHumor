package org.example.diariodehumor.model;

import java.util.Map;

public class AnalysisDTO {
    private double moodAvg;
    private int totalDays;
    private String bestMood;
    private double consistency;
    private Map<String,Integer> moodCount;

    // Getters
    public double getMoodAvg() {
        return moodAvg;
    }
    public int getTotalDays() {
        return totalDays;
    }
    public String getBestMood() {
        return bestMood;
    }
    public double getConsistency() {
        return consistency;
    }
    public Map<String,Integer> getMoodCount() {
        return moodCount;
    }

    // Setters
    public void setMoodAvg(double moodAvg) {
        this.moodAvg = moodAvg;
    }
    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }
    public void setBestMood(String bestMood) {
        this.bestMood = bestMood;
    }
    public void setConsistency(double consistency) {
        this.consistency = consistency;
    }
    public void setMoodCount(Map<String,Integer> maxCount) {
        this.moodCount = maxCount;
    }
}
