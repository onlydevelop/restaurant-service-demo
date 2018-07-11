package com.example.microservices.itemservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemServiceController {
    
    @GetMapping("/items/{id}/type/{type}")
    public Item getItem(@PathVariable Long id, @PathVariable String type) {
        
        return new Item(id, "Mutton Biriyani", 220);
    }
}