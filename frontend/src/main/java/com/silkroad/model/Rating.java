package com.silkroad.model;

/**
 * A single buyer rating/review left on an advertisement.
 * Field names mirror the backend's RatingResponse DTO
 * (id, buyerUsername, score, comment) so Gson can map them directly.
 */
public class Rating {
    private Long id;
    private String buyerUsername;
    private Integer score;
    private String comment;

    public Long getId() { return id; }
    public String getBuyerUsername() { return buyerUsername; }
    public Integer getScore() { return score; }
    public String getComment() { return comment; }

    public void setId(Long id) { this.id = id; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }
    public void setScore(Integer score) { this.score = score; }
    public void setComment(String comment) { this.comment = comment; }
}
