package com.parameta.employees;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@ControllerAdvice
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeRepository employeeRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/employee")
    public ResponseEntity<Map<String, Object>> createEmployee(@RequestBody Map<String, Object> employeeData) {
        logger.info("Received request to create employee.");
        Map<String, Object> response = new HashMap<>();

        String firstName = (String) employeeData.get("firstName");
        String lastName = (String) employeeData.get("lastName");
        String documentType = (String) employeeData.get("documentType");
        String documentNumber = (String) employeeData.get("documentNumber");
        LocalDate dateOfBirth = LocalDate.parse((String) employeeData.get("dateOfBirth"), formatter);
        LocalDate dateOfEmployment = LocalDate.parse((String) employeeData.get("dateOfEmployment"), formatter);
        String position = (String) employeeData.get("position");
        Double salary = (Double) employeeData.get("salary");

        if (firstName == null || lastName == null || documentType == null || documentNumber ==
                null || position == null || salary == null) {
            response.put("error", "All fields are required.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Period age = Period.between(dateOfBirth, LocalDate.now());
        if (age.getYears() < 18) {
            logger.error("One or more fields are missing.");
            response.put("error", "Employee must be of legal age.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Store data in MySQL database

        logger.info("Employee created successfully.");
        Period employmentTime = Period.between(dateOfEmployment, LocalDate.now());
        response.put("employmentTimeYears", employmentTime.getYears());
        response.put("employmentTimeMonths", employmentTime.getMonths());
        response.put("ageYears", age.getYears());
        response.put("ageMonths", age.getMonths());
        response.put("ageDays", age.getDays());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "An unexpected error occurred.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    public EmployeeRepository getEmployeeRepository() {
        return employeeRepository;
    }
}