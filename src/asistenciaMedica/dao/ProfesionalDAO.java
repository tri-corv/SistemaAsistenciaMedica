package asistenciaMedica.dao;

import asistenciaMedica.modelo.Profesional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfesionalDAO {
    public Profesional guardar(String nombre, String especialidad, String matricula) throws SQLException {
        String sql = "INSERT INTO profesionales (nombre, especialidad, matricula) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, nombre);
            sentencia.setString(2, especialidad);
            sentencia.setString(3, matricula);
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return new Profesional(claves.getInt(1), nombre, especialidad, matricula);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID generado para el profesional.");
    }

    public List<Profesional> listarTodos() throws SQLException {
        String sql = "SELECT id, nombre, especialidad, matricula FROM profesionales ORDER BY id";
        List<Profesional> profesionales = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                profesionales.add(mapear(resultado));
            }
        }

        return profesionales;
    }

    public Optional<Profesional> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, especialidad, matricula FROM profesionales WHERE id = ?";

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

    private Profesional mapear(ResultSet resultado) throws SQLException {
        return new Profesional(
                resultado.getInt("id"),
                resultado.getString("nombre"),
                resultado.getString("especialidad"),
                resultado.getString("matricula")
        );
    }
}
