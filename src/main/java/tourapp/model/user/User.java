package tourapp.model.user;

import org.mindrot.jbcrypt.BCrypt;
import java.util.Objects;

public class User {
    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private UserType userType;

    public User() {}

    public User(int id, String name, String email, String password, UserType userType) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = hashPassword(password);
        this.userType = userType;
    }

    public User(String name, String email, String password, UserType userType) {
        this(-1, name, email, password, userType);
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String password, String storedHash) {
        return BCrypt.checkpw(password, storedHash);
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public UserType getUserType() {
        return userType;
    }
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isAdmin() {
        return userType.getName().equalsIgnoreCase("ADMIN");
    }
    public boolean isManager() {
        return userType.getName().equalsIgnoreCase("MANAGER");
    }
    public boolean isCustomer() {
        return userType.getName().equalsIgnoreCase("CUSTOMER");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        User user = (User) obj;
        return id == user.id &&
                Objects.equals(name, user.name) &&
                Objects.equals(email, user.email) &&
                Objects.equals(passwordHash, user.passwordHash) &&
                Objects.equals(userType, user.userType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, passwordHash, userType);
    }

    @Override
    public String toString() {
        return String.format("User ID: %d\nName: %s\nEmail: %s\nUser Type: %s",
                id, name, email, userType);
    }
}