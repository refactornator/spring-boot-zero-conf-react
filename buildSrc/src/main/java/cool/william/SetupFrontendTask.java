package cool.william;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class SetupFrontendTask extends DefaultTask {

    public SetupFrontendTask() {
    }

    @TaskAction
    void setupReactFrontend() {
        try {
            String rootName = "react";
            URI rootUri = getClass().getClassLoader().getResource(rootName).toURI();
            Path rootPath;
            if (rootUri.getScheme().equals("jar")) {
                FileSystem fileSystem;
                try {
                    fileSystem = FileSystems.getFileSystem(rootUri);
                } catch (FileSystemNotFoundException e) {
                    fileSystem = FileSystems.newFileSystem(rootUri, Collections.<String, Object>emptyMap());
                }
                rootPath = fileSystem.getPath(rootName).toAbsolutePath();
            } else {
                rootPath = Paths.get(rootUri);
            }

            String projectDirectory = System.getProperty("user.dir");
            Files.walk(rootPath)
                    .filter(path -> !path.equals(rootPath))
                    .forEach(path -> {
                        String destination = path.subpath(1, path.getNameCount()).toString();
                        if (Files.isDirectory(path)) {
                            makeDirectory(destination);
                        } else {
                            InputStream resourceAsStream = getClass().getResourceAsStream(path.toString());
                            Path currentDirectory = Paths.get(projectDirectory, destination).toAbsolutePath().normalize();
                            copyFromStream(resourceAsStream, currentDirectory);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyFromStream(InputStream source, Path destination) {
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
}
