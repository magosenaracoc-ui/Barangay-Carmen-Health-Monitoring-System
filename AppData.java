package barangaycarmenhealthmonitoringsystem;

import java.util.ArrayList;
import java.util.List;

public class AppData {

    public static List<Patient> patients = new ArrayList<>();
    public static List<Appointment> appointments = new ArrayList<>();
    public static List<Consultation> consultations = new ArrayList<>();
    public static List<Service> services = new ArrayList<>();
    public static List<HealthRecord> healthRecords = new ArrayList<>();
    public static List<User> users = new ArrayList<>();

    static {

        // =====================================================
        // USERS
        // =====================================================

        users.add(new User("admin", "admin123", "Admin"));
        users.add(new User("staff", "staff123", "Staff"));

        // =====================================================
        // PATIENTS - 10 SAMPLE PATIENTS
        // =====================================================

        patients.add(new Patient(
                "P001",
                "Juan Dela Cruz",
                45,
                "M",
                "0912-345-6789",
                "Active"
        ));

        patients.add(new Patient(
                "P002",
                "Maria Santos",
                32,
                "F",
                "0923-111-2222",
                "Active"
        ));

        patients.add(new Patient(
                "P003",
                "Pedro Garcia",
                29,
                "M",
                "0933-222-3333",
                "Active"
        ));

        patients.add(new Patient(
                "P004",
                "Ana Reyes",
                37,
                "F",
                "0944-333-4444",
                "Active"
        ));

        patients.add(new Patient(
                "P005",
                "Lito Villanueva",
                60,
                "M",
                "0955-444-5555",
                "Inactive"
        ));

        patients.add(new Patient(
                "P006",
                "Sofia Garcia",
                24,
                "F",
                "0966-555-6666",
                "Active"
        ));

        patients.add(new Patient(
                "P007",
                "Jose Ramirez",
                50,
                "M",
                "0977-666-7777",
                "Active"
        ));

        patients.add(new Patient(
                "P008",
                "Elena Cruz",
                36,
                "F",
                "0988-777-8888",
                "Active"
        ));

        patients.add(new Patient(
                "P009",
                "Miguel Santos",
                39,
                "M",
                "0999-888-9999",
                "Active"
        ));

        patients.add(new Patient(
                "P010",
                "Angela Flores",
                27,
                "F",
                "0910-123-4567",
                "Active"
        ));

        // =====================================================
        // APPOINTMENTS - 10 SAMPLE APPOINTMENTS
        // =====================================================

        appointments.add(new Appointment(
                "08:00 AM",
                "Juan Dela Cruz",
                "Check-up",
                "Pending"
        ));

        appointments.add(new Appointment(
                "09:00 AM",
                "Maria Santos",
                "Follow-up",
                "Confirmed"
        ));

        appointments.add(new Appointment(
                "10:00 AM",
                "Pedro Garcia",
                "Consultation",
                "Pending"
        ));

        appointments.add(new Appointment(
                "11:00 AM",
                "Ana Reyes",
                "Check-up",
                "Confirmed"
        ));

        appointments.add(new Appointment(
                "01:00 PM",
                "Lito Villanueva",
                "Laboratory",
                "Rescheduled"
        ));

        appointments.add(new Appointment(
                "02:00 PM",
                "Sofia Garcia",
                "Health Monitoring",
                "Confirmed"
        ));

        appointments.add(new Appointment(
                "03:00 PM",
                "Jose Ramirez",
                "Follow-up",
                "Pending"
        ));

        appointments.add(new Appointment(
                "04:00 PM",
                "Elena Cruz",
                "General Check-up",
                "Completed"
        ));

        appointments.add(new Appointment(
                "04:30 PM",
                "Miguel Santos",
                "Blood Pressure Check",
                "Confirmed"
        ));

        appointments.add(new Appointment(
                "05:00 PM",
                "Angela Flores",
                "Health Monitoring",
                "Pending"
        ));

        // =====================================================
        // CONSULTATIONS - 10 SAMPLE CONSULTATIONS
        // =====================================================

        consultations.add(new Consultation(
                "Juan Dela Cruz",
                "May 20, 2026",
                "Check-up",
                "170 cm",
                "65 kg",
                "120 / 80",
                "36.5 °C",
                "78 bpm",
                "Completed"
        ));

        consultations.add(new Consultation(
                "Maria Santos",
                "May 20, 2026",
                "Follow-up",
                "158 cm",
                "55 kg",
                "118 / 78",
                "36.6 °C",
                "80 bpm",
                "Ongoing"
        ));

        consultations.add(new Consultation(
                "Pedro Garcia",
                "May 19, 2026",
                "Fever",
                "172 cm",
                "70 kg",
                "125 / 82",
                "37.2 °C",
                "82 bpm",
                "Completed"
        ));

        consultations.add(new Consultation(
                "Ana Reyes",
                "May 19, 2026",
                "Headache",
                "160 cm",
                "58 kg",
                "115 / 75",
                "36.4 °C",
                "76 bpm",
                "Completed"
        ));

        consultations.add(new Consultation(
                "Lito Villanueva",
                "May 18, 2026",
                "Hypertension",
                "168 cm",
                "72 kg",
                "130 / 85",
                "36.8 °C",
                "84 bpm",
                "Ongoing"
        ));

        consultations.add(new Consultation(
                "Sofia Garcia",
                "May 18, 2026",
                "General Check-up",
                "155 cm",
                "52 kg",
                "118 / 76",
                "36.5 °C",
                "77 bpm",
                "Completed"
        ));

        consultations.add(new Consultation(
                "Jose Ramirez",
                "May 17, 2026",
                "Cough",
                "169 cm",
                "68 kg",
                "122 / 80",
                "36.7 °C",
                "79 bpm",
                "Ongoing"
        ));

        consultations.add(new Consultation(
                "Elena Cruz",
                "May 17, 2026",
                "Health Monitoring",
                "157 cm",
                "54 kg",
                "116 / 75",
                "36.4 °C",
                "75 bpm",
                "Completed"
        ));

        consultations.add(new Consultation(
                "Miguel Santos",
                "May 16, 2026",
                "Blood Pressure Check",
                "171 cm",
                "73 kg",
                "128 / 84",
                "36.6 °C",
                "83 bpm",
                "Completed"
        ));

        consultations.add(new Consultation(
                "Angela Flores",
                "May 16, 2026",
                "Follow-up",
                "159 cm",
                "57 kg",
                "117 / 77",
                "36.5 °C",
                "78 bpm",
                "Ongoing"
        ));

        // =====================================================
        // HEALTH RECORDS - 10 SAMPLE HEALTH RECORDS
        // =====================================================

        healthRecords.add(new HealthRecord(
                "Juan Dela Cruz",
                "May 20, 2026",
                "170 cm",
                "65 kg",
                "120 / 80",
                "36.5 °C",
                "78 bpm",
                "Active"
        ));

        healthRecords.add(new HealthRecord(
                "Maria Santos",
                "May 20, 2026",
                "158 cm",
                "55 kg",
                "118 / 78",
                "36.6 °C",
                "80 bpm",
                "Active"
        ));

        healthRecords.add(new HealthRecord(
                "Pedro Garcia",
                "May 19, 2026",
                "172 cm",
                "70 kg",
                "125 / 82",
                "37.2 °C",
                "82 bpm",
                "Active"
        ));

        healthRecords.add(new HealthRecord(
                "Ana Reyes",
                "May 19, 2026",
                "160 cm",
                "58 kg",
                "115 / 75",
                "36.4 °C",
                "76 bpm",
                "Active"
        ));

        healthRecords.add(new HealthRecord(
                "Lito Villanueva",
                "May 18, 2026",
                "168 cm",
                "72 kg",
                "130 / 85",
                "36.8 °C",
                "84 bpm",
                "Inactive"
        ));

        healthRecords.add(new HealthRecord(
                "Sofia Garcia",
                "May 18, 2026",
                "155 cm",
                "52 kg",
                "118 / 76",
                "36.5 °C",
                "77 bpm",
                "Active"
        ));

        healthRecords.add(new HealthRecord(
                "Jose Ramirez",
                "May 17, 2026",
                "169 cm",
                "68 kg",
                "122 / 80",
                "36.7 °C",
                "79 bpm",
                "Active"
        ));

        healthRecords.add(new HealthRecord(
                "Elena Cruz",
                "May 17, 2026",
                "157 cm",
                "54 kg",
                "116 / 75",
                "36.4 °C",
                "75 bpm",
                "Active"
        ));

        healthRecords.add(new HealthRecord(
                "Miguel Santos",
                "May 16, 2026",
                "171 cm",
                "73 kg",
                "128 / 84",
                "36.6 °C",
                "83 bpm",
                "Active"
        ));

        healthRecords.add(new HealthRecord(
                "Angela Flores",
                "May 16, 2026",
                "159 cm",
                "57 kg",
                "117 / 77",
                "36.5 °C",
                "78 bpm",
                "Active"
        ));

        // =====================================================
        // SERVICES
        // =====================================================

        services.add(new Service(
                "S001",
                "General Check-up",
                "Available"
        ));

        services.add(new Service(
                "S002",
                "Blood Pressure Check",
                "Available"
        ));

        services.add(new Service(
                "S003",
                "Laboratory",
                "Available"
        ));

        services.add(new Service(
                "S004",
                "Medicine Assistance",
                "Available"
        ));

        services.add(new Service(
                "S005",
                "Health Monitoring",
                "Available"
        ));
    }

