package tourapp.util;

import javafx.stage.Stage;
import tourapp.model.location.LocationType;
import tourapp.model.location.Location;
import tourapp.model.meal.Meal;
import tourapp.model.meal.MealType;
import tourapp.model.tour.Tour;
import tourapp.model.tour.TourType;
import tourapp.model.transport.Transport;
import tourapp.model.transport.TransportType;
import tourapp.model.user.User;
import tourapp.service.location_service.*;
import tourapp.service.meal_service.*;
import tourapp.service.tour_service.*;
import tourapp.service.transport_service.*;
import tourapp.service.user_service.*;
import tourapp.view.*;
import tourapp.view.auth_controller.LoginController;
import tourapp.view.auth_controller.RegisterController;
import tourapp.view.location_controller.LocationController;
import tourapp.view.location_controller.LocationEditController;
import tourapp.view.location_controller.LocationTypeEditController;
import tourapp.view.meal_controller.MealController;
import tourapp.view.meal_controller.MealEditController;
import tourapp.view.meal_controller.MealTypeEditController;
import tourapp.view.tour_controller.BookedToursController;
import tourapp.view.tour_controller.DashboardController;
import tourapp.view.tour_controller.TourEditController;
import tourapp.view.tour_controller.TourTypeEditController;
import tourapp.view.transport_controller.TransportController;
import tourapp.view.transport_controller.TransportEditController;
import tourapp.view.transport_controller.TransportTypeEditController;
import tourapp.view.user_controller.UserCabinetController;
import tourapp.view.user_controller.UserController;
import tourapp.view.user_controller.UserEditController;

public class ControllerFactory {
    private final ServiceLocator serviceLocator;

    public ControllerFactory(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public LoginController createLoginController() {
        return new LoginController(
                serviceLocator.resolve(UserService.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(Stage.class),
                this
        );
    }

    public RegisterController createRegisterController() {
        return new RegisterController(
                serviceLocator.resolve(UserService.class),
                serviceLocator.resolve(UserTypeService.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(Stage.class),
                this
        );
    }

    public NavigationController createNavigationController() {
        return new NavigationController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class)
        );
    }

    public DashboardController createDashboardController() {
        return new DashboardController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(TourService.class),
                serviceLocator.resolve(LocationService.class),
                serviceLocator.resolve(TourTypeService.class),
                serviceLocator.resolve(MealTypeService.class),
                serviceLocator.resolve(TransportService.class),
                serviceLocator.resolve(UserTourService.class),
                this
        );
    }

    public AdminPanelController createAdminPanelController() {
        return new AdminPanelController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                this);
    }

    public LocationController createLocationController() {
        return new LocationController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(LocationService.class),
                serviceLocator.resolve(LocationTypeService.class),
                this
        );
    }

    public MealController createMealController() {
        return new MealController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(MealService.class),
                serviceLocator.resolve(MealTypeService.class),
                serviceLocator.resolve(MealMealTypeService.class),
                this
        );
    }

    public TransportController createTransportController() {
        return new TransportController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(TransportService.class),
                serviceLocator.resolve(TransportTypeService.class),
                this
        );
    }

    public UserController createUserController() {
        return new UserController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(UserService.class),
                serviceLocator.resolve(UserTypeService.class),
                this
        );
    }

    public UserEditController createUserEditController(User user) {
        return new UserEditController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(UserService.class),
                serviceLocator.resolve(UserTypeService.class),
                user
        );
    }

    public UserCabinetController createUserCabinetController() {
        return new UserCabinetController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(UserService.class),
                this
        );
    }

    public BookedToursController createBookedToursController() {
        return new BookedToursController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(TourService.class),
                serviceLocator.resolve(UserTourService.class),
                serviceLocator.resolve(UserService.class),
                this
        );
    }

    public TourEditController createTourEditController(Tour tour) {
        return new TourEditController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(TourService.class),
                serviceLocator.resolve(LocationService.class),
                serviceLocator.resolve(TourTypeService.class),
                serviceLocator.resolve(MealService.class),
                serviceLocator.resolve(TransportService.class),
                tour
        );
    }

    public TourTypeEditController createTourTypeEditController(TourType tourType) {
        return new TourTypeEditController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(TourTypeService.class),
                tourType
        );
    }

    public TransportEditController createTransportEditController(Transport transport) {
        return new TransportEditController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(TransportService.class),
                serviceLocator.resolve(TransportTypeService.class),
                transport
        );
    }

    public TransportTypeEditController createTransportTypeEditController(TransportType transportType) {
        return new TransportTypeEditController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(TransportTypeService.class),
                transportType
        );
    }

    public LocationEditController createLocationEditController(Location location) {
        return new LocationEditController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(LocationService.class),
                serviceLocator.resolve(LocationTypeService.class),
                location
        );
    }

    public LocationTypeEditController createLocationTypeEditController(LocationType locationType) {
        return new LocationTypeEditController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(LocationTypeService.class),
                locationType
        );
    }

    public MealEditController createMealEditController(Meal meal) {
        return new MealEditController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(MealService.class),
                serviceLocator.resolve(MealTypeService.class),
                meal
        );
    }

    public MealTypeEditController createMealTypeEditController(MealType mealType) {
        return new MealTypeEditController(
                serviceLocator.resolve(Stage.class),
                serviceLocator.resolve(SessionManager.class),
                serviceLocator.resolve(MealTypeService.class),
                mealType
        );
    }
}