package com.assessment.bank.rak.service.student.database.r2dbc.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.assessment.bank.rak.service.student.database.r2dbc.entity.Student;

import reactor.core.publisher.Flux;

@Repository
public interface StudentRepository extends R2dbcRepository<Student, Long> {

	// The findBy keyword with Pageable allows R2DBC to apply LIMIT and OFFSET automatically
    Flux<Student> findAllBy(Pageable pageable);
	
}
