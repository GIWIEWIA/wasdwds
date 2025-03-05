package com.example.progrest.security;

import com.example.progrest.entity.Person;
import com.example.progrest.repository.PersonRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final PersonRepository personRepository;

    public CustomUserDetailsService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.info("🔍 Looking for user with email: {}", email);

        Person user = personRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOGGER.warn("❌ User not found: {}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });

        LOGGER.info("✅ User found: {}", user.getEmail());
        return new CustomUserDetails(user.getEmail(), user.getPassword());
    }
}
