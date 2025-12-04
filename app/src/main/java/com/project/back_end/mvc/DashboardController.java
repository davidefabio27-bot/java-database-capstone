package com.project.back_end.mvc;

import com.project.back_end.services.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DashboardController {

// 1. Set Up the MVC Controller Class:
//    - Annotate the class with `@Controller` to indicate that it serves as an MVC controller returning view names (not JSON).
//    - This class handles routing to admin and doctor dashboard pages based on token validation.


// 2. Autowire the Shared AppService:
//    - Inject the common `AppService` class, which provides the token validation logic used to authorize access to dashboards.

@Autowired
private AppService service; 

// 3. Define the `adminDashboard` Method:
//    - Handles HTTP GET requests to `/adminDashboard/{token}`.
//    - Accepts an admin's token as a path variable.
//    - Validates the token using the shared service for the `"admin"` role.
//    - If the token is valid (i.e., no errors returned), forwards the user to the `"admin/adminDashboard"` view.
//    - If invalid, redirects to the root URL, likely the login or home page.

@GetMapping("/adminDashboard/{token}")
public String adminDashboard(@PathVariable String token) {

    // Validate token for "admin" role
    Map<String, Object> validationResult = service.validateToken(token, "admin");
    
    // If the validation result is empty → valid token
    if (validationResult.isEmpty()) {
        return "admin/adminDashboard"; // Thymeleaf template
    }

     // Invalid token → redirect to login
     return "redirect:http://localhost:8080";
}

// 4. Define the `doctorDashboard` Method:
//    - Handles HTTP GET requests to `/doctorDashboard/{token}`.
//    - Accepts a doctor's token as a path variable.
//    - Validates the token using the shared service for the `"doctor"` role.
//    - If the token is valid, forwards the user to the `"doctor/doctorDashboard"` view.
//    - If the token is invalid, redirects to the root URL.

// 4. Doctor Dashboard Controller
@GetMapping("/doctorDashboard/{token}")
public String doctorDashboard(@PathVariable String token) {

    // Validate token for "doctor" role
    Map<String, Object> validationResult = service.validateToken(token, "doctor");

    // If token is valid → open doctor dashboard
    if (validationResult.isEmpty()) {
        return "doctor/doctorDashboard"; // Thymeleaf template
    }

    // Invalid token → redirect to login
    return "redirect:http://localhost:8080";
}
}
