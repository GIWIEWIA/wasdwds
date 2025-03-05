package com.example.progrest.service;

import com.example.progrest.entity.Person;
import com.example.progrest.exception.UserNotFoundException;
import com.example.progrest.repository.PersonRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PersonService {
    private final PersonRepository personRepository;

    // ✅ กำหนดค่าคงที่ (Constant) เพื่อลดการใช้ซ้ำ
    private static final String USER_NOT_FOUND_WITH_ID = "User not found with id: ";
    private static final String USER_NOT_FOUND_WITH_USERNAME = "User not found with username: ";

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    // ✅ ดึงข้อมูลผู้ใช้ทั้งหมด
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    // ✅ ดึงข้อมูลผู้ใช้ตาม ID
    public Person getPersonById(Long id) {
        return personRepository.findById(id)

        
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_WITH_ID + id));
    }

    // ✅ ดึงข้อมูลผู้ใช้ที่ล็อกอินอยู่
    public Person getPersonByUsername(String username) {
        return personRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_WITH_USERNAME + username));
    }

    // ✅ อัปเดตข้อมูลผู้ใช้
    public Person updatePerson(Long id, Person updatedPerson) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_WITH_ID + id));

        person.setUsername(updatedPerson.getUsername());
        person.setEmail(updatedPerson.getEmail());
        person.setPassword(updatedPerson.getPassword());

        return personRepository.save(person);
    }

    // ✅ ลบผู้ใช้
    public void deletePerson(Long id) {
        if (!personRepository.existsById(id)) {
            throw new UserNotFoundException(USER_NOT_FOUND_WITH_ID + id);
        }
        personRepository.deleteById(id);
    }
}
