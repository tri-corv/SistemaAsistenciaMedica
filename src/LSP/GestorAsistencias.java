package LSP;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestorAsistencias {
    private final List<AsistenciaMedica> asistencias = new ArrayList<>();

    public void registrar(AsistenciaMedica asistencia) {
        asistencias.add(asistencia);
    }

    public List<AsistenciaMedica> listarTodas() {
        return List.copyOf(asistencias);
    }

    public List<AsistenciaMedica> listarPendientes() {
        return asistencias.stream()
                .filter(asistencia -> asistencia.getEstado() == EstadoAsistencia.PENDIENTE)
                .toList();
    }

    public Optional<AsistenciaMedica> buscarPorId(int id) {
        return asistencias.stream()
                .filter(asistencia -> asistencia.getId() == id)
                .findFirst();
    }

    public void atenderPendientes() {
        listarPendientes().forEach(AsistenciaMedica::atender);
    }
}
