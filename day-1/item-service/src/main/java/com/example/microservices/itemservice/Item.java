package com.example.microservices.itemservice;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Item {
    
    @Id
    private long id;
    private String name;
    private int price;
    
    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
    
    public Item() {}
    
    public Item(long id, String name, int price) {
        super();
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
