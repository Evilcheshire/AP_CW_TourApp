package tourapp.view.tour_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tourapp.model.location.Location;
import tourapp.model.tour.Tour;
import tourapp.model.user.User;
import tourapp.model.user.UserTour;
import tourapp.service.tour_service.TourService;
import tourapp.service.user_service.UserService;
import tourapp.service.user_service.UserTourService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.view.BaseController;
import tourapp.view.NavigationController;
import tourapp.view.auth_controller.LoginController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BookedToursController extends BaseController {

    @FXML BorderPane mainLayout;
    @FXML ScrollPane cardScrollPane;
    @FXML FlowPane bookedTourCardContainer;
    @FXML Label noBookingsLabel;
    @FXML VBox adminView;
    @FXML private TableView<BookingStatistic> bookingTable;
    @FXML private TableColumn<BookingStatistic, Integer> tourIdCol;
    @FXML private TableColumn<BookingStatistic, String> tourDescCol;
    @FXML private TableColumn<BookingStatistic, Double> tourPriceCol;
    @FXML private TableColumn<BookingStatistic, LocalDate> tourStartCol;
    @FXML private TableColumn<BookingStatistic, LocalDate> tourEndCol;
    @FXML private TableColumn<BookingStatistic, Integer> bookingCountCol;
    @FXML private TableColumn<BookingStatistic, Boolean> tourActiveCol;

    private final TourService tourService;
    private final UserTourService userTourService;
    private final UserService userService;
    private final ControllerFactory controllerFactory;

    public BookedToursController(Stage stage,
                                 SessionManager sessionManager,
                                 TourService tourService,
                                 UserTourService userTourService,
                                 UserService userService,
                                 ControllerFactory controllerFactory) {
        super(stage, sessionManager);
        this.tourService = tourService;
        this.userTourService = userTourService;
        this.userService = userService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void show() {
        loadAndShow("/tourapp/view/bookedTours.fxml", "TourApp - Заброньовані тури");
    }

    @FXML
    public void initialize() {
        initializeNavigationBar();
        setupViewBasedOnRole();
        loadBookedTours();
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
            });

            navigationController.setOnAdminPanel(() -> {
                if (!isCustomer()) {
                    controllerFactory.createAdminPanelController().show();
                } else {
                    showInfo("У вас немає прав для доступу до адміністративної панелі");
                }
            });

            navigationController.setOnProfile(() -> {
                controllerFactory.createUserCabinetController().show();
            });

            HBox navBar = navigationController.createNavigationBar(NavigationController.PAGE_BOOKED_TOURS);
            mainLayout.setTop(navBar);
        } catch (Exception e) {
            showError("Помилка ініціалізації навігації: " + e.getMessage());
            
        }
    }

    void setupViewBasedOnRole() {
        cardScrollPane.setVisible(isCustomer());
        cardScrollPane.setManaged(isCustomer());
        adminView.setVisible(!isCustomer());
        adminView.setManaged(!isCustomer());

        if (bookingTable != null && !isCustomer()) {
            initializeAdminTableColumns();
        }
    }

    private void initializeAdminTableColumns() {
        if (bookingTable == null) return;

        tourIdCol.setCellValueFactory(new PropertyValueFactory<>("tourId"));
        tourDescCol.setCellValueFactory(new PropertyValueFactory<>("tourDescription"));
        tourPriceCol.setCellValueFactory(new PropertyValueFactory<>("tourPrice"));
        tourStartCol.setCellValueFactory(new PropertyValueFactory<>("tourStartDate"));
        tourEndCol.setCellValueFactory(new PropertyValueFactory<>("tourEndDate"));
        bookingCountCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().bookingCount()).asObject());
        tourActiveCol.setCellValueFactory(new PropertyValueFactory<>("tourActive"));

        tourPriceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f грн", item));
                }
            }
        });

        tourActiveCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Активний" : "Неактивний");
                }
            }
        });

        bookingTable.setRowFactory(tv -> {
            TableRow<BookingStatistic> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    BookingStatistic statistic = row.getItem();
                    showTourBookingDetails(statistic);
                }
            });
            return row;
        });
    }

    @FXML
    public void loadBookedTours() {
        if (isCustomer()) {
            loadCustomerBookedTours();
        } else if (isAdmin() || isManager()) {
            loadBookingStatistics();
        }
    }

    void loadCustomerBookedTours() {
        try {
            int currentUserId = sessionManager.getCurrentSession().user().getId();
            List<UserTour> userTours = userTourService.findById1(currentUserId);

            if (userTours.isEmpty()) {
                bookedTourCardContainer.getChildren().clear();
                noBookingsLabel.setVisible(true);
                noBookingsLabel.setText("У вас поки немає заброньованих турів");
            } else {
                displayUserToursAsCards(userTours);
            }
        } catch (SQLException e) {
            showError("Помилка завантаження заброньованих турів: " + e.getMessage());
            
        }
    }

    @FXML
    public void loadBookingStatistics() {
        try {
            List<Tour> allTours = tourService.getAll();
            ObservableList<BookingStatistic> statistics = FXCollections.observableArrayList();

            for (Tour tour : allTours) {
                int bookingCount = userTourService.countUsersByTourId(tour.getId());
                statistics.add(new BookingStatistic(tour, bookingCount));
            }

            bookingTable.setItems(statistics);

        } catch (SQLException e) {
            showError("Помилка завантаження статистики бронювань: " + e.getMessage());
            
        }
    }

    void displayUserToursAsCards(List<UserTour> userTours) {
        if (bookedTourCardContainer == null) return;

        bookedTourCardContainer.getChildren().clear();
        noBookingsLabel.setVisible(false);

        for (UserTour userTour : userTours) {
            Tour tour = userTour.getTour();
            if (tour == null) continue;

            try {
                VBox card = createTourCard(tour, userTour);
                bookedTourCardContainer.getChildren().add(card);
            } catch (IOException e) {
                showError("Помилка завантаження картки туру: " + e.getMessage());
            }
        }
    }

    VBox createTourCard(Tour tour, UserTour userTour) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tourapp/view/tour/tourCard.fxml"));
        VBox card = loader.load();

        Label titleLabel = (Label) card.lookup("#titleLabel");
        Label priceLabel = (Label) card.lookup("#priceLabel");
        Label datesLabel = (Label) card.lookup("#datesLabel");
        Label statusLabel = (Label) card.lookup("#statusLabel");
        Button detailsButton = (Button) card.lookup("#detailsButton");
        Button cancelButton = (Button) card.lookup("#cancelButton");

        String title = tour.getDescription() != null ? tour.getDescription() : "Тур #" + tour.getId();
        titleLabel.setText(title);

        priceLabel.setText(String.format("Ціна: %.2f грн", tour.getPrice()));

        String startDate = tour.getStartDate() != null ?
                tour.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "Не вказано";
        String endDate = tour.getEndDate() != null ?
                tour.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "Не вказано";
        datesLabel.setText(String.format("Період: %s - %s", startDate, endDate));

        String statusText = "Статус: " + (tour.isActive() ? "Активний" : "Неактивний");
        statusLabel.setText(statusText);
        statusLabel.setStyle(tour.isActive() ? "-fx-text-fill: #5DB994;" : "-fx-text-fill: #D37373;");

        detailsButton.setOnAction(e -> showTourDetails(tour));
        cancelButton.setOnAction(e -> cancelBooking(userTour));

        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showTourDetails(tour);
            }
        });

        return card;
    }

    void showTourDetails(Tour tour) {
        try {
            Tour tourWithDetails = tour;
            try {
                tourWithDetails = (Tour) tourService.getClass().getMethod("getByIdWithDependencies", int.class)
                        .invoke(tourService, tour.getId());
            } catch (Exception ignored) {
                tourWithDetails = tourService.getById(tour.getId());
            }

            if (tourWithDetails == null) {
                tourWithDetails = tour;
            }

            StringBuilder details = new StringBuilder();
            details.append("ID туру: ").append(tourWithDetails.getId()).append("\n");
            details.append("Опис: ").append(tourWithDetails.getDescription() != null ?
                    tourWithDetails.getDescription() : "Не вказано").append("\n");
            details.append("Ціна: ").append(String.format("%.2f грн", tourWithDetails.getPrice())).append("\n");

            String startDate = tourWithDetails.getStartDate() != null ?
                    tourWithDetails.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "Не вказано";
            String endDate = tourWithDetails.getEndDate() != null ?
                    tourWithDetails.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "Не вказано";

            details.append("Період: ").append(startDate).append(" - ").append(endDate).append("\n");
            details.append("Статус: ").append(tourWithDetails.isActive() ? "Активний" : "Неактивний").append("\n");

            if (tourWithDetails.getType() != null) {
                details.append("Тип туру: ").append(tourWithDetails.getType().getName()).append("\n");
            }

            if (tourWithDetails.getMeal() != null) {
                details.append("Харчування: ").append(tourWithDetails.getMeal().getName()).append("\n");
            }

            if (tourWithDetails.getTransport() != null) {
                details.append("Транспорт: ").append(tourWithDetails.getTransport().getName()).append("\n");
            }

            try {
                @SuppressWarnings("unchecked")
                List<Location> tourLocations = (List<Location>) tourService.getClass()
                        .getMethod("getLocationsForTour", int.class)
                        .invoke(tourService, tourWithDetails.getId());

                if (tourLocations != null && !tourLocations.isEmpty()) {
                    details.append("Локації: ");
                    String locations = tourLocations.stream()
                            .map(location -> location.getName() + " (" + location.getCountry() + ")")
                            .collect(Collectors.joining(", "));
                    details.append(locations);
                }
            } catch (Exception ignored) {
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            alert.setTitle("Деталі туру");
            alert.setHeaderText("Тур #" + tourWithDetails.getId());
            alert.setContentText(details.toString());
            alert.getDialogPane().setPrefSize(400, 300);
            alert.setResizable(true);
            alert.showAndWait();

        } catch (SQLException e) {
            showError("Помилка при отриманні деталей туру: " + e.getMessage());
        }
    }

    void showTourBookingDetails(BookingStatistic statistic) {
        try {
            List<UserTour> userTours = userTourService.findById2(statistic.getTourId());

            StringBuilder details = new StringBuilder();
            details.append("Тур: ").append(statistic.getTourDescription()).append("\n");
            details.append("Ціна: ").append(String.format("%.2f грн", statistic.getTourPrice())).append("\n");

            String startDate = statistic.getTourStartDate() != null ?
                    statistic.getTourStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "Не вказано";
            String endDate = statistic.getTourEndDate() != null ?
                    statistic.getTourEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "Не вказано";

            details.append("Період: ").append(startDate).append(" - ").append(endDate).append("\n");
            details.append("Статус: ").append(statistic.getTourActive() ? "Активний" : "Неактивний").append("\n");
            details.append("Кількість бронювань: ").append(statistic.bookingCount()).append("\n\n");

            details.append("Користувачі, що забронювали:\n");
            if (userTours.isEmpty()) {
                details.append("Немає бронювань\n");
            } else {
                for (int i = 0; i < userTours.size(); i++) {
                    UserTour userTour = userTours.get(i);
                    try {
                        User user = userService.getById(userTour.getUserId());
                        if (user != null) {
                            details.append(String.format("%d. %s (%s)\n",
                                    i + 1, user.getName(), user.getEmail()));
                        } else {
                            details.append(String.format("%d. Користувач ID: %d (не знайдено)\n",
                                    i + 1, userTour.getUserId()));
                            }
                    } catch (SQLException e) {
                        logger.error("Помилка при завантаженні користувача ID {}: {}", userTour.getUserId(), e.getMessage());
                        details.append(String.format("%d. Користувач ID: %d (помилка завантаження даних)\n",
                                i + 1, userTour.getUserId()));
                    }
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Деталі бронювань туру");
            alert.setHeaderText("Тур #" + statistic.getTourId());
            alert.setContentText(details.toString());
            alert.getDialogPane().setPrefSize(500, 400);
            alert.setResizable(true);
            alert.showAndWait();

        } catch (SQLException e) {
            showError("Помилка при отриманні деталей бронювань: " + e.getMessage());
            
        }
    }

    void cancelBooking(UserTour userTour) {
        String tourName = userTour.getTour() != null && userTour.getTour().getDescription() != null ?
                userTour.getTour().getDescription() : "Тур #" + userTour.getTourId();

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Ви впевнені, що хочете скасувати бронювання туру '" + tourName + "'?",
                ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait();

        if (confirmAlert.getResult() == ButtonType.YES) {
            try {
                int userId = sessionManager.getCurrentSession().user().getId();
                int tourId = userTour.getTour() != null ? userTour.getTour().getId() : userTour.getTourId();

                boolean success = userTourService.deleteLink(userId, tourId);

                if (success) {
                    showInfo("Бронювання успішно скасовано.");
                    logger.info("Скасовано бронювання: ID користувача {}, ID туру {}", userId, tourId);
                    loadCustomerBookedTours();
                } else {
                    showError("Не вдалося скасувати бронювання.");
                }
            } catch (SQLException e) {
                showError("Помилка скасування бронювання: " + e.getMessage());
                
            }
        }
    }

    public static class BookingStatistic {
        private final Tour tour;
        private final int bookingCount;

        public BookingStatistic(Tour tour, int bookingCount) {
            this.tour = tour;
            this.bookingCount = bookingCount;
        }

        public Tour getTour() {
            return tour;
        }

        public int getBookingCount() {
            return bookingCount;
        }

        public int getTourId() {
            return tour.getId();
        }

        public String getTourDescription() {
            return tour.getDescription() != null ? tour.getDescription() : "Тур #" + tour.getId();
        }

        public double getTourPrice() {
            return tour.getPrice();
        }

        public LocalDate getTourStartDate() {
            return tour.getStartDate();
        }

        public LocalDate getTourEndDate() {
            return tour.getEndDate();
        }

        public boolean getTourActive() {
            return tour.isActive();
        }

        public int bookingCount() {
            return bookingCount;
        }
    }
}