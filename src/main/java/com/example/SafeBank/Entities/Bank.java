package com.example.SafeBank.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "banks")
@Getter
@Setter
@NoArgsConstructor
public class Bank {
    @Id
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "last_refreshed_at", nullable = false)
    private LocalDateTime lastRefreshedAt;

    public Bank(String code, String name, LocalDateTime lastRefreshedAt) {
        this.code = code;
        this.name = name;
        this.lastRefreshedAt = lastRefreshedAt;
    }
}
