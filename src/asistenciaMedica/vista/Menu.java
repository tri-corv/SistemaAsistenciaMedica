package asistenciaMedica.vista;

import asistenciaMedica.dao.AsistenciaMedicaDAO;
import asistenciaMedica.dao.ConexionBD;
import asistenciaMedica.dao.ObraSocialDAO;
import asistenciaMedica.dao.PacienteDAO;
import asistenciaMedica.dao.ProfesionalDAO;
import asistenciaMedica.modelo.AsistenciaMedica;
import asistenciaMedica.modelo.NivelUrgencia;
import asistenciaMedica.modelo.ObraSocial;
import asistenciaMedica.modelo.Paciente;
import asistenciaMedica.modelo.Profesional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Menu {
    private static final DateTimeFormatter FORMATO_ENTRADA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Scanner scanner = new Scanner(System.in);
    private final ObraSocialDAO obraSocialDAO = new ObraSocialDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final ProfesionalDAO profesionalDAO = new ProfesionalDAO();
    private final AsistenciaMedicaDAO asistenciaDAO = new AsistenciaMedicaDAO();

    public void iniciar() {
        verificarConexion();

        int opcion;
        do {
            mostrarOpciones();
            opcion = leerEntero("Seleccione una opcion: ");
            ejecutarOpcion(opcion);
        } while (opcion != 0);
    }

    private void verificarConexion() {
        try {
            ConexionBD.probarConexion();
            System.out.println("Conexion a MySQL establecida correctamente.");
        } catch (SQLException error) {
            System.out.println("No se pudo conectar a MySQL.");
            System.out.println("Revise que XAMPP tenga MySQL iniciado y que exista la base asistencia_medica.");
            System.out.println("Detalle: " + error.getMessage());
        }
    }

    private void mostrarOpciones() {
        System.out.println();
        System.out.println("=== SISTEMA DE ASISTENCIAS MEDICAS ===");
        System.out.println("1. Registrar paciente");
        System.out.println("2. Registrar profesional");
        System.out.println("3. Registrar asistencia medica");
        System.out.println("4. Listar asistencias pendientes");
        System.out.println("5. Atender asistencias pendientes");
        System.out.println("6. Buscar asistencia por ID");
        System.out.println("7. Listar todas las asistencias");
        System.out.println("8. Registrar obra social");
        System.out.println("9. Listar obras sociales");
        System.out.println("0. Salir");
    }

    private void ejecutarOpcion(int opcion) {
        try {
            switch (opcion) {
                case 1 -> registrarPaciente();
                case 2 -> registrarProfesional();
                case 3 -> registrarAsistencia();
                case 4 -> listarPendientes();
                case 5 -> atenderPendientes();
                case 6 -> buscarAsistencia();
                case 7 -> listarTodas();
                case 8 -> registrarObraSocial();
                case 9 -> listarObrasSociales();
                case 0 -> System.out.println("Saliendo del sistema.");
                default -> System.out.println("Opcion invalida.");
            }
        } catch (SQLException error) {
            System.out.println("Ocurrio un error al acceder a la base de datos.");
            System.out.println("Detalle: " + error.getMessage());
        }
    }

    private void registrarPaciente() throws SQLException {
        if (obraSocialDAO.listarTodas().isEmpty()) {
            System.out.println("Debe registrar al menos una obra social antes de registrar pacientes.");
            return;
        }

        System.out.println();
        System.out.println("--- Registrar paciente ---");
        String nombre = leerTexto("Nombre: ");
        String dni = leerTexto("DNI: ");
        ObraSocial obraSocial = seleccionarObraSocial();

        Paciente paciente = pacienteDAO.guardar(nombre, dni, obraSocial);
        System.out.println("Paciente registrado con ID " + paciente.getId() + ".");
    }

    private void registrarObraSocial() throws SQLException {
        System.out.println();
        System.out.println("--- Registrar obra social ---");
        String nombre = leerTexto("Nombre: ");
        double porcentaje = leerPorcentaje("Porcentaje de cobertura: ");

        ObraSocial obraSocial = obraSocialDAO.guardar(nombre, porcentaje);
        System.out.println("Obra social registrada con ID " + obraSocial.getId() + ".");
    }

    private void registrarProfesional() throws SQLException {
        System.out.println();
        System.out.println("--- Registrar profesional ---");
        String nombre = leerTexto("Nombre: ");
        String especialidad = leerTexto("Especialidad: ");
        String matricula = leerTexto("Matricula: ");

        Profesional profesional = profesionalDAO.guardar(nombre, especialidad, matricula);
        System.out.println("Profesional registrado con ID " + profesional.getId() + ".");
    }

    private void registrarAsistencia() throws SQLException {
        if (pacienteDAO.listarTodos().isEmpty() || profesionalDAO.listarTodos().isEmpty()) {
            System.out.println("Debe registrar al menos un paciente y un profesional antes de crear una asistencia.");
            return;
        }

        System.out.println();
        System.out.println("--- Registrar asistencia medica ---");
        Paciente paciente = seleccionarPaciente();
        Profesional profesional = seleccionarProfesional();
        LocalDateTime fechaHora = leerFechaHora();

        System.out.println("Tipos de asistencia:");
        System.out.println("1. Consulta general");
        System.out.println("2. Emergencia");
        System.out.println("3. Telemedicina");
        int tipo = leerEntero("Seleccione el tipo: ");

        AsistenciaMedica asistencia = crearAsistencia(tipo, paciente, profesional, fechaHora);
        if (asistencia == null) {
            System.out.println("No se registro la asistencia porque el tipo elegido no es valido.");
            return;
        }

        System.out.println("Asistencia registrada con ID " + asistencia.getId() + ".");
    }

    private AsistenciaMedica crearAsistencia(int tipo, Paciente paciente, Profesional profesional, LocalDateTime fechaHora)
            throws SQLException {
        return switch (tipo) {
            case 1 -> {
                String consultorio = leerTexto("Consultorio: ");
                yield asistenciaDAO.guardarConsultaGeneral(paciente, profesional, fechaHora, consultorio);
            }
            case 2 -> {
                NivelUrgencia nivel = leerNivelUrgencia();
                yield asistenciaDAO.guardarEmergencia(paciente, profesional, fechaHora, nivel);
            }
            case 3 -> {
                String enlace = leerTexto("Enlace de videollamada: ");
                yield asistenciaDAO.guardarTelemedicina(paciente, profesional, fechaHora, enlace);
            }
            default -> null;
        };
    }

    private Paciente seleccionarPaciente() throws SQLException {
        List<Paciente> pacientes = pacienteDAO.listarTodos();
        System.out.println("Pacientes disponibles:");
        pacientes.forEach(paciente -> System.out.println(paciente.getId() + ". " + paciente.getNombre()
                + " | DNI: " + paciente.getDni()
                + " | Obra social: " + paciente.getObraSocial().getNombre()
                + " (" + paciente.getObraSocial().getPorcentajeCobertura() + "%)"));

        while (true) {
            int id = leerEntero("ID del paciente: ");
            Optional<Paciente> paciente = pacientes.stream()
                    .filter(item -> item.getId() == id)
                    .findFirst();
            if (paciente.isPresent()) {
                return paciente.get();
            }
            System.out.println("No existe un paciente con ese ID.");
        }
    }

    private ObraSocial seleccionarObraSocial() throws SQLException {
        List<ObraSocial> obrasSociales = obraSocialDAO.listarTodas();
        System.out.println("Obras sociales disponibles:");
        obrasSociales.forEach(obraSocial -> System.out.println(obraSocial.getId() + ". " + obraSocial.getNombre()
                + " | Cobertura: " + obraSocial.getPorcentajeCobertura() + "%"));

        while (true) {
            int id = leerEntero("ID de la obra social: ");
            Optional<ObraSocial> obraSocial = obrasSociales.stream()
                    .filter(item -> item.getId() == id)
                    .findFirst();
            if (obraSocial.isPresent()) {
                return obraSocial.get();
            }
            System.out.println("No existe una obra social con ese ID.");
        }
    }

    private Profesional seleccionarProfesional() throws SQLException {
        List<Profesional> profesionales = profesionalDAO.listarTodos();
        System.out.println("Profesionales disponibles:");
        profesionales.forEach(profesional -> System.out.println(profesional.getId() + ". " + profesional.getNombre()
                + " | " + profesional.getEspecialidad()));

        while (true) {
            int id = leerEntero("ID del profesional: ");
            Optional<Profesional> profesional = profesionales.stream()
                    .filter(item -> item.getId() == id)
                    .findFirst();
            if (profesional.isPresent()) {
                return profesional.get();
            }
            System.out.println("No existe un profesional con ese ID.");
        }
    }

    private NivelUrgencia leerNivelUrgencia() {
        System.out.println("Niveles de urgencia:");
        System.out.println("1. BAJA");
        System.out.println("2. MEDIA");
        System.out.println("3. ALTA");

        while (true) {
            int opcion = leerEntero("Seleccione el nivel: ");
            switch (opcion) {
                case 1:
                    return NivelUrgencia.BAJA;
                case 2:
                    return NivelUrgencia.MEDIA;
                case 3:
                    return NivelUrgencia.ALTA;
                default:
                    System.out.println("Nivel invalido.");
            }
        }
    }

    private LocalDateTime leerFechaHora() {
        while (true) {
            String valor = leerTexto("Fecha y hora (yyyy-MM-dd HH:mm): ");
            try {
                return LocalDateTime.parse(valor, FORMATO_ENTRADA);
            } catch (DateTimeParseException error) {
                System.out.println("Formato invalido. Ejemplo valido: 2026-06-03 10:30");
            }
        }
    }

    private void listarPendientes() throws SQLException {
        System.out.println();
        System.out.println("--- Asistencias pendientes ---");
        List<AsistenciaMedica> pendientes = asistenciaDAO.listarPendientes();
        if (pendientes.isEmpty()) {
            System.out.println("No hay asistencias pendientes.");
            return;
        }
        pendientes.forEach(asistencia -> System.out.println(asistencia.resumen()));
    }

    private void atenderPendientes() throws SQLException {
        System.out.println();
        System.out.println("--- Atencion de asistencias pendientes ---");
        List<AsistenciaMedica> pendientes = asistenciaDAO.listarPendientes();
        if (pendientes.isEmpty()) {
            System.out.println("No hay asistencias pendientes para atender.");
            return;
        }

        for (AsistenciaMedica asistencia : pendientes) {
            asistencia.atender();
            asistenciaDAO.marcarAtendida(asistencia.getId());
        }
    }

    private void buscarAsistencia() throws SQLException {
        int id = leerEntero("ID de la asistencia: ");
        asistenciaDAO.buscarPorId(id)
                .ifPresentOrElse(
                        asistencia -> System.out.println(asistencia.resumen()),
                        () -> System.out.println("No existe una asistencia con ese ID.")
                );
    }

    private void listarTodas() throws SQLException {
        System.out.println();
        System.out.println("--- Todas las asistencias ---");
        List<AsistenciaMedica> asistencias = asistenciaDAO.listarTodas();
        if (asistencias.isEmpty()) {
            System.out.println("No hay asistencias registradas.");
            return;
        }
        asistencias.forEach(asistencia -> System.out.println(asistencia.resumen()));
    }

    private void listarObrasSociales() throws SQLException {
        System.out.println();
        System.out.println("--- Obras sociales ---");
        List<ObraSocial> obrasSociales = obraSocialDAO.listarTodas();
        if (obrasSociales.isEmpty()) {
            System.out.println("No hay obras sociales registradas.");
            return;
        }
        obrasSociales.forEach(obraSocial -> System.out.println(obraSocial.getId() + ". " + obraSocial.getNombre()
                + " | Cobertura: " + obraSocial.getPorcentajeCobertura() + "%"));
    }

    private int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = scanner.nextLine();
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException error) {
                System.out.println("Debe ingresar un numero.");
            }
        }
    }

    private double leerPorcentaje(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = scanner.nextLine();
            try {
                double porcentaje = Double.parseDouble(valor);
                if (porcentaje >= 0 && porcentaje <= 100) {
                    return porcentaje;
                }
                System.out.println("El porcentaje debe estar entre 0 y 100.");
            } catch (NumberFormatException error) {
                System.out.println("Debe ingresar un numero.");
            }
        }
    }

    private String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = scanner.nextLine().trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("El valor no puede estar vacio.");
        }
    }
}
