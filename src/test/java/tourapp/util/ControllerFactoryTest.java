package tourapp.util;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;

import tourapp.model.location.Location;
import tourapp.model.location.LocationType;
import tourapp.model.meal.Meal;
import tourapp.model.meal.MealType;
import tourapp.model.tour.Tour;
import tourapp.model.transport.Transport;
import tourapp.model.transport.TransportType;
import tourapp.model.user.User;
import tourapp.service.location_service.LocationService;
import tourapp.service.location_service.LocationTypeService;
import tourapp.service.meal_service.MealMealTypeService;
import tourapp.service.meal_service.MealService;
import tourapp.service.meal_service.MealTypeService;
import tourapp.service.tour_service.TourService;
import tourapp.service.tour_service.TourTypeService;
import tourapp.service.user_service.UserTourService;
import tourapp.service.transport_service.TransportService;
import tourapp.service.transport_service.TransportTypeService;
import tourapp.service.user_service.UserService;
import tourapp.service.user_service.UserTypeService;
import tourapp.view.AdminPanelController;
import tourapp.view.NavigationController;
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
import tourapp.view.transport_controller.TransportController;
import tourapp.view.transport_controller.TransportEditController;
import tourapp.view.transport_controller.TransportTypeEditController;
import tourapp.view.user_controller.UserCabinetController;
import tourapp.view.user_controller.UserController;
import tourapp.view.user_controller.UserEditController;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
class ControllerFactoryTest {

    @Mock private ServiceLocator serviceLocator;
    @Mock private Stage stage;
    @Mock private SessionManager sessionManager;
    @Mock private UserService userService;
    @Mock private UserTypeService userTypeService;
    @Mock private TourService tourService;
    @Mock private LocationService locationService;
    @Mock private TourTypeService tourTypeService;
    @Mock private MealService mealService;
    @Mock private MealTypeService mealTypeService;
    @Mock private MealMealTypeService mealMealTypeService;
    @Mock private TransportService transportService;
    @Mock private TransportTypeService transportTypeService;
    @Mock private UserTourService userTourService;
    @Mock private LocationTypeService locationTypeService;
    @Mock private Tour tour;
    @Mock private Transport transport;
    @Mock private TransportType transportType;
    @Mock private Location location;
    @Mock private LocationType locationType;
    @Mock private Meal meal;
    @Mock private MealType mealType;
    @Mock private User user;

    private ControllerFactory controllerFactory;

