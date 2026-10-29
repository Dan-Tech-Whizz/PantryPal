package com.pantrypal.model;

import jakarta.persistence.*;

/** A food item that can be stocked, shopped for or used in a recipe. */
@Entity
@Table(name = "INGREDIENT")
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Integer id;
    
    @Column(name = "ingredient_name", nullable = false, length = 100, unique = true)
    private String ingredientName;

    // @ManyToOne -> many Ingredients belong to one Category. This is the OWNING sid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Ingredient() { }

    public Ingredient(String ingredientName, Category category) {
        this.ingredientName = ingredientName;
        this.category = category;
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { 
    this.ingredientName = ingredientName; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    /** Excludes 'category' to avoid lazy-loading surprises. */
    @Override
    public String toString() {
        return "Ingredient{id=" + id + ", name='" + ingredientName + "'}";
    }
}
