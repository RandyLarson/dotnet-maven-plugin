package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "nuget-add", defaultPhase = LifecyclePhase.INSTALL)
public class NugetAddMojo extends AbstractDotnetMojo {

    @Parameter(property = "nuget.add.source", required = false, defaultValue = "~/.nuget/packages")
    private String nugetAddSource;

    @Parameter(property = "nuget.add.enabled", required = false, defaultValue = "true")
    private boolean nugetAddEnabled;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!nugetAddEnabled) {
            getLog().debug("Disabled, skipping");
            return;
        }

        final PluginHelper helper = getPluginHelper();
        if (!helper.isNugetAvailable()) {
            getLog().warn("The [nuget] command is not available on path, skipping");
            return;
        }

        for (File subDirectory : helper.getProjectDirectories()) {
            try {
                final String targetPath;
                if (this.nugetAddSource == null || this.nugetAddSource.isEmpty()) {
                    // default
                    targetPath = System.getProperty("user.home") + "/.nuget/packages";
                } else if (this.nugetAddSource.startsWith("~")) {
                    targetPath = System.getProperty("user.home") + this.nugetAddSource.substring(1);
                } else {
                    targetPath = this.nugetAddSource;
                }
                final File nugetPackage = helper.getNugetPackage(subDirectory);
                final String nugetPackagePath = nugetPackage.getCanonicalPath();
                helper.executeCommand(subDirectory, "nuget", "add", nugetPackagePath, "-Source", targetPath);
            } catch (Exception e) {
                throw new MojoFailureException("Command [nuget add] failed!", e);
            }
        }
    }
}
