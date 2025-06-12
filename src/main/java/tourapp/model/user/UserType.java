package tourapp.model.user;

public class UserType {
    private int id;
    private String name;

    public UserType() {
    }

    public UserType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserType userType = (UserType) obj;
        return id == userType.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}