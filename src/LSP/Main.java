package LSP;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        GestorAsistencias gestor = new GestorAsistencias();

        Paciente pacienteUno = new Paciente(1, "Ana Gomez", "35123456", "OSDE");
        Paciente pacienteDos = new Paciente(2, "Luis Perez", "28777888", "Swiss Medical");
        Paciente pacienteTres = new Paciente(3, "Marta Ruiz", "40999111", "Particular");

        Profesional clinica = new Profesional(1, "Dra. Laura Medina", "Clinica medica", "MP-1020");
        Profesional guardia = new Profesional(2, "Dr. Pablo Torres", "Emergentologia", "MP-7781");
        Profesional virtual = new Profesional(3, "Dra. Sofia Ramos", "Medicina familiar", "MP-4403");

        gestor.registrar(new ConsultaGeneral(1, pacienteUno, clinica, LocalDateTime.of(2026, 6, 3, 10, 30), "A12"));
        gestor.registrar(new Emergencia(2, pacienteDos, guardia, LocalDateTime.of(2026, 6, 1, 14, 0), NivelUrgencia.ALTA));
        gestor.registrar(new Telemedicina(3, pacienteTres, virtual, LocalDateTime.of(2026, 6, 4, 18, 15), "https://meet.salud.local/consulta-3"));

        System.out.println("ASISTENCIAS PENDIENTES");
        gestor.listarPendientes().forEach(asistencia -> System.out.println(asistencia.resumen()));

        System.out.println();
        System.out.println("ATENCION DE ASISTENCIAS");
        gestor.atenderPendientes();
    }
}
