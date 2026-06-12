package com.typingtutor.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ExamSubmitRequest {
    @NotNull @Min(0) @Max(300) private Integer wpm;
    @NotNull @DecimalMin("0.0") @DecimalMax("100.0") private Double accuracy;
    @NotNull @Min(0) @Max(7200) private Integer timeTaken;

    public Integer getWpm() { return wpm; }
    public void setWpm(Integer wpm) { this.wpm = wpm; }
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
    public Integer getTimeTaken() { return timeTaken; }
    public void setTimeTaken(Integer timeTaken) { this.timeTaken = timeTaken; }
}
