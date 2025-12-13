package org.example.utils;

import lombok.experimental.UtilityClass;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@UtilityClass
public class ResourceUtils {

    public static InputStream getResourceAsStream(String path) {
        InputStream is = ResourceUtils.class
                .getClassLoader()
                .getResourceAsStream(path);

        return Objects.requireNonNull(is, "Resource not found: " + path);
    }

    public static String getResourceAsString(String path) {
        try (InputStream is = getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }

    public static String getResourceFullPath(String path) {
        return ResourceUtils.class.getClassLoader().getResource(path).getPath();
    }

    public static OutputStream getResourceAsOutputStream(String path) throws FileNotFoundException {
        return new FileOutputStream(Objects.requireNonNull(ResourceUtils.class.getClassLoader().getResource(path)).getPath());

    }

    public static byte[] getResourceAsBytes(String path) {
        try (InputStream is = getResourceAsStream(path)) {
            return is.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }
}
