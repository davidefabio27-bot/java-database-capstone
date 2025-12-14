import { API_BASE_URL } from "../config/config.js";
const PATIENT_API = API_BASE_URL + '/patient';

// For creating a patient in db
export async function patientSignup(data) {
  try {
    const response = await fetch(`${PATIENT_API}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    });

    const result = await response.json();
    if (!response.ok) {
      throw new Error(result.message);
    }

    return { success: response.ok, message: result.message };
  } catch (error) {
    console.error("Error :: patientSignup :: ", error);
    return { success: false, message: error.message };
  }
}

// For logging in patient
export async function patientLogin(data) {
  console.log("patientLogin :: ", data);
  return await fetch(`${PATIENT_API}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });
}

// For getting patient data (name, id, etc). Used in booking appointments
export async function getPatientData(token) {
  try {
    const response = await fetch(`${PATIENT_API}/${encodeURIComponent(token)}`); // ✅ token codificato
    if (!response.ok) throw new Error("Failed to fetch patient data");

    const data = await response.json();
    return data.patient || null;
  } catch (error) {
    console.error("Error fetching patient details:", error);
    return null;
  }
}

// Fetch patient appointments
export async function getPatientAppointments(id, token, user) {
  try {
    const response = await fetch(`${PATIENT_API}/${id}/${encodeURIComponent(user)}/${encodeURIComponent(token)}`); // ✅ encode
    if (!response.ok) throw new Error("Failed to fetch appointments");

    const data = await response.json();
    return data.appointments || [];
  } catch (error) {
    console.error("Error fetching patient appointments:", error);
    return [];
  }
}

// Filter appointments
export async function filterAppointments(condition, name, token) {
  try {
    const url = `${PATIENT_API}/filter/${encodeURIComponent(condition)}/${encodeURIComponent(name)}/${encodeURIComponent(token)}`; // ✅ encode
    const response = await fetch(url, {
      method: "GET",
      headers: { "Content-Type": "application/json" }
    });

    if (!response.ok) throw new Error("Failed to fetch appointments");

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error filtering appointments:", error);
    alert("Something went wrong!");
    return { appointments: [] };
  }
}