package asistenciaMedica.dao;

import asistenciaMedica.modelo.AsistenciaMedica;
import asistenciaMedica.modelo.ConsultaGeneral;
import asistenciaMedica.modelo.Emergencia;
import asistenciaMedica.modelo.EstadoAsistencia;
import asistenciaMedica.modelo.NivelUrgencia;
import asistenciaMedica.modelo.ObraSocial;
import asistenciaMedica.modelo.Paciente;
import asistenciaMedica.modelo.Profesional;
import asistenciaMedica.modelo.Telemedicina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AsistenciaMedicaDAO {
    private static final String SELECT_BASE = """
            SELECT a.id, a.tipo, a.fecha_hora, a.estado, a.consultorio, a.nivel_urgencia, a.enlace_videollamada,
                   p.id AS paciente_id, p.nombre AS paciente_nombre, p.dni AS paciente_dni,
                   os.id AS obra_social_id, os.nombre AS obra_social_nombre,
                   os.porcentaje_cobertura AS obra_social_porcentaje,
                   pr.id AS profesional_id, pr.nombre AS profesional_nombre, pr.especialidad AS profesional_especialidad,
                   pr.matricula AS profesional_matricula
            FROM asistencias_medicas a
            INNER JOIN pacientes p ON p.id = a.paciente_id
            INNER JOIN obras_sociales os ON os.id = p.obra_social_id
            INNER JOIN profesionales pr ON pr.id = a.profesional_id
            """;

    public ConsultaGeneral guardarConsultaGeneral(Paciente paciente, Profesional profesional, LocalDateTime fechaHora,
                                                  String consultorio) throws SQLException {
        int id = insertar(paciente, profesional, "CONSULTA_GENERAL", fechaHora, consultorio, null, null);
        return new ConsultaGeneral(id, paciente, profesional, fechaHora, consultorio);
    }

    public Emergencia guardarEmergencia(Paciente paciente, Profesional profesional, LocalDateTime fechaHora,
                                        NivelUrgencia nivelUrgencia) throws SQLException {
        int id = insertar(paciente, profesional, "EMERGENCIA", fechaHora, null, nivelUrgencia.name(), null);
        return new Emergencia(id, paciente, profesional, fechaHora, nivelUrgencia);
    }

    public Telemedicina guardarTelemedicina(Paciente paciente, Profesional profesional, LocalDateTime fechaHora,
                                            String enlaceVideollamada) throws SQLException {
        int id = insertar(paciente, profesional, "TELEMEDICINA", fechaHora, null, null, enlaceVideollamada);
        return new Telemedicina(id, paciente, profesional, fechaHora, enlaceVideollamada);
    }

    public List<AsistenciaMedica> listarTodas() throws SQLException {
        return listar(SELECT_BASE + " ORDER BY a.id");
    }

    public List<AsistenciaMedica> listarPendientes() throws SQLException {
        return listar(SELECT_BASE + " WHERE a.estado = 'PENDIENTE' ORDER BY a.id");
    }

    public Optional<AsistenciaMedica> buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE a.id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(mapear(resultado));
                }
            }
        }

        return Optional.empty();
    }

    public void marcarAtendida(int id) throws SQLException {
        String sql = "UPDATE asistencias_medicas SET estado = 'ATENDIDA' WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            sentencia.executeUpdate();
        }
    }

    private int insertar(Paciente paciente, Profesional profesional, String tipo, LocalDateTime fechaHora,
                         String consultorio, String nivelUrgencia, String enlaceVideollamada) throws SQLException {
        String sql = """
                INSERT INTO asistencias_medicas
                (paciente_id, profesional_id, tipo, fecha_hora, estado, consultorio, nivel_urgencia, enlace_videollamada)
                VALUES (?, ?, ?, ?, 'PENDIENTE', ?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, paciente.getId());
            sentencia.setInt(2, profesional.getId());
            sentencia.setString(3, tipo);
            sentencia.setTimestamp(4, Timestamp.valueOf(fechaHora));
            sentencia.setString(5, consultorio);
            sentencia.setString(6, nivelUrgencia);
            sentencia.setString(7, enlaceVideollamada);
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID generado para la asistencia.");
    }

    private List<AsistenciaMedica> listar(String sql) throws SQLException {
        List<AsistenciaMedica> asistencias = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                asistencias.add(mapear(resultado));
            }
        }

        return asistencias;
    }

    private AsistenciaMedica mapear(ResultSet resultado) throws SQLException {
        ObraSocial obraSocial = new ObraSocial(
                resultado.getInt("obra_social_id"),
                resultado.getString("obra_social_nombre"),
                resultado.getDouble("obra_social_porcentaje")
        );

        Paciente paciente = new Paciente(
                resultado.getInt("paciente_id"),
                resultado.getString("paciente_nombre"),
                resultado.getString("paciente_dni"),
                obraSocial
        );

        Profesional profesional = new Profesional(
                resultado.getInt("profesional_id"),
                resultado.getString("profesional_nombre"),
                resultado.getString("profesional_especialidad"),
                resultado.getString("profesional_matricula")
        );

        int id = resultado.getInt("id");
        LocalDateTime fechaHora = resultado.getTimestamp("fecha_hora").toLocalDateTime();
        String tipo = resultado.getString("tipo");

        AsistenciaMedica asistencia = switch (tipo) {
            case "CONSULTA_GENERAL" -> new ConsultaGeneral(
                    id,
                    paciente,
                    profesional,
                    fechaHora,
                    resultado.getString("consultorio")
            );
            case "EMERGENCIA" -> new Emergencia(
                    id,
                    paciente,
                    profesional,
                    fechaHora,
                    NivelUrgencia.valueOf(resultado.getString("nivel_urgencia"))
            );
            case "TELEMEDICINA" -> new Telemedicina(
                    id,
                    paciente,
                    profesional,
                    fechaHora,
                    resultado.getString("enlace_videollamada")
            );
            default -> throw new SQLException("Tipo de asistencia desconocido: " + tipo);
        };

        if (EstadoAsistencia.ATENDIDA.name().equals(resultado.getString("estado"))) {
            asistencia.marcarAtendida();
        }

        return asistencia;
    }
}
