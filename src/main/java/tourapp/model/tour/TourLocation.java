package tourapp.model.tour;

import tourapp.model.location.Location;

public class TourLocation {
    private int tourId;
    private int locationId;
    private Location location;

    public TourLocation() {
    }

    public TourLocation(int tourId, int locationId) {
        this.tourId = tourId;
        this.locationId = locationId;
    }

    public TourLocation(int tourId, Location location) {
        this.tourId = tourId;
        this.locationId = location.getId();
        this.location = location;
    }

    public int getTourId() {
        return tourId;
    }
    public int getLocationId() {
        return locationId;
    }
    public Location getLocation() {
        return location;
    }

    public void setTourId(int tourId) {
        this.tourId = tourId;
    }
    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }
    public void setLocation(Location location) {
        this.location = location;
        if (location != null) {
            this.locationId = location.getId();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TourLocation that = (TourLocation) o;
        return tourId == that.tourId && locationId == that.locationId;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(tourId, locationId);
    }
}