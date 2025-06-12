package tourapp.model.tour;

import tourapp.model.location.Location;
import tourapp.model.meal.Meal;
import tourapp.model.transport.Transport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tour {
    private int id;
    private String name;
    private String description;
    private TourType type;
    private Transport transport;
    private Meal meal;
    private LocalDate startDate;
    private LocalDate endDate;
    private double price;
    private List<Location> locations;
    private boolean isActive;

    public Tour() {
        this.locations = new ArrayList<>();
        this.isActive = true;
    }

    public int getId() {
        return id;
    }
    public String getName() { return name; }
    public String getDescription() {
        return description;
    }
    public TourType getType() {
        return type;
    }
    public Transport getTransport() {
        return transport;
    }
    public Meal getMeal() {
        return meal;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public double getPrice() {
        return price;
    }
    public List<Location> getLocations() {
        return locations;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setType(TourType type) {
        this.type = type;
    }
    public void setTransport(Transport transport) {
        this.transport = transport;
    }
    public void setMeal(Meal meal) {
        this.meal = meal;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Tour tour = (Tour) obj;
        return id == tour.id &&
                Double.compare(tour.price, price) == 0 &&
                isActive == tour.isActive &&
                Objects.equals(name, tour.name) &&
                Objects.equals(description, tour.description) &&
                Objects.equals(type, tour.type) &&
                Objects.equals(transport, tour.transport) &&
                Objects.equals(meal, tour.meal) &&
                Objects.equals(startDate, tour.startDate) &&
                Objects.equals(endDate, tour.endDate) &&
                Objects.equals(locations, tour.locations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, type, transport, meal,
                startDate, endDate, price, locations, isActive);
    }

    @Override
    public String toString() {
        StringBuilder locationStr = new StringBuilder();
        if (locations != null && !locations.isEmpty()) {
            for (Location location : locations) {
                locationStr.append(location.getName()).append(", ").append(location.getCountry()).append("; ");
            }
        }

        return String.format("Tour ID: %d\nName: %s\nLocations: %s\nStart Date: %s\nEnd Date: %s\nType: %s\nTotal Price: %.2f",
                id, name, locationStr.toString(), startDate, endDate, type != null ? type.getName() : "-", price);
    }
}