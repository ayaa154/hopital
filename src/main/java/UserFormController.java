import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UserFormController {

    @FXML private Label titleLabel;
    @FXML private TextField nomField, prenomField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;

    private User user;
    private boolean editMode;
    private UserFormCallback callback;

    public void initialize() {
        roleCombo.getItems().addAll("admin", "medecin", "receptionniste", "patient");
    }

    public void setUser(User user, boolean editMode, UserFormCallback callback) {
        this.user = user;
        this.editMode = editMode;
        this.callback = callback;

        titleLabel.setText(editMode ? "Modifier l'utilisateur" : "Ajouter un utilisateur");

        if (editMode && user != null) {
            nomField.setText(user.getNom());
            prenomField.setText(user.getPrenom());
            emailField.setText(user.getEmail());
            roleCombo.setValue(user.getRole());
        }
    }

    @FXML
    public void handleSave() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String role = roleCombo.getValue();
        String password = passwordField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || role == null || (!editMode && password.isEmpty())) {
            showAlert("Veuillez remplir tous les champs.");
            return;
        }

        if (user == null) user = new User();

        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setRole(role);
        if (!password.isEmpty()) user.setMotDePasse(password);

        callback.onUserSaved(user, editMode);

        ((Stage) nomField.getScene().getWindow()).close();
    }

    @FXML
    public void handleCancel() {
        ((Stage) nomField.getScene().getWindow()).close();
    }

    private void showAlert(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }
}
