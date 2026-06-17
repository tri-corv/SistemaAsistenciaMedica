package asistenciaMedica;

import asistenciaMedica.vista.Menu;
import asistenciaMedica.vista.VentanaPrincipal;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            if ("consola".equalsIgnoreCase(args[0])) {
                new Menu().iniciar();
                return;
            }

            if ("ventana".equalsIgnoreCase(args[0])) {
                VentanaPrincipal.mostrar();
                return;
            }
        }

        mostrarSelector();
    }

    private static void mostrarSelector() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== SISTEMA DE ASISTENCIAS MEDICAS ===");
        System.out.println("1. Abrir ventana principal");
        System.out.println("2. Usar menu por consola");
        System.out.print("Seleccione una opcion: ");

        String opcion = scanner.nextLine().trim();

        if ("2".equals(opcion)) {
            new Menu().iniciar();
        } else {
            VentanaPrincipal.mostrar();
        }
    }
}
