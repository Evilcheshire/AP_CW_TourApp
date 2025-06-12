package tourapp.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.view.auth_controller.LoginController;

public class AdminPanelController extends BaseController {

    @FXML BorderPane mainLayout;
    @FXML private HBox adminNavigation;
    @FXML private VBox contentArea;
    @FXML private Label welcomeLabel;
    @FXML private Button btnTours;
    @FXML private Button btnLocations;
    @FXML private Button btnMeals;
    @FXML private Button btnTransport;
    @FXML private Button btnUsers;
    @FXML private VBox usersCard;

    private final ControllerFactory controllerFactory;

    public AdminPanelController(Stage stage,
                                SessionManager sessionManager,
                                ControllerFactory controllerFactory) {
        super(stage, sessionManager);
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void show() {
        loadAndShow("/tourapp/view/adminPanel.fxml", "TourApp - Адміністративна панель");
    }

    @FXML
    public void initialize() {
        setupAccessControls();
        initializeNavigationBar();
        setupWelcomeMessage();
    }

    void initializeNavigationBar() {
        try {
            NavigationController navigationController = controllerFactory.createNavigationController();

            navigationController.setOnLogout(() -> {
                sessionManager.endSession();
                LoginController loginController = controllerFactory.createLoginController();
                loginController.show();
            });

            navigationController.setOnExit(stage::close);

            navigationController.setOnSearch(() -> {
                controllerFactory.createDashboardController().show();
            });

            navigationController.setOnBooked(() -> {
                controllerFactory.createBookedToursController().show();
            });

            navigationController.setOnAdminPanel(() -> {
            });

            navigationController.setOnProfile(() -> {
                controllerFactory.createUserCabinetController().show();
            });

            HBox navBar = navigationController.createNavigationBar(NavigationController.PAGE_ADMIN_PANEL);
            mainLayout.setTop(navBar);
        } catch (Exception e) {
            showError("Помилка ініціалізації навігації: " + e.getMessage());
        }
    }

    void setupAccessControls() {
        btnUsers.setVisible(isAdmin());
        btnUsers.setManaged(isAdmin());
        usersCard.setVisible(isAdmin());
        usersCard.setManaged(isAdmin());
    }

    void setupWelcomeMessage() {
        String userName = sessionManager.getCurrentSession().user().getName();
        String userRole = isAdmin() ? "Адміністратор" : "Менеджер";
        welcomeLabel.setText(String.format("Вітаємо в адміністративній панелі, %s (%s)!", userName, userRole));
    }

    @FXML
    public void handleToursManagement() {
        controllerFactory.createDashboardController().show();
    }

    @FXML
    public void handleLocationsManagement() {
        controllerFactory.createLocationController().show();
    }

    @FXML
    public void handleMealsManagement() {
        controllerFactory.createMealController().show();
    }

    @FXML
    public void handleTransportManagement() {
        controllerFactory.createTransportController().show();
    }

    @FXML
    public void handleUsersManagement() {
        if (!isAdmin()) {
            showInfo("Тільки адміністратори можуть управляти користувачами");
            return;
        }
        controllerFactory.createUserController().show();
    }
}