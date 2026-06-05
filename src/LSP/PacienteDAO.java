package LSP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PacienteDAO {
    public Paciente guardar(String nombre, String dni, ObraSocial obraSocial) throws SQLException {
        String sql = "INSERT INTO pacientes (nombre, dni, obra_social_id) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, nombre);
            sentencia.setString(2, dni);
            sentencia.setInt(3, obraSocial.getId());
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return new Paciente(claves.getInt(1), nombre, dni, obraSocial);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID generado para el paciente.");
    }

    public List<Paciente> listarTodos() throws SQLException {
        String sql = """
                SELECT p.id, p.nombre, p.dni,
                       os.id AS obra_social_id, os.nombre AS obra_social_nombre,
                       os.porcentaje_cobertura AS obra_social_porcentaje
                FROM pacientes p
                INNER JOIN obras_sociales os ON os.id = p.obra_social_id
                ORDER BY p.id
                """;
        List<Paciente> pacientes = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                pacientes.add(mapear(resultado));
            }
        }

        return pacientes;
    }

    public Optional<Paciente> buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT p.id, p.nombre, p.dni,
                       os.id AS obra_social_id, os.nombre AS obra_social_nombre,
                       os.porcentaje_cobertura AS obra_social_porcentaje
                FROM pacientes p
                INNER JOIN obras_sociales os ON os.id = p.obra_social_id
                WHERE p.id = ?
                """;

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

    private Paciente mapear(ResultSet resultado) throws SQLException {
        ObraSocial obraSocial = new ObraSocial(
                resultado.getInt("obra_social_id"),
                resultado.getString("obra_social_nombre"),
                resultado.getDouble("obra_social_porcentaje")
        );

        return new Paciente(
                resultado.getInt("id"),
                resultado.getString("nombre"),
                resultado.getString("dni"),
                obraSocial
        );
    }
}
