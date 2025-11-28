package com.project.back_end.services;

import com.project.back_end.model.*;
import com.project.back_end.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Service {
// 1. **@Service Annotation**
// The @Service annotation marks this class as a service component in Spring. This allows Spring to automatically detect it through component scanning
// and manage its lifecycle, enabling it to be injected into controllers or other services using @Autowired or constructor injection.

// 2. **Constructor Injection for Dependencies**
// The constructor injects all required dependencies (TokenService, Repositories, and other Services). This approach promotes loose coupling, improves testability,
// and ensures that all required dependencies are provided at object creation time.

private final TokenService tokenService;
private final AdminRepository adminRepository;
private final DoctorRepository doctorRepository;
private final PatientRepository patientRepository;
private final DoctorService doctorService;
private final PatientService patientService;

public ServiceMain(TokenService tokenService,
                       AdminRepository adminRepository,
                       DoctorRepository doctorRepository,
                       PatientRepository patientRepository,
                       DoctorService doctorService,
                       PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

// 3. **validateToken Method**
// This method checks if the provided JWT token is valid for a specific user. It uses the TokenService to perform the validation.
// If the token is invalid or expired, it returns a 401 Unauthorized response with an appropriate error message. This ensures security by preventing
// unauthorized access to protected resources.

public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
    Map<String string> response = new HashMap<>();

    if ( token == null || !tokenService.validateToken(token, user)) {
        response.put("message", "invalid or expired token");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    response.put("message", "Token is valid");
        return new ResponseEntity<>(response, HttpStatus.OK);
}

// 4. **validateAdmin Method**
// This method validates the login credentials for an admin user.
// - It first searches the admin repository using the provided username.
// - If an admin is found, it checks if the password matches.
// - If the password is correct, it generates and returns a JWT token (using the admin’s username) with a 200 OK status.
// - If the password is incorrect, it returns a 401 Unauthorized status with an error message.
// - If no admin is found, it also returns a 401 Unauthorized.
// - If any unexpected error occurs during the process, a 500 Internal Server Error response is returned.
// This method ensures that only valid admin users can access secured parts of the system.

public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
    Map<String, String> response = new HashMap<>();

    try {
        Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());

        if (admin == null) {
            response.put("message", "Admin not found");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        if (!admin.getPassword().equals(receivedAdmin.getPassword())) {
            response.put("message", "Incorrect password");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String token = tokenService.generateToken(admin.getUsername());
        response.put("token", token);
        return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (Exception e) {
        response.put("message", "Server error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

// 5. **filterDoctor Method**
// This method provides filtering functionality for doctors based on name, specialty, and available time slots.
// - It supports various combinations of the three filters.
// - If none of the filters are provided, it returns all available doctors.
// This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.

public Map<String, Object> filterDoctor(String name, String specialty, String time) {
    Map<String, Object> response = new HashMap<>();

    List<Doctor> doctors;

    if (name == null && specialty == null && time == null) {
        doctors = doctorRepository.findAll();
    } else {
        doctors = doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
    }

    response.put("doctors", doctors);
    return response;
}

// 6. **validateAppointment Method**
// This method validates if the requested appointment time for a doctor is available.
// - It first checks if the doctor exists in the repository.
// - Then, it retrieves the list of available time slots for the doctor on the specified date.
// - It compares the requested appointment time with the start times of these slots.
// - If a match is found, it returns 1 (valid appointment time).
// - If no matching time slot is found, it returns 0 (invalid).
// - If the doctor doesn’t exist, it returns -1.
// This logic prevents overlapping or invalid appointment bookings.

public int validateAppointment(Appointment appointment) {

    Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctorId());

    if (doctorOpt.isEmpty()) {
        return -1; // doctor does not exist
    }

    Doctor doctor = doctorOpt.get();

    List<String> availability = doctorService.getDoctorAvailability(doctor, appointment.getDate());

    if (availability.contains(appointment.getTime())) {
        return 1; // valid
    }

    return 0; // not available
}

// 7. **validatePatient Method**
// This method checks whether a patient with the same email or phone number already exists in the system.
// - If a match is found, it returns false (indicating the patient is not valid for new registration).
// - If no match is found, it returns true.
// This helps enforce uniqueness constraints on patient records and prevent duplicate entries.

public boolean validatePatient(Patient patient) {
    Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
    return existing == null; // true if patient does NOT exist
}

// 8. **validatePatientLogin Method**
// This method handles login validation for patient users.
// - It looks up the patient by email.
// - If found, it checks whether the provided password matches the stored one.
// - On successful validation, it generates a JWT token and returns it with a 200 OK status.
// - If the password is incorrect or the patient doesn't exist, it returns a 401 Unauthorized with a relevant error.
// - If an exception occurs, it returns a 500 Internal Server Error.
// This method ensures only legitimate patients can log in and access their data securely.

public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
    Map<String, String> response = new HashMap<>();

    try {
        Patient patient = patientRepository.findByEmail(login.getEmail());

        if (patient == null) {
            response.put("message", "Patient not found");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        if (!patient.getPassword().equals(login.getPassword())) {
            response.put("message", "Incorrect password");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String token = tokenService.generateToken(login.getEmail());
        response.put("token", token);
        return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (Exception e) {
        response.put("message", "Server error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


// 9. **filterPatient Method**
// This method filters a patient's appointment history based on condition and doctor name.
// - It extracts the email from the JWT token to identify the patient.
// - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
// - If no filters are provided, it retrieves all appointments for the patient.
// This flexible method supports patient-specific querying and enhances user experience on the client side.

public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
    Map<String, Object> response = new HashMap<>();

    try {
        String email = tokenService.extractUsername(token);
        List<Appointment> allAppointments = patientService.getAppointmentsByEmail(email);

        if (allAppointments == null) {
            response.put("message", "Patient not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        List<Appointment> filtered;

        if (condition != null && name != null) {
            filtered = patientService.filterByDoctorAndCondition(allAppointments, name, condition);
        } else if (condition != null) {
            filtered = patientService.filterByCondition(allAppointments, condition);
        } else if (name != null) {
            filtered = patientService.filterByDoctor(allAppointments, name);
        } else {
            filtered = allAppointments; // no filter
        }

        response.put("appointments", filtered);
        return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (Exception e) {
        response.put("message", "Server error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
}
