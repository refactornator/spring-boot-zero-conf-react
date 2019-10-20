package cool.william;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class SetupFrontendTask extends DefaultTask {

    public SetupFrontendTask() {
    }

    @TaskAction
    void setupReactFrontend() {
        try {
            String rootName = "/react";
            URI rootUri = getClass().getResource(rootName).toURI();
            Path rootPath;
            if (rootUri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(rootUri, Collections.<String, Object>emptyMap());
                rootPath = fileSystem.getPath(rootName);
            } else {
                rootPath = Paths.get(rootUri);
            }

            Files.walk(rootPath)
                    .filter(path -> !path.equals(rootPath))
                    .forEach(path -> {
                        InputStream resourceAsStream = getClass().getResourceAsStream(path.toString());
                        Path currentDirectory = Paths.get(path.getFileName().toString()).toAbsolutePath().normalize();
                        copy(resourceAsStream, currentDirectory);
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copy(InputStream source, Path destination) {
        try {
            Files.copy(source, destination, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void makeDirectory(String directory) {
        Path path = Paths.get(directory);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createFileFromResource(String fileToCreate, String resourceFilename) {
        try {
            Path resourcePath = Paths.get(getClass().getClassLoader().getResource(resourceFilename).toURI());
            byte[] fileBytes = Files.readAllBytes(resourcePath);
            Files.write(Paths.get(fileToCreate), fileBytes);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
