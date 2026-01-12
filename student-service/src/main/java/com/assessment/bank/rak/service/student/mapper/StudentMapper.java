package com.assessment.bank.rak.service.student.mapper;

import java.util.function.Function;

import com.assessment.bank.rak.service.student.database.r2dbc.entity.Student;
import com.assessment.bank.rak.service.student.web.request.StudentRequest;

public class StudentMapper {

	public static final Function<StudentRequest, Student> StudentRequestToStudentMapper = request -> {
        Student student = new Student();
        student.setName(request.studentName());
        student.setEmail(request.studentEmail());
        student.setGrade(request.grade());
        student.setMobileNumber(request.mobileNumber());
        student.setSchoolName(request.schoolName() != null ? request.schoolName() : "true");
        return student;
    };
	
}
