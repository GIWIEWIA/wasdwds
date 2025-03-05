package com.example.progrest.controller;

import com.example.progrest.entity.Person;
import com.example.progrest.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/persons") // ใช้ path `/api/persons`
@CrossOrigin("*") // อนุญาตให้ Angular เรียก API ได้
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    // ✅ 1. ดึงข้อมูลผู้ใช้ทั้งหมด
    @GetMapping
    public ResponseEntity<List<Person>> getAllPersons() {
        return ResponseEntity.ok(personService.getAllPersons());
    }

    // ✅ 2. ดึงข้อมูลผู้ใช้ตาม ID
    @GetMapping("/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getPersonById(id));
    }

    // ✅ 3. ดึงข้อมูลผู้ใช้ที่ล็อกอินอยู่
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized")); // Unauthorized ถ้าไม่ได้ล็อกอิน
        }

        // ✅ ใช้ `Map.of()` แทน `HashMap` เพื่อทำให้โค้ดสะอาดขึ้น
        return ResponseEntity.ok(Map.of("name", userDetails.getUsername()));
    }

    // ✅ 4. อัปเดตข้อมูลผู้ใช้
    @PutMapping("/{id}")
    public ResponseEntity<Person> updatePerson(@PathVariable Long id, @RequestBody Person updatedPerson) {
        return ResponseEntity.ok(personService.updatePerson(id, updatedPerson));
    }

    // ✅ 5. ลบผู้ใช้
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully!"));
    }
}
