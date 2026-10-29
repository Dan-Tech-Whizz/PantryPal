package com.pantrypal.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** A PantryPal account. One User has MANY PantryItems (One-to-Many). */
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled = true;
    
    @Column(name = "allow_purchase_suggestions", nullable = false)
    private Boolean allowPurchaseSuggestions = false;
    
    // mappedBy       -> FK is in PantryItem.user, so User is the INVERSE side.
    // cascade ALL    -> persist/update/remove a User and their items follow.
    // orphanRemoval  -> an item removed from this list is deleted from the database.
    // fetch LAZY     -> items are NOT loaded until you use the list
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PantryItem> pantryItems = new ArrayList<>();
    
    public User() { }
    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }
    // ---- Helpers keep BOTH sides of the relationship in sync ----
    public void addPantryItem(PantryItem item) {
        pantryItems.add(item);
        item.setUser(this);
    }
    public void removePantryItem(PantryItem item) {
        pantryItems.remove(item);
        item.setUser(null);
    }
    // ---- Getters and setters ----
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Boolean getNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(Boolean v) {this.notificationsEnabled = v; }
    public Boolean getAllowPurchaseSuggestions() { return allowPurchaseSuggestions; }
    public void setAllowPurchaseSuggestions(Boolean v) { this.allowPurchaseSuggestions = v; }
    public List<PantryItem> getPantryItems() { return pantryItems; }
    public void setPantryItems(List<PantryItem> pantryItems) { this.pantryItems = pantryItems; }
    
    /** Excludes 'pantryItems' and the password hash on purpose. */
    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
