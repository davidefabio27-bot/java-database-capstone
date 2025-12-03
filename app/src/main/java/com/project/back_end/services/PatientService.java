package com.project.back_end.services;

import com.project.back_end.dto.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.TokenService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {
    // 1. **Add @Service Annotation**:
    //    - The `@Service` annotation is used to mark this class as a Spring service component. 
    //    - It will be managed by Spring's container and used for business logic related to patients and appointments.

    // 2. **Constructor Injection for Dependencies**:
    //    - The `PatientService` class has dependencies on `PatientRepository`, `AppointmentRepository`, and `TokenService`.
    //    - These dependencies are injected via the constructor to maintain good practices of dependency injection and testing.

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // 3. **createPatient Method**:
    //    - Creates a new patient in the database. It saves the patient object using the `PatientRepository`.
    //    - If the patient is successfully saved, the method returns `1`; otherwise, it logs the error and returns `0`.
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Nuovo metodo per ottenere gli appuntamenti di un paziente tramite email
    public List<Appointment> getAppointmentsByEmail(String email) {
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            return null; // Nessun paziente trovato
        }
        return appointmentRepository.findByPatient_Id(patient.getId());
    }

    // 4. **getPatientAppointment Method**:
    //    - Retrieves a list of appointments for a specific patient, based on their ID.
    //    - The appointments are then converted into `AppointmentDTO` objects for easier consumption by the API client.
    //    - This method is marked as `@Transactional` to ensure database consistency during the transaction.
    @Transactional
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Controllo token valido
            String email = tokenService.extractEmail(token);
            if (email == null) {
                response.put("error", "invalid token");
                return ResponseEntity.status(401).body(response);
            }

            Patient patient = patientRepository.findByEmail(email);
            if (patient == null || !patient.getId().equals(id)) {
                response.put("error", "unauthorized");
                return ResponseEntity.status(401).body(response);
            }

            List<Appointment> appointments = appointmentRepository.findByPatient_Id(id);
            List<AppointmentDTO> dtos = appointments.stream()
                    .map(AppointmentDTO::new)
                    .toList();

            response.put("appointments", dtos);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Server error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // 5. **filterByCondition Method**:
    //    - Filters appointments for a patient based on the condition (e.g., "past" or "future").
    //    - Retrieves appointments with a specific status (0 for future, 1 for past) for the patient.
    //    - Converts the appointments into `AppointmentDTO` and returns them in the response.
    //    - Instruction: Ensure the method correctly handles "past" and "future" conditions, and that invalid conditions are caught and returned as errors.

    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long patientId, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = tokenService.extractEmail(token);
            if (email == null) {
                response.put("error", "invalid token");
                return ResponseEntity.status(401).body(response);
            }

            Patient patient = patientRepository.findByEmail(email);
            if (patient == null || !patient.getId().equals(patientId)) {
                response.put("error", "unauthorized");
                return ResponseEntity.status(401).body(response);
            }

            int status;
            if (condition.equalsIgnoreCase("past")) status = 1;
            else if (condition.equalsIgnoreCase("future")) status = 0;
            else {
                response.put("error", "Invalid condition");
                return ResponseEntity.badRequest().body(response);
            }

            List<AppointmentDTO> dtos = appointmentRepository
                    .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status)
                    .stream()
                    .map(AppointmentDTO::new)
                    .toList();

            response.put("appointments", dtos);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Server error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // 6. **filterByDoctor Method**:
    //    - Filters appointments for a patient based on the doctor's name.
    //    - It retrieves appointments where the doctor’s name matches the given value, and the patient ID matches the provided ID.
    //    - Instruction: Ensure that the method correctly filters by doctor's name and patient ID and handles any errors or invalid cases.

    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = tokenService.extractEmail(token);
            if (email == null) {
                response.put("error", "invalid token");
                return ResponseEntity.status(401).body(response);
            }

            Patient patient = patientRepository.findByEmail(email);
            if (patient == null || !patient.getId().equals(patientId)) {
                response.put("error", "unauthorized");
                return ResponseEntity.status(401).body(response);
            }

            List<AppointmentDTO> dtos = appointmentRepository
                    .filterByDoctorNameAndPatientId(name, patientId)
                    .stream()
                    .map(AppointmentDTO::new)
                    .toList();

            response.put("appointments", dtos);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Server error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // 7. **filterByDoctorAndCondition Method**:
    //    - Filters appointments based on both the doctor's name and the condition (past or future) for a specific patient.
    //    - This method combines filtering by doctor name and appointment status (past or future).
    //    - Converts the appointments into `AppointmentDTO` objects and returns them in the response.
    //    - Instruction: Ensure that the filter handles both doctor name and condition properly, and catches errors for invalid input.

    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = tokenService.extractEmail(token);
            if (email == null) {
                response.put("error", "invalid token");
                return ResponseEntity.status(401).body(response);
            }

            Patient patient = patientRepository.findByEmail(email);
            if (patient == null || !patient.getId().equals(patientId)) {
                response.put("error", "unauthorized");
                return ResponseEntity.status(401).body(response);
            }

            int status;
            if (condition.equalsIgnoreCase("past")) status = 1;
            else if (condition.equalsIgnoreCase("future")) status = 0;
            else {
                response.put("error", "Invalid condition");
                return ResponseEntity.badRequest().body(response);
            }

            List<AppointmentDTO> dtos = appointmentRepository
                    .filterByDoctorNameAndPatientIdAndStatus(name, patientId, status)
                    .stream()
                    .map(AppointmentDTO::new)
                    .toList();

            response.put("appointments", dtos);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Server error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // 8. **getPatientDetails Method**:
    //    - Retrieves patient details using the `tokenService` to extract the patient's email from the provided token.
    //    - Once the email is extracted, it fetches the corresponding patient from the `patientRepository`.
    //    - It returns the patient's information in the response body.
    //    - Instruction: Make sure that the token extraction process works correctly and patient details are fetched properly based on the extracted email.

    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = tokenService.extractEmail(token);
            if (email == null) {
                response.put("error", "invalid token");
                return ResponseEntity.status(401).body(response);
            }

            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(404).body(response);
            }

            response.put("patient", patient);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Server error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // 9. **Handling Exceptions and Errors**:
    //    - The service methods handle exceptions using try-catch blocks and log any issues that occur. If an error occurs during database operations, the service responds with appropriate HTTP status codes (e.g., `500 Internal Server Error`).
    //    - Instruction: Ensure that error handling is consistent across the service, with proper logging and meaningful error messages returned to the client.

    // 10. **Use of DTOs (Data Transfer Objects)**:
    //    - The service uses `AppointmentDTO` to transfer appointment-related data between layers. This ensures that sensitive or unnecessary data (e.g., password or private patient information) is not exposed in the response.
    //    - Instruction: Ensure that DTOs are used appropriately to limit the exposure of internal data and only send the relevant fields to the client.

    public List<Appointment> filterByCondition(List<Appointment> appointments, String condition) {
        int status = condition.equalsIgnoreCase("past") ? 1 : 0;
        return appointments.stream()
                .filter(a -> a.getStatus() == status)
                .toList();
    }

    public List<Appointment> filterByDoctor(List<Appointment> appointments, String doctorName) {
        return appointments.stream()
                .filter(a -> a.getDoctor().getName().equalsIgnoreCase(doctorName))
                .toList();
    }

    public List<Appointment> filterByDoctorAndCondition(List<Appointment> appointments, String doctorName, String condition) {
        int status = condition.equalsIgnoreCase("past") ? 1 : 0;
        return appointments.stream()
                .filter(a -> a.getDoctor().getName().equalsIgnoreCase(doctorName) && a.getStatus() == status)
                .toList();
    }

}