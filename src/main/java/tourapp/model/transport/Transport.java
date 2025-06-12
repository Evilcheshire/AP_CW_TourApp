package tourapp.model.transport;

import java.util.Objects;

public class Transport {
    private int id;
    private String name;
    private TransportType type;
    private double pricePerPerson;

    public Transport() {
    }

    public Transport(int id, String name, TransportType type, double pricePerPerson) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.pricePerPerson = pricePerPerson;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public TransportType getType() {
        return type;
    }
    public double getPricePerPerson() {
        return pricePerPerson;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setType(TransportType type) {
        this.type = type;
    }
    public void setPricePerPerson(double pricePerPerson) {
        this.pricePerPerson = pricePerPerson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transport transport = (Transport) o;
        return id == transport.id &&
                Double.compare(transport.pricePerPerson, pricePerPerson) == 0 &&
                Objects.equals(name, transport.name) &&
                Objects.equals(type, transport.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, pricePerPerson);
    }

    @Override
    public String toString() {
        return String.format("%s\nType: %s\nPrice per person: %.2f",
                name, type != null ? type.getName() : "—", pricePerPerson);
    }
}