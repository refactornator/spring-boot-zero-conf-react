package cool.william;

import com.github.psxpaul.task.ExecFork;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Arrays;
import java.util.Optional;

public class SetupFrontendPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager()
                .apply("com.moowork.node");

        SetupFrontendTask setupFrontendTask = project.getTasks()
                .create("setupReactFrontend", SetupFrontendTask.class);

        Optional<Task> npmInstall = project.getTasksByName("npmInstall", true)
                .stream()
                .findFirst();
        if(npmInstall.isPresent()) {
            setupFrontendTask.finalizedBy(npmInstall.get());
        }

        project.getPluginManager()
                .apply("com.github.psxpaul.execfork");

        ExecFork startWebpackWatch = project.getTasks()
                .create("startWebpackWatch", ExecFork.class);
        startWebpackWatch.setExecutable("node_modules/.bin/webpack");
        startWebpackWatch.setArgs(Arrays.asList(new String[]{"--watch"}));
        startWebpackWatch.setWaitForOutput("Built at");
        npmInstall.ifPresent(startWebpackWatch::dependsOn);

        Optional<Task> bootRun = project.getTasksByName("bootRun", true)
                .stream()
                .findFirst();
        bootRun.ifPresent(startWebpackWatch::setStopAfter);

        Optional<Task> processResources = project.getTasksByName("processResources", true)
                .stream()
                .findFirst();
        processResources.ifPresent(task -> task.dependsOn(startWebpackWatch));
    }
}
