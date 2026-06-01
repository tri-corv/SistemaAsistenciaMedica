package LSP;

public class Paciente {
    private final int id;
    private final String nombre;
    private final String dni;
    private final String cobertura;

    public Paciente(int id, String nombre, String dni, String cobertura) {
        this.id = id;
        this.nombre = nombre;
        this.dni = dni;
        this.cobertura = cobertura;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDni() {
        return dni;
    }

    public String getCobertura() {
        return cobertura;
    }
}
