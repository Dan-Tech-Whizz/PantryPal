package com.pantrypal.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** A food group that decides the default shelf life, e.g. Dairy = 7 days. */
// @Entity -> this class is mapped to a database table.
// @Table  -> names the table explicitly so it matches the SQL script.
@Entity
@Table(name = "CATEGORY")

public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer id;
    
    @Column(name = "category_name", nullable = false, length = 50, unique = true)
    private String categoryName;
    
    @Column(name = "shelf_life_days", nullable = false)
    private Integer shelfLifeDays;
   
    // mappedBy -> the foreign key lives in Ingredient.category (inverse side).
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Ingredient> ingredients = new ArrayList<>();
   
    /** Hibernate REQUIRES a no-argument constructor. */
    public Category() { }
    public Category(String categoryName, Integer shelfLifeDays) {
        this.categoryName = categoryName;
        this.shelfLifeDays = shelfLifeDays;
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getShelfLifeDays() { return shelfLifeDays; }
    public void setShelfLifeDays(Integer shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; }
    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { 
    this.ingredients = ingredients; }

    // Excludes 'ingredients': printing a lazy collection can fail or loop. 
    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + categoryName + "',shelfLife=" + shelfLifeDays + "d}";
    }
}