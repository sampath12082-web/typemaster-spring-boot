package com.typingtutor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ExamSubmitRequest {
    @NotNull @Min(0) private Integer wpm;
    @NotNull private Double accuracy;
    @NotNull @Min(0) private Integer timeTaken;

    public Integer getWpm() { return wpm; }
    public void setWpm(Integer wpm) { this.wpm = wpm; }
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
    public Integer getTimeTaken() { return timeTaken; }
    public void setTimeTaken(Integer timeTaken) { this.timeTaken = timeTaken; }
}
