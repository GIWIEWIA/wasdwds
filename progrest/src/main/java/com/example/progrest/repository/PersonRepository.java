package com.example.progrest.repository;

import com.example.progrest.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
  Optional<Person> findByUsername(String username);
  Optional<Person> findByEmail(String email);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
}
