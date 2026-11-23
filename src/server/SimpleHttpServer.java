package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Appointment;
import model.Doctor;
import model.DoctorStats;
import model.Patient;
import repository.InMemoryRepository;
import service.AppointmentService;

import java.awt.Desktop;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Built-in Core Java HTTP Server using com.sun.net.httpserver.
 * Serves the web simulator dashboard and provides lightweight REST JSON endpoints
 * without external libraries or frameworks.
 */
public class SimpleHttpServer {
    private static final int DEFAULT_PORT = 8080;
    private final int port;
    private final InMemoryRepository repo;
    private final AppointmentService appointmentService;
    private HttpServer server;

    public SimpleHttpServer(int port, InMemoryRepository repo, AppointmentService appointmentService) {
        this.port = (port > 0) ? port : DEFAULT_PORT;
        this.repo = repo;
        this.appointmentService = appointmentService;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));

        // REST API Endpoints
        server.createContext("/api/doctors", new DoctorsHandler());
        server.createContext("/api/patients", new PatientsHandler());
        server.createContext("/api/appointments", new AppointmentsHandler());
        server.createContext("/api/stats", new StatsHandler());

        // Static Web Asset Handler (serves web/ folder)
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("Local HTTP Server started successfully at: http://localhost:" + port + "/");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public int getPort() {
        return port;
    }

    public void openInBrowser() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:" + port + "/"));
            }
        } catch (Exception ignored) {}
    }

    private class DoctorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("[");
            List<Doctor> docs = new java.util.ArrayList<>(repo.getAllDoctors());
            for (int i = 0; i < docs.size(); i++) {
                Doctor d = docs.get(i);
                json.append(String.format("{\"id\":%d,\"name\":\"%s\",\"specialty\":\"%s\",\"fee\":%.2f}",
                        d.getDoctorId(), escape(d.getName()), escape(d.getSpecialty()), d.getConsultationFee()));
                if (i < docs.size() - 1) json.append(",");
            }
            json.append("]");
            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    private class PatientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("[");
            List<Patient> list = new java.util.ArrayList<>(repo.getAllPatients());
            for (int i = 0; i < list.size(); i++) {
                Patient p = list.get(i);
                json.append(String.format("{\"id\":%d,\"name\":\"%s\",\"contact\":\"%s\"}",
                        p.getPatientId(), escape(p.getName()), escape(p.getContact())));
                if (i < list.size() - 1) json.append(",");
            }
            json.append("]");
            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    private class AppointmentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("[");
            List<Appointment> list = repo.getAllAppointments();
            for (int i = 0; i < list.size(); i++) {
                Appointment a = list.get(i);
                json.append(String.format("{\"id\":%d,\"patient\":\"%s\",\"doctor\":\"%s\",\"time\":\"%s\",\"status\":\"%s\",\"followUp\":%b}",
                        a.getAppointmentId(), escape(a.getPatient().getName()), escape(a.getDoctor().getName()),
                        a.getDateTime().toString(), a.getStatus(), a.isFollowUp()));
                if (i < list.size() - 1) json.append(",");
            }
            json.append("]");
            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            DoctorStats s = appointmentService.getDoctorStats(1);
            String json = String.format("{\"doctorId\":%d,\"doctorName\":\"%s\",\"total\":%d,\"pending\":%d,\"approved\":%d,\"approvalRate\":%.1f}",
                    s.getDoctorId(), escape(s.getDoctorName()), s.getTotalAssigned(), s.getPendingCount(), s.getApprovedCount(), s.getApprovalRatePercent());
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isBlank()) {
                path = "/index.html";
            }

            File file = new File("web" + path);
            if (!file.exists() || file.isDirectory()) {
                file = new File("web/index.html");
            }

            if (!file.exists()) {
                String notFound = "404 Not Found: " + path;
                sendResponse(exchange, 404, "text/plain", notFound);
                return;
            }

            String contentType = getMimeType(file.getName());
            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static String getMimeType(String filename) {
        if (filename.endsWith(".html") || filename.endsWith(".htm")) return "text/html";
        if (filename.endsWith(".css")) return "text/css";
        if (filename.endsWith(".js")) return "application/javascript";
        if (filename.endsWith(".json")) return "application/json";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".svg")) return "image/svg+xml";
        return "text/plain";
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
