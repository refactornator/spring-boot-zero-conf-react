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
        npmInstall.ifPresent(setupFrontendTask::finalizedBy);

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
        bootRun.ifPresent(task -> task.dependsOn(startWebpackWatch));

        ExecFork webpackBuild = project.getTasks()
                .create("webpackBuild", ExecFork.class);
        webpackBuild.setExecutable("node_modules/.bin/webpack");
        webpackBuild.setArgs(Arrays.asList(new String[]{"--mode=production"}));
        webpackBuild.setWaitForOutput("Built at");
        npmInstall.ifPresent(webpackBuild::dependsOn);

        Optional<Task> bootJar = project.getTasksByName("bootJar", true)
                .stream()
                .findFirst();
        bootJar.ifPresent(task -> task.dependsOn(webpackBuild));
    }
}
