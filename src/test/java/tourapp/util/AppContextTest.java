package tourapp.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import tourapp.dao.location_dao.*;
import tourapp.dao.meal_dao.*;
import tourapp.dao.tour_dao.*;
import tourapp.dao.transport_dao.*;
import tourapp.dao.user_dao.*;
import tourapp.service.location_service.*;
import tourapp.service.meal_service.*;
import tourapp.service.tour_service.*;
import tourapp.service.transport_service.*;
import tourapp.service.user_service.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppContextTest {

    private AppContext appContext;

    @Mock
    private ServiceLocator mockServiceLocator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create AppContext with provided ServiceLocator")
    void shouldCreateAppContextWithProvidedServiceLocator() {
        // Given
        SessionManager mockSessionManager = mock(SessionManager.class);

        try (MockedConstruction<SessionManager> mockedSessionManager =
                     mockConstruction(SessionManager.class, (mock, context) -> {
                     })) {

            // When
            AppContext context = new AppContext(mockServiceLocator);

            // Then
            assertNotNull(context);
            assertEquals(mockServiceLocator, context.getServiceLocator());
            assertNotNull(context.getSessionManager());

            verify(mockServiceLocator).register(eq(SessionManager.class), any(SessionManager.class));
        }
    }

    @Test
    void shouldCreateAppContextWithDefaultServiceLocator() {
        // When
        AppContext context = new AppContext();

        // Then
        assertNotNull(context);
        assertNotNull(context.getServiceLocator());
        assertNotNull(context.getSessionManager());
    }

    @Test
    void shouldConfigureAllServicesCorrectly() {
        // Given
        ServiceLocator realServiceLocator = new ServiceLocator();

        try (MockedConstruction<ConnectionFactory> mockedConnectionFactory =
                     mockConstruction(ConnectionFactory.class);

             MockedConstruction<UserTypeDao> mockedUserTypeDao =
                     mockConstruction(UserTypeDao.class);
             MockedConstruction<UserDao> mockedUserDao =
                     mockConstruction(UserDao.class);
             MockedConstruction<LocationDao> mockedLocationDao =
                     mockConstruction(LocationDao.class);
             MockedConstruction<LocationTypeDao> mockedLocationTypeDao =
                     mockConstruction(LocationTypeDao.class);
             MockedConstruction<MealDao> mockedMealDao =
                     mockConstruction(MealDao.class);
             MockedConstruction<MealTypeDao> mockedMealTypeDao =
                     mockConstruction(MealTypeDao.class);
             MockedConstruction<MealMealTypeDao> mockedMealMealTypeDao =
                     mockConstruction(MealMealTypeDao.class);
             MockedConstruction<TransportDao> mockedTransportDao =
                     mockConstruction(TransportDao.class);
             MockedConstruction<TransportTypeDao> mockedTransportTypeDao =
                     mockConstruction(TransportTypeDao.class);
             MockedConstruction<TourTypeDao> mockedTourTypeDao =
                     mockConstruction(TourTypeDao.class);
             MockedConstruction<TourLocationDao> mockedTourLocationDao =
                     mockConstruction(TourLocationDao.class);
             MockedConstruction<TourDao> mockedTourDao =
                     mockConstruction(TourDao.class);
             MockedConstruction<UserTourDao> mockedUserTourDao =
                     mockConstruction(UserTourDao.class)) {

            // When
            AppContext context = new AppContext(realServiceLocator);

            // Then
            assertEquals(1, mockedConnectionFactory.constructed().size());
            assertTrue(realServiceLocator.contains(ConnectionFactory.class));

            assertEquals(1, mockedUserTypeDao.constructed().size());
            assertEquals(1, mockedUserDao.constructed().size());
            assertEquals(1, mockedLocationDao.constructed().size());
            assertEquals(1, mockedLocationTypeDao.constructed().size());
            assertEquals(1, mockedMealDao.constructed().size());
            assertEquals(1, mockedMealTypeDao.constructed().size());
            assertEquals(1, mockedMealMealTypeDao.constructed().size());
            assertEquals(1, mockedTransportDao.constructed().size());
            assertEquals(1, mockedTransportTypeDao.constructed().size());
            assertEquals(1, mockedTourTypeDao.constructed().size());
            assertEquals(1, mockedTourLocationDao.constructed().size());
            assertEquals(1, mockedTourDao.constructed().size());
            assertEquals(1, mockedUserTourDao.constructed().size());

            assertTrue(realServiceLocator.contains(UserTypeDao.class));
            assertTrue(realServiceLocator.contains(UserDao.class));
            assertTrue(realServiceLocator.contains(LocationDao.class));
            assertTrue(realServiceLocator.contains(LocationTypeDao.class));
            assertTrue(realServiceLocator.contains(MealDao.class));
            assertTrue(realServiceLocator.contains(MealTypeDao.class));
            assertTrue(realServiceLocator.contains(MealMealTypeDao.class));
            assertTrue(realServiceLocator.contains(TransportDao.class));
            assertTrue(realServiceLocator.contains(TransportTypeDao.class));
            assertTrue(realServiceLocator.contains(TourTypeDao.class));
            assertTrue(realServiceLocator.contains(TourLocationDao.class));
            assertTrue(realServiceLocator.contains(TourDao.class));
            assertTrue(realServiceLocator.contains(UserTourDao.class));
        }
    }

    @Test
    void shouldRegisterAllServicesCorrectly() {
        // Given
        ServiceLocator realServiceLocator = new ServiceLocator();

        try (MockedConstruction<ConnectionFactory> mockedConnectionFactory =
                     mockConstruction(ConnectionFactory.class);

             MockedConstruction<UserTypeDao> mockedUserTypeDao =
                     mockConstruction(UserTypeDao.class);
             MockedConstruction<UserDao> mockedUserDao =
                     mockConstruction(UserDao.class);
             MockedConstruction<LocationDao> mockedLocationDao =
                     mockConstruction(LocationDao.class);
             MockedConstruction<LocationTypeDao> mockedLocationTypeDao =
                     mockConstruction(LocationTypeDao.class);
             MockedConstruction<MealDao> mockedMealDao =
                     mockConstruction(MealDao.class);
             MockedConstruction<MealTypeDao> mockedMealTypeDao =
                     mockConstruction(MealTypeDao.class);
             MockedConstruction<MealMealTypeDao> mockedMealMealTypeDao =
                     mockConstruction(MealMealTypeDao.class);
             MockedConstruction<TransportDao> mockedTransportDao =
                     mockConstruction(TransportDao.class);
             MockedConstruction<TransportTypeDao> mockedTransportTypeDao =
                     mockConstruction(TransportTypeDao.class);
             MockedConstruction<TourTypeDao> mockedTourTypeDao =
                     mockConstruction(TourTypeDao.class);
             MockedConstruction<TourLocationDao> mockedTourLocationDao =
                     mockConstruction(TourLocationDao.class);
             MockedConstruction<TourDao> mockedTourDao =
                     mockConstruction(TourDao.class);
             MockedConstruction<UserTourDao> mockedUserTourDao =
                     mockConstruction(UserTourDao.class)) {

            // When
            AppContext context = new AppContext(realServiceLocator);

            // User services
            assertTrue(realServiceLocator.contains(UserService.class));
            assertTrue(realServiceLocator.contains(UserTypeService.class));
            assertTrue(realServiceLocator.contains(UserTourService.class));

            // Location services
            assertTrue(realServiceLocator.contains(LocationService.class));
            assertTrue(realServiceLocator.contains(LocationTypeService.class));

            // Meal services
            assertTrue(realServiceLocator.contains(MealService.class));
            assertTrue(realServiceLocator.contains(MealTypeService.class));
            assertTrue(realServiceLocator.contains(MealMealTypeService.class));

            // Transport services
            assertTrue(realServiceLocator.contains(TransportService.class));
            assertTrue(realServiceLocator.contains(TransportTypeService.class));

            // Tour services
            assertTrue(realServiceLocator.contains(TourService.class));
            assertTrue(realServiceLocator.contains(TourTypeService.class));
            assertTrue(realServiceLocator.contains(TourLocationService.class));
        }
    }

    @Test
    void shouldReturnCorrectSessionManagerInstance() {
        // Given
        ServiceLocator realServiceLocator = new ServiceLocator();

        try (MockedConstruction<ConnectionFactory> mockedConnectionFactory =
                     mockConstruction(ConnectionFactory.class);
             MockedConstruction<SessionManager> mockedSessionManager =
                     mockConstruction(SessionManager.class)) {

            // When
            AppContext context = new AppContext(realServiceLocator);
            SessionManager sessionManager = context.getSessionManager();

            // Then
            assertNotNull(sessionManager);
            assertEquals(1, mockedSessionManager.constructed().size());
            assertEquals(mockedSessionManager.constructed().getFirst(), sessionManager);
        }
    }

    @Test
    void shouldReturnCorrectServiceLocatorInstance() {
        // Given
        ServiceLocator testServiceLocator = new ServiceLocator();

        // When
        AppContext context = new AppContext(testServiceLocator);

        // Then
        assertEquals(testServiceLocator, context.getServiceLocator());
    }

    @Test
    void shouldHandleSessionManagerRegistrationCorrectly() {
        // Given
        ServiceLocator spyServiceLocator = spy(new ServiceLocator());

        try (MockedConstruction<SessionManager> mockedSessionManager =
                     mockConstruction(SessionManager.class)) {

            // When
            AppContext context = new AppContext(spyServiceLocator);

            // Then
            verify(spyServiceLocator).register(eq(SessionManager.class), any(SessionManager.class));
            assertTrue(spyServiceLocator.contains(SessionManager.class));
        }
    }

    @Test
    void shouldCreateUniqueSessionManagerForEachAppContextInstance() {
        // When
        AppContext context1 = new AppContext();
        AppContext context2 = new AppContext();

        // Then
        assertNotNull(context1.getSessionManager());
        assertNotNull(context2.getSessionManager());
        assertNotSame(context1.getSessionManager(), context2.getSessionManager());
    }

    @Test
    void shouldCreateUniqueServiceLocatorForEachDefaultAppContextInstance() {
        // When
        AppContext context1 = new AppContext();
        AppContext context2 = new AppContext();

        // Then
        assertNotNull(context1.getServiceLocator());
        assertNotNull(context2.getServiceLocator());
        assertNotSame(context1.getServiceLocator(), context2.getServiceLocator());
    }

    @Test
    void shouldMaintainSameSessionManagerInstanceThroughoutLifecycle() {
        // Given
        AppContext context = new AppContext();

        // When
        SessionManager sessionManager1 = context.getSessionManager();
        SessionManager sessionManager2 = context.getSessionManager();

        // Then
        assertSame(sessionManager1, sessionManager2);
    }

    @Test
    void shouldMaintainSameServiceLocatorInstanceThroughoutLifecycle() {
        // Given
        ServiceLocator serviceLocator = new ServiceLocator();
        AppContext context = new AppContext(serviceLocator);

        // When
        ServiceLocator retrieved1 = context.getServiceLocator();
        ServiceLocator retrieved2 = context.getServiceLocator();

        // Then
        assertSame(serviceLocator, retrieved1);
        assertSame(retrieved1, retrieved2);
    }

}