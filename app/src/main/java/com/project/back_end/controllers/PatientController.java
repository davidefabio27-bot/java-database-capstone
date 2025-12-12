package com.project.back_end.controllers;

import com.project.back_end.dto.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller for patient-related operations.
//    - Use `@RequestMapping("/patient")` to prefix all endpoints with `/patient`, grouping all patient functionalities under a common route.

    private final PatientService patientService;
    private final AppService appService;

    @Autowired
    public PatientController(PatientService patientService, AppService appService) {
        this.patientService = patientService;
        this.appService = appService;
    }

// 3. Define the `getPatient` Method:
//    - Handles HTTP GET requests to retrieve patient details using a token.
//    - Validates the token for the `"patient"` role using the shared service.
//    - If the token is valid, returns patient information; otherwise, returns an appropriate error message.

    @GetMapping("/{token}")
    public ResponseEntity<?> getPatient(@PathVariable String token) {
        Map<String, Object> response = appService.validateToken(token, "patient");
        if (response.containsKey("message")) { // token non valido
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        return patientService.getPatientDetails(token);
    }

// 4. Define the `createPatient` Method:
//    - Handles HTTP POST requests for patient registration.
//    - Accepts a validated `Patient` object in the request body.
//    - First checks if the patient already exists using the shared service.
//    - If validation passes, attempts to create the patient and returns success or error messages based on the outcome.

    @PostMapping
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        Map<String, Object> response = new HashMap<>();

        if (!appService.validatePatient(patient)) {
            response.put("message", "Patient with email id or phone no already exist");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        int created = patientService.createPatient(patient);

        if (created == 1) {
            response.put("message", "Signup successful");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

// 5. Define the `login` Method:
//    - Handles HTTP POST requests for patient login.
//    - Accepts a `Login` DTO containing email/username and password.
//    - Delegates authentication to the `validatePatientLogin` method in the shared service.
//    - Returns a response with a token or an error message depending on login success.

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login) {
        return appService.validatePatientLogin(login);
    }

// 6. Define the `getPatientAppointment` Method:
//    - Handles HTTP GET requests to fetch appointment details for a specific patient.
//    - Requires the patient ID and token as path variables.
//    - Validates the token using the shared service.
//    - If valid, retrieves the patient's appointment data from `PatientService`; otherwise, returns a validation error.

    @GetMapping("/{id}/{token}")
    public ResponseEntity<?> getPatientAppointments(@PathVariable Long id,
                                                    @PathVariable String token) {

        Map<String, Object> response = appService.validateToken(token, "patient");
        if (response.containsKey("message")) {
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        return patientService.getPatientAppointment(id, token);
    }

// 7. Define the `filterPatientAppointment` Method:
//    - Handles HTTP GET requests to filter a patient's appointments based on specific conditions.
//    - Accepts filtering parameters: `condition`, `name`, and a token.
//    - Token must be valid for a `"patient"` role.
//    - If valid, delegates filtering logic to the shared service and returns the filtered result.

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<?> filterPatientAppointment(@PathVariable String condition,
                                                      @PathVariable String name,
                                                      @PathVariable String token) {

        Map<String, Object> response = appService.validateToken(token, "patient");
        if (response.containsKey("message")) {
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        return appService.filterPatient(condition, name, token);
    }
}

