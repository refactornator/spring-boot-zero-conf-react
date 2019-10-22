package cool.william;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

class SetupFrontendTask extends DefaultTask {

    private String projectDirectory = System.getProperty("user.dir");

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

            appendToGitIgnore();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void appendToGitIgnore() {
        String content =
                "### Frontend Dependencies ###\n" +
                "node_modules/\n\n" +
                "### Frontend Bundle ###\n" +
                "src/main/resources/static/*.js\n" +
                "src/main/resources/templates/*.html\n";
        Path gitIgnore = Paths.get(projectDirectory, ".gitignore");
        try {
            if(!FileUtils.readFileToString(gitIgnore.toFile(), UTF_8).contains(content)) {
                Files.writeString(gitIgnore, content, CREATE, APPEND);
            }
        } catch (IOException e) {
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
