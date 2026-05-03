package ca.cgjennings.apps.arkham.plugins;

import ca.cgjennings.apps.arkham.project.NewTaskType;
import ca.cgjennings.apps.arkham.project.Project;
import ca.cgjennings.apps.arkham.project.ProjectUtilities;
import ca.cgjennings.apps.arkham.project.Task;
import java.io.File;
import java.io.IOException;

/**
 * Task type for creating catalog tasks. Used to create private plug-in catalogs
 * or test how plug-ins will appear in a catalog.
 *
 * @author Christopher G. Jennings (<https://cgjennings.ca/contact>)
 */
final class CatToolsTaskType extends NewTaskType {

    @Override
    public String getLabel() {
        return "Local Catalogue";
    }

    @Override
    public String getDescription() {
        return "Create a local plug-in catalogue to test how your plug-in will appear in "
                + "the official catalogue.<p>Can also be used to prepare a private catalogue "
                + "that can be uploaded to your own server.";
    }

    @Override
    public String getType() {
        return "CATALOGUE";
    }

    @Override
    public String getIconResource() {
        return "cgj/devtools/task.png";
    }

    @Override
    public boolean initializeNewTask(Project project, Task task) throws IOException {
        File parent = task.getFile();
        new File(parent, "Local Catalog").mkdirs();
        new File(parent, "Staging Area").mkdir();
        new File(parent, "Upload Queue").mkdir();
        copy(parent, "Clear Upload Queue.ajs");
        copy(parent, "Publish to Local Catalog.ajs");
        copy(parent, "View Local Catalog.ajs");
        copy(parent, "quick start guide.txt");
        return true;
    }

    private void copy(File folder, String file) throws IOException {
        ProjectUtilities.copyResourceToFile(
                "cgj/devtools/" + file,
                new File(folder, file)
        );
    }
}