    // =====================================================
    // PATIENT
    // =====================================================

    public static class Patient {

        public String id;
        public String name;
        public int age;
        public String sex;
        public String contact;
        public String status;

        public Patient(
                String id,
                String name,
                int age,
                String sex,
                String contact,
                String status) {

            this.id = id;
            this.name = name;
            this.age = age;
            this.sex = sex;
            this.contact = contact;
            this.status = status;
        }
    }

    // =====================================================
    // APPOINTMENT
    // =====================================================

    public static class Appointment {

        public String time;
        public String patientName;
        public String purpose;
        public String status;

        public Appointment(
                String time,
                String patientName,
                String purpose,
                String status) {

            this.time = time;
            this.patientName = patientName;
            this.purpose = purpose;
            this.status = status;
        }
    }

    // =====================================================
    // CONSULTATION
    // =====================================================

    public static class Consultation {

        public String patientName;
        public String date;
        public String reason;

        public String height;
        public String weight;
        public String bloodPressure;
        public String temperature;
        public String pulseRate;

        public String status;

        // OLD CONSTRUCTOR
        public Consultation(
                String patientName,
                String date,
                String reason,
                String status) {

            this(
                    patientName,
                    date,
                    reason,
                    "",
                    "",
                    "",
                    "",
                    "",
                    status
            );
        }

