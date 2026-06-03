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
    public Paciente guardar(String nombre, String dni, String cobertura) throws SQLException {
        String sql = "INSERT INTO pacientes (nombre, dni, cobertura) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, nombre);
            sentencia.setString(2, dni);
            sentencia.setString(3, cobertura);
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return new Paciente(claves.getInt(1), nombre, dni, cobertura);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID generado para el paciente.");
    }

    public List<Paciente> listarTodos() throws SQLException {
        String sql = "SELECT id, nombre, dni, cobertura FROM pacientes ORDER BY id";
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
        String sql = "SELECT id, nombre, dni, cobertura FROM pacientes WHERE id = ?";

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
        return new Paciente(
                resultado.getInt("id"),
                resultado.getString("nombre"),
                resultado.getString("dni"),
                resultado.getString("cobertura")
        );
    }
}
