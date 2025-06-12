package tourapp.util;

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

public class AppContext {

    private final ServiceLocator serviceLocator;
    private final SessionManager sessionManager;

    public AppContext(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
        this.sessionManager = new SessionManager();
        this.serviceLocator.register(SessionManager.class, sessionManager);
        configureServices();
    }

    public AppContext() {
        this(new ServiceLocator());
    }

    private void configureServices() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        serviceLocator.register(ConnectionFactory.class, connectionFactory);

        registerDaos(connectionFactory);
        registerServices();
    }

    private void registerDaos(ConnectionFactory connectionFactory) {
        // USER

        UserTypeDao userTypeDao = new UserTypeDao(connectionFactory);
        UserDao userDao = new UserDao(connectionFactory);

        serviceLocator.register(UserDao.class, userDao);
        serviceLocator.register(UserTypeDao.class, userTypeDao);

        // LOCATION
        LocationDao locationDao = new LocationDao(connectionFactory);
        LocationTypeDao locationTypeDao = new LocationTypeDao(connectionFactory);

        serviceLocator.register(LocationDao.class, locationDao);
        serviceLocator.register(LocationTypeDao.class, locationTypeDao);

        // MEAL
        MealDao mealDao = new MealDao(connectionFactory);
        MealTypeDao mealTypeDao = new MealTypeDao(connectionFactory);
        MealMealTypeDao mealMealTypeDao = new MealMealTypeDao(connectionFactory);

        serviceLocator.register(MealDao.class, mealDao);
        serviceLocator.register(MealTypeDao.class, mealTypeDao);
        serviceLocator.register(MealMealTypeDao.class, mealMealTypeDao);

        // TRANSPORT
        TransportDao transportDao = new TransportDao(connectionFactory);
        TransportTypeDao transportTypeDao = new TransportTypeDao(connectionFactory);

        serviceLocator.register(TransportDao.class, transportDao);
        serviceLocator.register(TransportTypeDao.class, transportTypeDao);

        // TOUR
        TourTypeDao tourTypeDao = new TourTypeDao(connectionFactory);
        TourLocationDao tourLocationDao = new TourLocationDao(connectionFactory, locationDao);
        TourDao tourDao = new TourDao(connectionFactory, mealDao, transportDao, tourTypeDao, tourLocationDao);
        UserTourDao userTourDao = new UserTourDao(connectionFactory, tourDao);

        serviceLocator.register(TourTypeDao.class, tourTypeDao);
        serviceLocator.register(TourLocationDao.class, tourLocationDao);
        serviceLocator.register(TourDao.class, tourDao);
        serviceLocator.register(UserTourDao.class, userTourDao);
    }

    private void registerServices() {
        // USER
        serviceLocator.register(UserService.class, new UserService(serviceLocator.resolve(UserDao.class)));
        serviceLocator.register(UserTypeService.class, new UserTypeService(serviceLocator.resolve(UserTypeDao.class)));
        serviceLocator.register(UserTourService.class, new UserTourService(serviceLocator.resolve(UserTourDao.class)));

        // LOCATION
        serviceLocator.register(LocationService.class, new LocationService(serviceLocator.resolve(LocationDao.class)));
        serviceLocator.register(LocationTypeService.class, new LocationTypeService(serviceLocator.resolve(LocationTypeDao.class)));

        // MEAL
        serviceLocator.register(MealService.class, new MealService(serviceLocator.resolve(MealDao.class)));
        serviceLocator.register(MealTypeService.class, new MealTypeService(serviceLocator.resolve(MealTypeDao.class)));
        serviceLocator.register(MealMealTypeService.class, new MealMealTypeService(serviceLocator.resolve(MealMealTypeDao.class)));

        // TRANSPORT
        serviceLocator.register(TransportService.class, new TransportService(serviceLocator.resolve(TransportDao.class)));
        serviceLocator.register(TransportTypeService.class, new TransportTypeService(serviceLocator.resolve(TransportTypeDao.class)));

        // TOUR
        serviceLocator.register(TourService.class, new TourService(serviceLocator.resolve(TourDao.class)));
        serviceLocator.register(TourTypeService.class, new TourTypeService(serviceLocator.resolve(TourTypeDao.class)));
        serviceLocator.register(TourLocationService.class, new TourLocationService(serviceLocator.resolve(TourLocationDao.class)));
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }
}
