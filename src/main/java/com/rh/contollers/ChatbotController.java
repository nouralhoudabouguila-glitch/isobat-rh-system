package com.rh.contollers;

import com.rh.services.ChatbotService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatbotController {

    @FXML
    private VBox chatContainer;

    @FXML
    private TextField txtQuestion;

    @FXML
    private Button btnEnvoyer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Label lblStatut;

    private ChatbotService chatbotService;

    @FXML
    public void initialize() {
        chatbotService = new ChatbotService();

        btnEnvoyer.setOnAction(e -> envoyerQuestion());
        txtQuestion.setOnAction(e -> envoyerQuestion());

        // Message de bienvenue personnalisé
        ajouterMessageSysteme("👋 Bonjour ! Je suis votre assistant RH.\n\n" +
                "Je suis là pour vous aider à retrouver facilement " +
                "les informations de votre base de données.\n\n" +
                "🔹 **Ce que je peux faire :**\n" +
                "• 📊 Vous donner des statistiques sur les employés\n" +
                "• 💰 Consulter les avances et retenues\n" +
                "• 🍽️ Vérifier les tickets restaurant\n" +
                "• 📄 Rechercher des factures\n" +
                "• 🤝 Suivre le recrutement\n\n" +
                "💡 **Exemples :**\n" +
                "• \"Combien d'employés dans le B2B ?\"\n" +
                "• \"Quelles sont les avances en attente ?\"\n" +
                "• \"Liste des factures impayées\"\n" +
                "• \"Qui a été embauché ce mois-ci ?\"");

        if (!chatbotService.isApiConfigured()) {
            ajouterMessageSysteme("⚠️ **Configuration requise :**\n\n" +
                    "La clé API GROQ n'est pas configurée.\n\n" +
                    "Pour utiliser le chatbot, vous devez :\n" +
                    "1. Obtenir une clé API gratuite sur console.groq.com\n" +
                    "2. La remplacer dans ChatbotService.java");
        }

        scrollPane.vvalueProperty().bind(chatContainer.heightProperty());
        txtQuestion.requestFocus();
    }

    @FXML
    private void envoyerQuestion() {
        String question = txtQuestion.getText().trim();
        if (question.isEmpty()) return;

        ajouterMessageUtilisateur(question);
        txtQuestion.clear();

        btnEnvoyer.setDisable(true);
        txtQuestion.setDisable(true);
        lblStatut.setText("🤔 Recherche en cours...");

        new Thread(() -> {
            try {
                String reponse = chatbotService.poserQuestion(question);

                Platform.runLater(() -> {
                    ajouterMessageChatbot(reponse);
                    btnEnvoyer.setDisable(false);
                    txtQuestion.setDisable(false);
                    lblStatut.setText("✅ Prêt");
                    txtQuestion.requestFocus();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    ajouterMessageChatbot("❌ Désolé, une erreur est survenue : " + e.getMessage());
                    btnEnvoyer.setDisable(false);
                    txtQuestion.setDisable(false);
                    lblStatut.setText("❌ Erreur");
                });
            }
        }).start();
    }

    private void ajouterMessageUtilisateur(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        Label label = new Label(message);
        label.setStyle("-fx-background-color: #4F6815; -fx-text-fill: white; -fx-padding: 10 16; -fx-background-radius: 18; -fx-max-width: 500; -fx-wrap-text: true;");
        label.setWrapText(true);

        messageBox.getChildren().add(label);
        chatContainer.getChildren().add(messageBox);
    }

    private void ajouterMessageChatbot(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        // Utiliser un TextArea pour les messages longs
        if (message.length() > 200) {
            TextArea textArea = new TextArea(message);
            textArea.setStyle("-fx-background-color: #F0E6DA; -fx-padding: 10 14; -fx-background-radius: 12; -fx-max-width: 500; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
            textArea.setWrapText(true);
            textArea.setEditable(false);
            textArea.setPrefHeight(Region.USE_COMPUTED_SIZE);
            textArea.setMaxHeight(300);
            textArea.setMinHeight(50);

            messageBox.getChildren().add(textArea);
        } else {
            TextFlow textFlow = new TextFlow();
            textFlow.setStyle("-fx-background-color: #F0E6DA; -fx-padding: 10 16; -fx-background-radius: 18; -fx-max-width: 500;");

            Text messageText = new Text(message);
            messageText.setStyle("-fx-fill: #1A1A1A; -fx-font-size: 13px;");
            textFlow.getChildren().add(messageText);

            messageBox.getChildren().add(textFlow);
        }

        chatContainer.getChildren().add(messageBox);
    }

    private void ajouterMessageSysteme(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(10, 10, 5, 10));

        TextFlow textFlow = new TextFlow();
        textFlow.setStyle("-fx-background-color: #FFF8E1; -fx-padding: 12 16; -fx-background-radius: 12; -fx-max-width: 550; -fx-border-color: #FFEDAB; -fx-border-radius: 12;");

        Text messageText = new Text(message);
        messageText.setStyle("-fx-fill: #333333; -fx-font-size: 13px;");
        textFlow.getChildren().add(messageText);

        messageBox.getChildren().add(textFlow);
        chatContainer.getChildren().add(messageBox);
    }

    @FXML
    private void onExemples() {
        String exemples = """
            📝 **Voici quelques questions que vous pouvez poser :**
            
            👥 **Employés :**
            • Combien d'employés y a-t-il dans l'entreprise ?
            • Liste des employés du département B2B
            • Qui a été embauché ce mois-ci ?
            • Quel est le salaire moyen des employés ?
            • Quels sont les employés avec le plus haut salaire ?
            
            💰 **Avances & Retenues :**
            • Quelles sont les avances en attente de validation ?
            • Montant total des retenues de ce mois
            • Liste des retenues par employé
            
            🍽️ **Tickets restaurant :**
            • Combien de tickets restaurant ont été distribués ce mois ?
            • Montant total des tickets restaurant
            • Employés qui ont reçu des tickets
            
            📄 **Factures :**
            • Factures en attente de paiement
            • Fournisseurs les plus utilisés
            • Montant total des factures par devise
            • Factures arrivant à échéance bientôt
            
            🤝 **Recrutement :**
            • Candidats en attente d'entretien
            • Combien de candidats ont été retenus ?
            • Liste des candidats acceptés
            • Participants à la prochaine formation
            
            💡 **Posez votre question naturellement, je comprends le langage courant !**
            """;
        ajouterMessageSysteme(exemples);
    }

    @FXML
    private void onEffacerChat() {
        chatContainer.getChildren().clear();
        ajouterMessageSysteme("🧹 Chat effacé ! Je suis prêt pour de nouvelles questions.");
    }
}