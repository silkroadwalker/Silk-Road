package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import com.silkroad.model.AdDetail;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

public class AdDetailsController {

    @FXML private HBox imagesBox;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label categoryCityLabel;
    @FXML private Label statusValueLabel;
    @FXML private Label rejectionReasonLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label sellerLabel;
    @FXML private Label sellerRatingLabel;
    @FXML private Label ownerNoteLabel;
    @FXML private Button favoriteButton;
    @FXML private Button messageButton;
    @FXML private Button rateButton;
    @FXML private Label statusLabel;

    private AdDetail ad;
    private boolean isFavorite;

    @FXML
    public void initialize() {
        Ad selected = SceneManager.getSelectedAd();
        if (selected == null) {
            statusLabel.setText("No ad selected.");
            hideActions();
            return;
        }

        try {
            ad = ApiClient.getAdDetails(selected.getId());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not load ad details: " + e.getMessage());
            hideActions();
            return;
        }

        render();
        loadImages();
        loadFavoriteState();
    }

    private void render() {
        titleLabel.setText(ad.getTitle());
        priceLabel.setText("$" + ad.getPrice());
        categoryCityLabel.setText(ad.getCategory() + " · " + ad.getCity());
        statusValueLabel.setText("Status: " + ad.getStatus());
        descriptionLabel.setText(ad.getDescription());

        if (ad.getRejectionReason() != null && !ad.getRejectionReason().isBlank()) {
            rejectionReasonLabel.setText("Rejection reason: " + ad.getRejectionReason());
        }

        sellerLabel.setText(ad.getSellerFullName() + " (@" + ad.getSellerUsername() + ") · " + ad.getSellerPhone());
        sellerLabel.setGraphic(UiComponents.avatar(ad.getSellerFullName(), 32));
        sellerLabel.setGraphicTextGap(10);

        Double avg = ad.getAverageRating();
        if (avg != null && avg > 0) {
            sellerRatingLabel.setText(String.format("Average rating: %.1f / 5", avg));
        } else {
            sellerRatingLabel.setText("No ratings yet.");
        }

        if (ad.isSubmitter()) {
            ownerNoteLabel.setText("This is your ad. Manage editing, deleting, or marking it sold from My Ads.");
            hideActions();
        }
    }

    private void hideActions() {
        favoriteButton.setVisible(false);
        favoriteButton.setManaged(false);
        messageButton.setVisible(false);
        messageButton.setManaged(false);
        rateButton.setVisible(false);
        rateButton.setManaged(false);
    }

    private void loadImages() {
        List<String> urls = ad.getImageUrls();
        if (urls == null || urls.isEmpty()) {
            return;
        }
        for (String url : urls) {
            try {
                byte[] bytes = ApiClient.getImageBytes(url);
                Image image = new Image(new ByteArrayInputStream(bytes));
                ImageView view = new ImageView(image);
                double fitHeight = 180;
                view.setFitHeight(fitHeight);
                view.setPreserveRatio(true);

                double renderedWidth = image.getHeight() > 0
                        ? fitHeight * (image.getWidth() / image.getHeight())
                        : fitHeight;

                Rectangle clip = new Rectangle(renderedWidth, fitHeight);
                clip.setArcWidth(14);
                clip.setArcHeight(14);
                view.setClip(clip);
                imagesBox.getChildren().add(view);
            } catch (Exception e) {
                e.printStackTrace();
                // skip broken images rather than blocking the whole page
            }
        }
    }

    private void loadFavoriteState() {
        try {
            List<Ad> favorites = ApiClient.getFavorites();
            isFavorite = favorites.stream().anyMatch(a -> a.getId().equals(ad.getId()));
            favoriteButton.setText(isFavorite ? "Remove from Favorites" : "Add to Favorites");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleToggleFavorite() {
        try {
            if (isFavorite) {
                ApiClient.removeFavorite(ad.getId());
            } else {
                ApiClient.addFavorite(ad.getId());
            }
            isFavorite = !isFavorite;
            favoriteButton.setText(isFavorite ? "Remove from Favorites" : "Add to Favorites");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not update favorites: " + e.getMessage());
        }
    }

    @FXML
    private void handleMessageSeller() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Message Seller");
        dialog.setHeaderText("Send a message about \"" + ad.getTitle() + "\"");
        dialog.setContentText("Message:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) {
            return;
        }

        try {
            ApiClient.sendMessage(ad.getId(), result.get());
            Dialogs.info("Message sent! You can continue the conversation from Messages.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not send message: " + e.getMessage());
        }
    }

    @FXML
    private void handleRateSeller() {
        TextInputDialog scoreDialog = new TextInputDialog();
        scoreDialog.setTitle("Rate Seller");
        scoreDialog.setHeaderText("Rate " + ad.getSellerFullName() + " for this ad (1-5)");
        scoreDialog.setContentText("Score:");

        Optional<String> scoreResult = scoreDialog.showAndWait();
        if (scoreResult.isEmpty() || scoreResult.get().isBlank()) {
            return;
        }

        int score;
        try {
            score = Integer.parseInt(scoreResult.get().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Score must be a whole number between 1 and 5.");
            return;
        }
        if (score < 1 || score > 5) {
            statusLabel.setText("Score must be between 1 and 5.");
            return;
        }

        TextInputDialog commentDialog = new TextInputDialog();
        commentDialog.setTitle("Rate Seller");
        commentDialog.setHeaderText("Optional comment");
        commentDialog.setContentText("Comment:");
        Optional<String> commentResult = commentDialog.showAndWait();
        String comment = commentResult.orElse(null);

        try {
            ApiClient.rateSeller(ad.getId(), score, comment);
            Dialogs.info("Thanks for rating the seller!");
            loadFavoriteState();
            ad = ApiClient.getAdDetails(ad.getId());
            render();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not submit rating: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }
}
