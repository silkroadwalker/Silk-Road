package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Small reusable UI building blocks shared by Home, Favorites, My Ads,
 * Conversations, and Ad Details, so the same "ad card" / "avatar" / "status
 * chip" looks and behaves identically everywhere it shows up.
 */
public final class UiComponents {

    public static final double CARD_WIDTH = 210;
    public static final double IMAGE_HEIGHT = 128;

    private UiComponents() {
    }

    /**
     * Builds a picture-forward ad card (used by Home, Favorites, My Ads grids).
     *
     * @param ad          the ad to render
     * @param showStatus  whether to show the PENDING/APPROVED/REJECTED/SOLD chip
     * @param actionRow   optional extra buttons rendered under the text (may be null)
     * @param onOpen      called when the card itself is clicked (may be null)
     */
    public static VBox buildAdCard(Ad ad, boolean showStatus, List<Node> actionRow, Runnable onOpen) {
        StackPane imageWrap = new StackPane();
        imageWrap.setPrefSize(CARD_WIDTH, IMAGE_HEIGHT);
        imageWrap.setMinSize(CARD_WIDTH, IMAGE_HEIGHT);
        imageWrap.setMaxSize(CARD_WIDTH, IMAGE_HEIGHT);
        imageWrap.getStyleClass().add("ad-card-image-wrap");

        Label placeholder = new Label("No photo yet");
        placeholder.getStyleClass().add("ad-card-meta");
        imageWrap.getChildren().add(placeholder);

        loadThumbnail(ad, imageWrap);

        Label title = new Label(ad.getTitle() != null ? ad.getTitle() : "Untitled");
        title.getStyleClass().add("ad-card-title");
        title.setWrapText(true);
        title.setMaxWidth(CARD_WIDTH - 24);

        Label price = new Label("$" + formatPrice(ad.getPrice()));
        price.getStyleClass().add("ad-card-price");

        Label meta = new Label(safe(ad.getCategory()) + " · " + safe(ad.getCity()));
        meta.getStyleClass().add("ad-card-meta");
        meta.setWrapText(true);
        meta.setMaxWidth(CARD_WIDTH - 24);

        VBox textBox = new VBox(4, title, price, meta);
        textBox.setPadding(new Insets(10, 12, 0, 12));

        if (showStatus) {
            Label chip = statusChip(ad.getStatus());
            if (chip != null) {
                textBox.getChildren().add(1, chip);
            }
        }

        VBox card = new VBox(imageWrap, textBox);
        card.getStyleClass().add("ad-card");
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.setPadding(new Insets(0, 0, 12, 0));

        if (actionRow != null && !actionRow.isEmpty()) {
            HBox actions = new HBox(6);
            actions.setAlignment(Pos.CENTER_LEFT);
            actions.setPadding(new Insets(10, 12, 0, 12));
            actions.getChildren().addAll(actionRow);
            card.getChildren().add(actions);
        }

        if (onOpen != null) {
            card.setOnMouseClicked(e -> onOpen.run());
        }

        return card;
    }

    private static void loadThumbnail(Ad ad, StackPane wrap) {
        String url = ad.getThumbnailUrl();
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            byte[] bytes = ApiClient.getImageBytes(url);
            ImageView view = new ImageView(new Image(new ByteArrayInputStream(bytes)));
            view.setFitWidth(CARD_WIDTH);
            view.setFitHeight(IMAGE_HEIGHT);
            view.setPreserveRatio(false);

            Rectangle clip = new Rectangle(CARD_WIDTH, IMAGE_HEIGHT);
            clip.setArcWidth(14);
            clip.setArcHeight(14);
            view.setClip(clip);

            wrap.getChildren().setAll(view);
        } catch (Exception e) {
            // keep the "No photo yet" placeholder if the image can't be loaded
            e.printStackTrace();
        }
    }

    public static Label statusChip(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String upper = status.toUpperCase();
        Label chip = new Label(upper.charAt(0) + upper.substring(1).toLowerCase());
        switch (upper) {
            case "PENDING":
                chip.getStyleClass().add("tag-pending");
                break;
            case "APPROVED":
                chip.getStyleClass().add("tag-approved");
                break;
            case "REJECTED":
                chip.getStyleClass().add("tag-rejected");
                break;
            case "SOLD":
                chip.getStyleClass().add("tag-sold");
                break;
            default:
                return null;
        }
        return chip;
    }

    /** Small circular avatar with the first letter of a name/username. */
    public static StackPane avatar(String name, double size) {
        Circle circle = new Circle(size / 2.0);
        circle.getStyleClass().add("avatar-circle");

        String initial = (name == null || name.isBlank()) ? "?" : name.trim().substring(0, 1).toUpperCase();
        Label label = new Label(initial);
        label.getStyleClass().add("avatar-initial");

        StackPane pane = new StackPane(circle, label);
        pane.setPrefSize(size, size);
        pane.setMinSize(size, size);
        pane.setMaxSize(size, size);
        return pane;
    }

    private static String formatPrice(double price) {
        if (price == Math.floor(price) && !Double.isInfinite(price)) {
            return String.valueOf((long) price);
        }
        return String.format("%.2f", price);
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
