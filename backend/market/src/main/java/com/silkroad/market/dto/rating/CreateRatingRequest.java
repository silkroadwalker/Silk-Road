package com.silkroad.market.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateRatingRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;

    private String comment;

    public CreateRatingRequest() {
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}