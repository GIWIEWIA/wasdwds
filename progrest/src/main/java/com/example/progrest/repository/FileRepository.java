package com.example.progrest.repository;

import com.example.progrest.entity.FileEntity;
import com.example.progrest.entity.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findBySenderEmail(String senderEmail);
    List<FileEntity> findByStatus(FileStatus status);
}
