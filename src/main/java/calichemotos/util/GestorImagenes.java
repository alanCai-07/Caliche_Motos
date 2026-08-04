package calichemotos.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class GestorImagenes {

    private static final String CARPETA_BASE = "imagenes/repuestos";

    private GestorImagenes() {
    }

    /**
     * Copia la imagen seleccionada a la carpeta del proyecto con el codigo del
     * repuesto como nombre.
     */
    public static String guardarImagenRepuesto(String idRepuesto, File origen) throws IOException {
        File dirDestino = new File(CARPETA_BASE);
        if (!dirDestino.exists())
            dirDestino.mkdirs();

        String extension = obtenerExtension(origen.getName());
        String nombreDestino = idRepuesto + "." + extension;
        Path destino = Path.of(CARPETA_BASE, nombreDestino);

        Files.copy(origen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

        // Se guarda como ruta relativa, compatible al abrir el archivo desde
        // cualquier parte del codigo.
        return CARPETA_BASE + "/" + nombreDestino;
    }

    private static String obtenerExtension(String nombreArchivo) {
        int i = nombreArchivo.lastIndexOf('.');
        return (i > 0) ? nombreArchivo.substring(i + 1).toLowerCase() : "png";
    }

    /** Devuelve el File de la imagen o null si no existe o no esta definida. */
    public static File obtenerArchivo(String rutaImagen) {
        if (rutaImagen == null)
            return null;
        rutaImagen = rutaImagen.trim();
        if (rutaImagen.isBlank())
            return null;

        String rutaNormalizada = rutaImagen.replace("/", File.separator).replace("\\", File.separator);

        File archivo = new File(rutaImagen);
        if (archivo.exists())
            return archivo;

        File directo = new File(System.getProperty("user.dir"), rutaNormalizada);
        if (directo.exists())
            return directo;

        Path carpetaActual = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path rutaBuscada = carpetaActual.resolve(rutaNormalizada).normalize();
        if (rutaBuscada.toFile().exists())
            return rutaBuscada.toFile();

        Path actual = carpetaActual;
        while (actual.getParent() != null) {
            actual = actual.getParent();
            Path posible = actual.resolve(rutaNormalizada).normalize();
            if (posible.toFile().exists())
                return posible.toFile();
        }

        return null;
    }
}
