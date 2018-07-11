package com.example.microservices.itemservice;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemServiceController {
    
    @Autowired
    private ItemRepository repository;
    
    @GetMapping("/items/{id}/type/{type}")
    public Item getItem(@PathVariable Long id, @PathVariable String type) {
        
        Optional<Item> item = repository.findById(id);
        return item.get(); 
    }
}