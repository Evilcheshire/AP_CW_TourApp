package tourapp.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tourapp.model.user.User;
import tourapp.util.SessionManager;

import java.io.IOException;

public class NavigationController {

    @FXML private HBox navigationBar;
    @FXML Label userInfoLabel;
    @FXML Button btnProfile;
    @FXML Button btnSearch;
    @FXML Button btnBooked;
    @FXML Button btnAdminPanel;
    @FXML Button btnLogout;
    @FXML Button btnExitApp;

    private final SessionManager sessionManager;
    private String currentPage = "";

    private Runnable onLogout;
    private Runnable onExit;
    private Runnable onSearch;
    private Runnable onBooked;
    private Runnable onAdminPanel;
    private Runnable onProfile;

    public static final String PAGE_DASHBOARD = "DASHBOARD";
    public static final String PAGE_ADMIN_PANEL = "ADMIN_PANEL";
    public static final String PAGE_BOOKED_TOURS = "BOOKED_TOURS";
    public static final String PAGE_PROFILE = "PROFILE";

    public NavigationController(Stage stage, SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public HBox createNavigationBar() {
        return createNavigationBar("");
    }

    public HBox createNavigationBar(String currentPage) {
        try {
            this.currentPage = currentPage;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tourapp/view/navigation.fxml"));
            loader.setController(this);
            HBox navBar = loader.load();
            setupNavigation();
            return navBar;
        } catch (IOException e) {
            return new HBox();
        }
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
        setupNavigation();
    }

    void setupNavigation() {
        if (sessionManager.hasActiveSession()) {
            User user = sessionManager.getCurrentSession().user();
            userInfoLabel.setText("Вітаємо, " + user.getName() + "!");
        }

        boolean isAdmin = sessionManager.hasActiveSession() && sessionManager.getCurrentSession().isAdmin();
        boolean isManager = sessionManager.hasActiveSession() && sessionManager.getCurrentSession().isManager();
        boolean isLoggedIn = sessionManager.hasActiveSession();

        boolean shouldShowAdminPanel = (isAdmin || isManager) && !PAGE_ADMIN_PANEL.equals(currentPage);
        btnAdminPanel.setVisible(shouldShowAdminPanel);
        btnAdminPanel.setManaged(shouldShowAdminPanel);

        boolean shouldShowBooked = isLoggedIn  && !PAGE_BOOKED_TOURS.equals(currentPage);
        if (isAdmin || isManager) btnBooked.setText("Заброньовані тури");
        btnBooked.setVisible(shouldShowBooked);
        btnBooked.setManaged(shouldShowBooked);

        boolean shouldShowSearch = isLoggedIn && !PAGE_DASHBOARD.equals(currentPage);
        btnSearch.setVisible(shouldShowSearch);
        btnSearch.setManaged(shouldShowSearch);

        boolean shouldShowProfile = isLoggedIn && !PAGE_PROFILE.equals(currentPage);
        btnProfile.setVisible(shouldShowProfile);
        btnProfile.setManaged(shouldShowProfile);
    }

    @FXML
    public void handleProfile() {
        onProfile.run();
    }

    @FXML
    public void handleSearch() {
        onSearch.run();
    }

    @FXML
    public void handleBooked() {
        onBooked.run();
    }

    @FXML
    public void handleAdminPanel() {
        if (!sessionManager.getCurrentSession().isAdmin() && !sessionManager.getCurrentSession().isManager()) {
            return;
        }

        onAdminPanel.run();
    }

    @FXML
    public void handleLogout() {
        sessionManager.endSession();
        onLogout.run();
    }

    @FXML
    public void handleExitApp() {
        sessionManager.endSession();
        onExit.run();
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }

    public void setOnSearch(Runnable onSearch) {
        this.onSearch = onSearch;
    }

    public void setOnBooked(Runnable onBooked) {
        this.onBooked = onBooked;
    }

    public void setOnAdminPanel(Runnable onAdminPanel) {
        this.onAdminPanel = onAdminPanel;
    }

    public void setOnProfile(Runnable onProfile) {
        this.onProfile = onProfile;
    }
}