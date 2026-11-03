package model;

/**
 * Abstract base class representing a human actor in the clinical system.
 * Demonstrates Core Object-Oriented Principles:
 * - Abstraction: Encapsulates shared identity attributes and abstract role contracts.
 * - Inheritance: Extended by specialized actors (Patient and Doctor).
 * - Polymorphism: Dynamic method dispatch via getRole() and getSummaryDetails().
 */
public abstract class Person {
    protected final int id;
    protected String name;

    public Person(int id, String name) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive integer.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank.");
        }
        this.id = id;
        this.name = name.trim();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank.");
        }
        this.name = name.trim();
    }

    /**
     * Polymorphic method to determine actor role in the clinical system.
     */
    public abstract String getRole();

    /**
     * Polymorphic summary description for reports and UI displays.
     */
    public abstract String getSummaryDetails();
}
