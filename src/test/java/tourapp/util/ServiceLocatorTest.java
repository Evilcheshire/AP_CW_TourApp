package tourapp.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class ServiceLocatorTest {

    private ServiceLocator serviceLocator;

    @BeforeEach
    void setUp() {
        serviceLocator = new ServiceLocator();
    }

    @Test
    void shouldRegisterAndResolveService() {
        String testService = "Test Service";

        serviceLocator.register(String.class, testService);
        String resolved = serviceLocator.resolve(String.class);

        assertEquals(testService, resolved);
    }

    @Test
    void shouldThrowExceptionWhenResolvingUnregisteredService() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> serviceLocator.resolve(String.class)
        );

        assertEquals("No service registered for type: java.lang.String", exception.getMessage());
    }

    @Test
    void shouldReturnTrueWhenServiceIsRegistered() {
        serviceLocator.register(String.class, "test");

        boolean contains = serviceLocator.contains(String.class);

        assertTrue(contains);
    }

    @Test
    void shouldReturnFalseWhenServiceIsNotRegistered() {

        boolean contains = serviceLocator.contains(String.class);

        assertFalse(contains);
    }

    @Test
    void shouldRemoveServiceSuccessfully() {
        serviceLocator.register(String.class, "test");
        assertTrue(serviceLocator.contains(String.class));

        serviceLocator.remove(String.class);

        assertFalse(serviceLocator.contains(String.class));
    }

    @Test
    void shouldClearAllServices() {
        serviceLocator.register(String.class, "test1");
        serviceLocator.register(Integer.class, 42);

        serviceLocator.clear();

        assertFalse(serviceLocator.contains(String.class));
        assertFalse(serviceLocator.contains(Integer.class));
    }

    @Test
    void shouldOverwriteExistingService() {
        String firstService = "First";
        String secondService = "Second";

        serviceLocator.register(String.class, firstService);

        serviceLocator.register(String.class, secondService);
        String resolved = serviceLocator.resolve(String.class);

        assertEquals(secondService, resolved);
    }

    @Test
    void shouldHandleMultipleDifferentTypes() {
        String stringService = "String Service";
        Integer intService = 123;
        Boolean boolService = true;

        serviceLocator.register(String.class, stringService);
        serviceLocator.register(Integer.class, intService);
        serviceLocator.register(Boolean.class, boolService);

        assertEquals(stringService, serviceLocator.resolve(String.class));
        assertEquals(intService, serviceLocator.resolve(Integer.class));
        assertEquals(boolService, serviceLocator.resolve(Boolean.class));
    }

    interface TestInterface {
        String getName();
    }

    static class TestImplementation implements TestInterface {
        @Override
        public String getName() {
            return "TestImplementation";
        }
    }

    @Test
    void shouldHandleInterfaceImplementations() {
        TestInterface implementation = new TestImplementation();

        serviceLocator.register(TestInterface.class, implementation);
        TestInterface resolved = serviceLocator.resolve(TestInterface.class);

        assertEquals(implementation, resolved);
        assertEquals("TestImplementation", resolved.getName());
    }
}