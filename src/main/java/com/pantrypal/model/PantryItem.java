package com.pantrypal.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * One stocked item in a user's pantry (Many-to-One to User, Ingredient and 
Receipt).
 * Card colour is NOT stored: it is derived from expiryDate and today's date.
 */
@Entity
@Table(name = "PANTRY_ITEM")
public class PantryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private Receipt receipt;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ItemStatus status = ItemStatus.ACTIVE;
    
    @Column(name = "status_date")
    private LocalDate statusDate;
    
    public PantryItem() { }
    public PantryItem(LocalDate purchaseDate, LocalDate expiryDate, Integer quantity) {
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }
    public Receipt getReceipt() { return receipt; }
    public void setReceipt(Receipt receipt) { this.receipt = receipt; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
    public LocalDate getStatusDate() { return statusDate; }
    public void setStatusDate(LocalDate statusDate) { this.statusDate = statusDate; }
    
    @Override
    public String toString() {
        return "PantryItem{id=" + id + ", qty=" + quantity + ", expires=" + expiryDate + ", status=" + status + "}";
    }
 }