        // NEW CONSTRUCTOR
        public Consultation(
                String patientName,
                String date,
                String reason,
                String height,
                String weight,
                String bloodPressure,
                String temperature,
                String pulseRate,
                String status) {

            this.patientName = patientName;
            this.date = date;
            this.reason = reason;
            this.height = height;
            this.weight = weight;
            this.bloodPressure = bloodPressure;
            this.temperature = temperature;
            this.pulseRate = pulseRate;
            this.status = status;
        }
    }

    // =====================================================
    // HEALTH RECORD
    // =====================================================

    public static class HealthRecord {

        public String patientName;
        public String lastVisit;
        public String height;
        public String weight;
        public String bloodPressure;
        public String temperature;
        public String pulseRate;
        public String status;

        // OLD CONSTRUCTOR
        public HealthRecord(
                String patientName,
                String lastVisit,
                String bloodPressure,
                String temperature,
                String pulseRate,
                String weight,
                String status) {

            this(
                    patientName,
                    lastVisit,
                    "",
                    weight,
                    bloodPressure,
                    temperature,
                    pulseRate,
                    status
            );
        }

        // NEW CONSTRUCTOR
        public HealthRecord(
                String patientName,
                String lastVisit,
                String height,
                String weight,
                String bloodPressure,
                String temperature,
                String pulseRate,
                String status) {

            this.patientName = patientName;
            this.lastVisit = lastVisit;
            this.height = height;
            this.weight = weight;
            this.bloodPressure = bloodPressure;
            this.temperature = temperature;
            this.pulseRate = pulseRate;
            this.status = status;
        }
    }

    // =====================================================
    // SERVICE
    // =====================================================

    public static class Service {

        public String id;
        public String name;
        public String status;

        public Service(
                String id,
                String name,
                String status) {

            this.id = id;
            this.name = name;
            this.status = status;
        }
    }

    // =====================================================
    // USER
    // =====================================================

    public static class User {

        public String username;
        public String password;
        public String role;

        public User(
                String username,
                String password,
                String role) {

            this.username = username;
            this.password = password;
            this.role = role;
        }
    }
}

