package com.pantrypal.model;

/**
* Allowed values of PANTRY_ITEM.status (VARCHAR(10)).
* Stored as text thanks to @Enumerated(EnumType.STRING).
*/
public enum ItemStatus {
    ACTIVE, 
    CONSUMED, 
    EXPIRED, 
    DISCARDED
}