    @BeforeAll
    static void initializeJavaFX() throws InterruptedException {
        if (!Platform.isFxApplicationThread()) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(() -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
    }

    @BeforeEach
    void setUp() {
        controllerFactory = new ControllerFactory(serviceLocator);

        lenient().when(serviceLocator.resolve(Stage.class)).thenReturn(stage);
        lenient().when(serviceLocator.resolve(SessionManager.class)).thenReturn(sessionManager);
        lenient().when(serviceLocator.resolve(UserService.class)).thenReturn(userService);
        lenient().when(serviceLocator.resolve(UserTypeService.class)).thenReturn(userTypeService);
        lenient().when(serviceLocator.resolve(TourService.class)).thenReturn(tourService);
        lenient().when(serviceLocator.resolve(LocationService.class)).thenReturn(locationService);
        lenient().when(serviceLocator.resolve(TourTypeService.class)).thenReturn(tourTypeService);
        lenient().when(serviceLocator.resolve(MealService.class)).thenReturn(mealService);
        lenient().when(serviceLocator.resolve(MealTypeService.class)).thenReturn(mealTypeService);
        lenient().when(serviceLocator.resolve(MealMealTypeService.class)).thenReturn(mealMealTypeService);
        lenient().when(serviceLocator.resolve(TransportService.class)).thenReturn(transportService);
        lenient().when(serviceLocator.resolve(TransportTypeService.class)).thenReturn(transportTypeService);
        lenient().when(serviceLocator.resolve(UserTourService.class)).thenReturn(userTourService);
        lenient().when(serviceLocator.resolve(LocationTypeService.class)).thenReturn(locationTypeService);

        lenient().when(tour.getId()).thenReturn(1);
        lenient().when(tour.getName()).thenReturn("Test Tour");

        lenient().when(transport.getId()).thenReturn(1);
        lenient().when(transport.getName()).thenReturn("Test Transport");

        lenient().when(transportType.getId()).thenReturn(1);
        lenient().when(transportType.getName()).thenReturn("Bus");

        lenient().when(location.getId()).thenReturn(1);
        lenient().when(location.getName()).thenReturn("Test Location");

        lenient().when(locationType.getId()).thenReturn(1);
        lenient().when(locationType.getName()).thenReturn("Hotel");

        lenient().when(meal.getId()).thenReturn(1);
        lenient().when(meal.getName()).thenReturn("Test Meal");

        lenient().when(mealType.getId()).thenReturn(1);
        lenient().when(mealType.getName()).thenReturn("Breakfast");
    }

    @Test
    void testCreateLoginController() {
        runOnFxThread(() -> {
            // When
            LoginController controller = controllerFactory.createLoginController();

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(UserService.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(Stage.class);
        });
    }

    @Test
    void testCreateRegisterController() {
        runOnFxThread(() -> {
            // When
            RegisterController controller = controllerFactory.createRegisterController();

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(UserService.class);
            verify(serviceLocator).resolve(UserTypeService.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(Stage.class);
        });
    }

    @Test
    void testCreateNavigationController() {
        runOnFxThread(() -> {
            // When
            NavigationController controller = controllerFactory.createNavigationController();

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
        });
    }

    @Test
    void testCreateDashboardController() {
        runOnFxThread(() -> {
            // When
            DashboardController controller = controllerFactory.createDashboardController();

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(TourService.class);
            verify(serviceLocator).resolve(LocationService.class);
            verify(serviceLocator).resolve(TourTypeService.class);
            verify(serviceLocator).resolve(MealTypeService.class);
            verify(serviceLocator).resolve(TransportService.class);
            verify(serviceLocator).resolve(UserTourService.class);
        });
    }

    @Test
    void testCreateAdminPanelController() {
        runOnFxThread(() -> {
            // When
            AdminPanelController controller = controllerFactory.createAdminPanelController();

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
        });
    }

    @Test
    void testCreateLocationController() {
        runOnFxThread(() -> {
            // When
            LocationController controller = controllerFactory.createLocationController();

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(LocationService.class);
            verify(serviceLocator).resolve(LocationTypeService.class);
        });
    }

    @Test
    void testCreateMealController() {
        runOnFxThread(() -> {
            // When
            MealController controller = controllerFactory.createMealController();

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(MealService.class);
            verify(serviceLocator).resolve(MealTypeService.class);
            verify(serviceLocator).resolve(MealMealTypeService.class);
        });
    }

    @Test
    void testCreateTransportController() {
        runOnFxThread(() -> {
            // When
            TransportController controller = controllerFactory.createTransportController();

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(TransportService.class);
            verify(serviceLocator).resolve(TransportTypeService.class);
        });
    }

    @Test
    void testCreateUserController() {
        runOnFxThread(() -> {
            // When
            UserController controller = controllerFactory.createUserController();

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(UserService.class);
            verify(serviceLocator).resolve(UserTypeService.class);
        });
    }

    @Test
    void testCreateUserEditController() {
        runOnFxThread(() -> {
            // When
            UserEditController controller = controllerFactory.createUserEditController(user);

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(UserService.class);
            verify(serviceLocator).resolve(UserTypeService.class);
        });
    }

    @Test
    void testCreateUserCabinetController() {
        runOnFxThread(() -> {
            // When
            UserCabinetController controller = controllerFactory.createUserCabinetController();

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(UserService.class);
        });
    }

    @Test
    void testCreateBookedToursController() {
        runOnFxThread(() -> {
            // When
            BookedToursController controller = controllerFactory.createBookedToursController();

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(TourService.class);
            verify(serviceLocator).resolve(UserTourService.class);
            verify(serviceLocator).resolve(UserService.class);
        });
    }

    @Test
    void testCreateTourEditController() {
        runOnFxThread(() -> {
            // When
            TourEditController controller = controllerFactory.createTourEditController(tour);

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(TourService.class);
            verify(serviceLocator).resolve(LocationService.class);
            verify(serviceLocator).resolve(TourTypeService.class);
            verify(serviceLocator).resolve(MealService.class);
            verify(serviceLocator).resolve(TransportService.class);
        });
    }

    @Test
    void testCreateTransportEditController() {
        runOnFxThread(() -> {
            // When
            TransportEditController controller = controllerFactory.createTransportEditController(transport);

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(TransportService.class);
            verify(serviceLocator).resolve(TransportTypeService.class);
        });
    }

    @Test
    void testCreateTransportTypeEditController() {
        runOnFxThread(() -> {
            // When
            TransportTypeEditController controller = controllerFactory.createTransportTypeEditController(transportType);

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(TransportTypeService.class);
        });
    }

    @Test
    void testCreateLocationEditController() {
        runOnFxThread(() -> {
            // When
            LocationEditController controller = controllerFactory.createLocationEditController(location);

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(LocationService.class);
            verify(serviceLocator).resolve(LocationTypeService.class);
        });
    }

    @Test
    void testCreateLocationTypeEditController() {
        runOnFxThread(() -> {
            // When
            LocationTypeEditController controller = controllerFactory.createLocationTypeEditController(locationType);

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(LocationTypeService.class);
        });
    }

    @Test
    void testCreateMealEditController() {
        runOnFxThread(() -> {
            // When
            MealEditController controller = controllerFactory.createMealEditController(meal);

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(MealService.class);
            verify(serviceLocator).resolve(MealTypeService.class);
        });
    }

    @Test
    void testCreateMealTypeEditController() {
        runOnFxThread(() -> {
            // When
            MealTypeEditController controller = controllerFactory.createMealTypeEditController(mealType);

            // Then
            assertNotNull(controller);
            verify(serviceLocator).resolve(Stage.class);
            verify(serviceLocator).resolve(SessionManager.class);
            verify(serviceLocator).resolve(MealTypeService.class);
        });
    }

    @Test
    void testServiceLocatorInteractionConsistency() {
        runOnFxThread(() -> {
            // Given
            reset(serviceLocator);
            when(serviceLocator.resolve(Stage.class)).thenReturn(stage);
            when(serviceLocator.resolve(SessionManager.class)).thenReturn(sessionManager);
            when(serviceLocator.resolve(UserService.class)).thenReturn(userService);

            // When
            controllerFactory.createLoginController();
            controllerFactory.createNavigationController();

            // Then
            verify(serviceLocator, times(2)).resolve(Stage.class);
            verify(serviceLocator, times(2)).resolve(SessionManager.class);
            verify(serviceLocator, times(1)).resolve(UserService.class);
        });
    }

    @Test
    void testMultipleControllerCreationWithSameParameters() {
        runOnFxThread(() -> {
            // When
            TourEditController controller1 = controllerFactory.createTourEditController(tour);
            TourEditController controller2 = controllerFactory.createTourEditController(tour);

            // Then
            assertNotNull(controller1);
            assertNotNull(controller2);
            assertNotSame(controller1, controller2); // Different instances

            // Verify service locator was called appropriate number of times
            verify(serviceLocator, times(2)).resolve(Stage.class);
            verify(serviceLocator, times(2)).resolve(SessionManager.class);
            verify(serviceLocator, times(2)).resolve(TourService.class);
        });
    }

    @Test
    void testCreateTourEditControllerWithNullTour() {
        runOnFxThread(() -> {
            assertDoesNotThrow(() -> {
                TourEditController controller = controllerFactory.createTourEditController(null);
                assertNotNull(controller);
            });
        });
    }

    @Test
    void testCreateTransportEditControllerWithNullTransport() {
        runOnFxThread(() -> {
            assertDoesNotThrow(() -> {
                TransportEditController controller = controllerFactory.createTransportEditController(null);
                assertNotNull(controller);
            });
        });
    }

    @Test
    void testCreateLocationEditControllerWithNullLocation() {
        runOnFxThread(() -> {
            assertDoesNotThrow(() -> {
                LocationEditController controller = controllerFactory.createLocationEditController(null);
                assertNotNull(controller);
            });
        });
    }

    @Test
    void testCreateMealEditControllerWithNullMeal() {
        runOnFxThread(() -> {
            assertDoesNotThrow(() -> {
                MealEditController controller = controllerFactory.createMealEditController(null);
                assertNotNull(controller);
            });
        });
    }

    private void runOnFxThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    runnable.run();
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Test was interrupted while waiting for JavaFX thread");
            }
        }
    }
}