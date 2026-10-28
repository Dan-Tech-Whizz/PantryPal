package com.pantrypal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** A scanned shopping receipt uploaded by a User. */
@Entity
@Table(name = "RECEIPT")
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt;
    
    @Column(name = "image_path", length = 255)
    private String imagePath;
    
    public Receipt() { }
    public Receipt(User user, LocalDateTime scannedAt, String imagePath) {
        this.user = user;
        this.scannedAt = scannedAt;
        this.imagePath = imagePath;
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }
    public String getImagePath() { return imagePath; }

    public void setImagePath(String imagePath) { this.imagePath = imagePath; 
}
    @Override
    public String toString() {
        return "Receipt{id=" + id + ", scannedAt=" + scannedAt + ", image='" + imagePath + "'}";
    }
}