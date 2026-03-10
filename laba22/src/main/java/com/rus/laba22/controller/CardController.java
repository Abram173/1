package com.rus.laba22.controller;

import com.rus.laba22.model.Card;
import com.rus.laba22.repository.CardRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardRepository cardRepository;

    public CardController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @PostMapping
    public Card create(@RequestBody Card card) {
        return cardRepository.save(card);
    }

    @GetMapping
    public List<Card> getAll() {
        return cardRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Card> getById(@PathVariable Long id) {
        return cardRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Card> update(@PathVariable Long id, @RequestBody Card updated) {
        return cardRepository.findById(id)
                .map(existing -> {
                    existing.setNumber(updated.getNumber());
                    existing.setBlocked(updated.isBlocked());
                    existing.setAccount(updated.getAccount());
                    return ResponseEntity.ok(cardRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!cardRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cardRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

