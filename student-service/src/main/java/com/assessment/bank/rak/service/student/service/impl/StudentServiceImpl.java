package com.assessment.bank.rak.service.student.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.assessment.bank.rak.service.student.aop.ExecutionTimeLogger;
import com.assessment.bank.rak.service.student.database.r2dbc.entity.Student;
import com.assessment.bank.rak.service.student.database.r2dbc.repository.StudentRepository;
import com.assessment.bank.rak.service.student.mapper.StudentMapper;
import com.assessment.bank.rak.service.student.service.StudentService;
import com.assessment.bank.rak.service.student.web.request.StudentRequest;

import reactor.core.publisher.Mono;

@Service
public class StudentServiceImpl implements StudentService {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
	
    @Override
    @ExecutionTimeLogger
    public Mono<Student> addStudent(StudentRequest studentRequest) {
        return Mono.just(studentRequest)
            .map(StudentMapper.StudentRequestToStudentMapper) 	// Transformation Step
            .flatMap(studentRepository::save)					// Persistence Step
            .doOnNext(saved -> LOGGER.info("Successfully added student with ID: {}", saved.getStudentId()))
            .doOnError(error -> LOGGER.error("Failed to add student: {}", studentRequest.studentName(), error));
    }

	@Override
	@ExecutionTimeLogger
	public Mono<Page<Student>> getAllStudents(Pageable pageable) {
		
		LOGGER.debug("Fetching page {} of students with size {}", pageable.getPageNumber(), pageable.getPageSize());
		
		return studentRepository.findAllBy(pageable)
                .collectList() // Collect Flux into a Mono<List>
                .zipWith(studentRepository.count()) // Combine with total count
                // Fix: Explicitly define the return type as Page<Student>
                .map(tuple -> {
                    Page<Student> page = new PageImpl<>(
                            tuple.getT1(), 
                            pageable, 
                            tuple.getT2()
                    );
                    return page; 
                })
                .doOnSuccess(page -> LOGGER.info("Retrieved {} students for page {}", page.getNumberOfElements(), pageable.getPageNumber()))
                .doOnError(error -> LOGGER.error("Error occurred while fetching paginated students", error));
	}

	@Override
	@ExecutionTimeLogger
    public Mono<Student> getStudentById(String studentId) {
        return studentRepository.findById(Long.valueOf(studentId))
            .doOnNext(student -> LOGGER.debug("Found student: {}", student.getName()))
            .switchIfEmpty(Mono.defer(() -> {
                LOGGER.warn("Student not found with ID: {}", studentId);
                return Mono.empty();
            }))
            .doOnError(error -> LOGGER.error("Error searching for student ID: {}", studentId, error));
    }

}
