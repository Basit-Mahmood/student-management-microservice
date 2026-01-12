package com.assessment.bank.rak.service.student.web.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.assessment.bank.rak.service.student.database.r2dbc.entity.Student;
import com.assessment.bank.rak.service.student.service.StudentService;
import com.assessment.bank.rak.service.student.web.request.StudentRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student API(s)", description = "Endpoints for managing student records in the RAK bank assessment system")
public class StudentController {

	private static final Logger LOGGER = LogManager.getLogger();

	private final StudentService studentService;

	public StudentController(StudentService studentService) {
		this.studentService = studentService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
        summary = "Add a new student",
        description = "Creates a new student record and returns the saved entity with a generated ID."
    )
    @ApiResponse(responseCode = "210", description = "Student successfully created") // 201 Created
    @ApiResponse(responseCode = "400", description = "Invalid student data provided")
	public Mono<Student> addStudent(@Valid @RequestBody StudentRequest request) {
		
		LOGGER.info("REST request to add student: {}", request.studentName());

		return studentService.addStudent(request)
				.doOnNext(saved -> LOGGER.info("Student created successfully with ID: {}", saved.getStudentId()))
				.doOnError(ex -> LOGGER.error("Failed to add student via REST", ex));
	}

	@GetMapping
	@Operation(summary = "Get all students", description = "Returns a paginated list of all students.")
	public Mono<Page<Student>> getAllStudents(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
		
		LOGGER.info("REST request to get all students - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

		return studentService
				.getAllStudents(pageable)
				.doOnSuccess(page -> LOGGER.info("Fetched {} students out of {} total", page.getNumberOfElements(), page.getTotalElements()))
				.doOnError(ex -> LOGGER.error("Failed to fetch students page", ex));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Find student by ID", description = "Retrieves a single student record by their primary ID.")
	@Parameter(name = "id", description = "The ID of the student to retrieve", example = "101")
    @ApiResponse(responseCode = "200", description = "Student found")
    @ApiResponse(responseCode = "404", description = "Student not found with the given ID")
	public Mono<ResponseEntity<Student>> getStudentById(@PathVariable String id) {
		
		LOGGER.debug("REST request to get student by ID: {}", id);

		return studentService.getStudentById(id)
			.map(student -> {
				LOGGER.info("Student found for ID: {}", id);
				return ResponseEntity.ok(student);
			})
			.defaultIfEmpty(ResponseEntity.notFound().build())
			.doOnSuccess(response -> {
				if (response.getStatusCode().is4xxClientError()) {
					LOGGER.warn("Student lookup returned 404 for ID: {}", id);
				}
			})
			.doOnError(ex -> LOGGER.error("Error occurred during student lookup for ID: {}", id, ex));
	}

}
