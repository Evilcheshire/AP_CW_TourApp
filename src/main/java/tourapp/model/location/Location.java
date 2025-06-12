package tourapp.model.location;

import java.util.Objects;

public class Location {
    private int id;
    private String name;
    private String country;
    private String description;
    private LocationType locationType;

    public Location() {
    }

    public Location(int id, String name, String country, String description, LocationType locationType) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.description = description;
        this.locationType = locationType;
    }

    public Location(String name, String country, String description, LocationType locationType) {
        this(-1, name, country, description, locationType);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public LocationType getLocationType() { return locationType; }
    public int getLocationTypeId() { return locationType != null ? locationType.getId() : -1; }
    public String getDescription() { return description; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCountry(String country) { this.country = country; }
    public void setLocationType(LocationType locationType) { this.locationType = locationType; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return id == location.id &&
                Objects.equals(name, location.name) &&
                Objects.equals(country, location.country) &&
                Objects.equals(description, location.description) &&
                Objects.equals(locationType, location.locationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, country, description, locationType);
    }

    @Override
    public String toString() {
        return String.format("Локація: %s\nКраїна: %s\nТип: %s\nОпис: %s",
                name, country, locationType != null ? locationType.getName() : "—", description);
    }
}