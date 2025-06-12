package tourapp.model.user;

import tourapp.model.tour.Tour;

public class UserTour {
    private int userId;
    private int tourId;
    private Tour tour;

    public UserTour() {
    }

    public UserTour(int userId, int tourId) {
        this.userId = userId;
        this.tourId = tourId;
    }

    public UserTour(int userId, Tour tour) {
        this.userId = userId;
        this.tourId = tour.getId();
        this.tour = tour;
    }

    public int getUserId() {
        return userId;
    }
    public int getTourId() {
        return tourId;
    }
    public Tour getTour() {
        return tour;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setTourId(int tourId) {
        this.tourId = tourId;
    }
    public void setTour(Tour tour) {
        this.tour = tour;
        if (tour != null) {
            this.tourId = tour.getId();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTour userTour = (UserTour) o;
        return userId == userTour.userId && tourId == userTour.tourId;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(userId, tourId);
    }
}