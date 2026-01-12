package com.assessment.bank.rak.service.student.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.assessment.bank.rak.service.student.database.r2dbc.entity.Student;
import com.assessment.bank.rak.service.student.web.request.StudentRequest;

import reactor.core.publisher.Mono;

public interface StudentService {

	Mono<Student> addStudent(StudentRequest studentRequest);
	
	Mono<Page<Student>> getAllStudents(Pageable pageable);
	
	Mono<Student> getStudentById(String studentId);
	
}
