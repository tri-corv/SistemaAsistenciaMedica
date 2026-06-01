# Aplicacion del principio LSP

El sistema usa `AsistenciaMedica` como contrato base. `ConsultaGeneral`, `Emergencia` y `Telemedicina` heredan de esa clase y pueden sustituirla sin cambiar el comportamiento esperado del sistema.

La clase `GestorAsistencias` trabaja con una lista de `AsistenciaMedica` y ejecuta `AsistenciaMedica::atender` sin preguntar que subtipo concreto recibio. Cada subtipo solo cambia su tipo e indicacion especifica, pero todos respetan el mismo contrato.

Esto evita condiciones como `if asistencia es telemedicina`, metodos vacios o excepciones por operaciones no soportadas.
