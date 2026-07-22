package com.silkroad.market.dto.rating;

public class RatingResponse {

    private Long id;

    private String buyerUsername;

    private Integer score;

    private String comment;

    public RatingResponse() {
    }

    public RatingResponse(
            Long id,
            String buyerUsername,
            Integer score,
            String comment) {

        this.id = id;
        this.buyerUsername = buyerUsername;
        this.score = score;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBuyerUsername() {
        return buyerUsername;
    }

    public void setBuyerUsername(String buyerUsername) {
        this.buyerUsername = buyerUsername;
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