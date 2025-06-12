package tourapp.util;

import tourapp.model.user.User;

public class SessionManager {
    private UserSession currentSession;

    public void startSession(User user) {
        currentSession = new UserSession(user);
    }

    public UserSession getCurrentSession() {
        return currentSession;
    }

    public boolean hasActiveSession() {
        return currentSession != null;
    }

    public void endSession() {
        currentSession = null;
    }

    public record UserSession(User user) {

        public void setUser(User newUser) {
        }

            public boolean isAdmin() {
                return user.isAdmin();
            }

            public boolean isManager() {
                return user.isManager();
            }

            public boolean isCustomer() {
                return user.isCustomer();
            }
        }
}
