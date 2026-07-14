package com.silkroad.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "ratings", uniqueConstraints = @UniqueConstraint(columnNames = { "advertisement_id", "buyer_id" }))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "advertisement_id")
    private Advertisement advertisement;

    @ManyToOne(optional = false)
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer score;

    @Column(length = 500)
    private String comment;

    public Rating() {
    }

    public Advertisement getAdvertisement() {
        return advertisement;
    }

    public void setAdvertisement(Advertisement advertisement) {
        this.advertisement = advertisement;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
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