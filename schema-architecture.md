## SECTION 1. 
This Spring Boot application uses both MVC and REST controllers.
Thymeleaf templates are used for the Admin and Doctor dashboards, 
while REST APIs serve all other modules. The application interacts 
with two databases—MySQL (for patient, doctor, appointment, and admin data) 
and MongoDB (for prescriptions). All controllers route requests through
a common service layer, which in turn delegates to the appropriate repositories.
MySQL uses JPA entities while MongoDB uses document models.

---

## SECTION 2.
1) The user sends a request
   The flow begins when a user interacts with the system —
   either by opening a Thymeleaf page (e.g., AdminDashboard, DoctorDashboard)
   or by calling a REST endpoint through a web/mobile client
   such as Appointments or PatientDashboard.

2) Request is routed to the correct controller
   Spring maps the URL to a specific controller.
-  MVC requests go to Thymeleaf Controllers, which return HTML templates.
-  API requests go to REST Controllers, which return JSON responses.

3) The controller delegates the operation to the Service Layer
   Controllers do not contain business logic. They pass incoming data
   to a service class, which handles all validations, workflows, and decision-making.

4) The Service Layer interacts with the Repository Layer
   Services call one or more repositories to retrieve or save information.
   This may involve checking availability, verifying user data, or coordinating
   actions across entities.

5) Repositories communicate with the databases
   Each repository triggers operations on the appropriate database engine:
-  MySQL (via Spring Data JPA) for structured data like users, doctors, patients, appointments.
-  MongoDB (via Spring Data MongoDB) for flexible documents such as prescriptions.

6) Data from the database is mapped into model classes
   Database results are converted into application models:
-  JPA Entities for MySQL
-  @Document classes for MongoDB
   These model objects allow the application to work with the data in an object-oriented way.

7) The response is returned to the user
   The controller receives the models from the service layer and finalizes the response:
-  MVC controllers pass the models to Thymeleaf templates and render HTML pages.
-  REST controllers serialize the models (or DTOs) into JSON.
   This completes the full request-response cycle.
