package ca.cgjennings.apps.arkham.plugins;

import ca.cgjennings.apps.arkham.StrangeEons;
import ca.cgjennings.ui.textedit.CodeType;
import ca.cgjennings.apps.arkham.project.Member;
import ca.cgjennings.apps.arkham.project.Project;
import ca.cgjennings.apps.arkham.project.ProjectUtilities;
import ca.cgjennings.apps.arkham.project.Task;
import ca.cgjennings.apps.arkham.project.TaskAction;
import java.io.File;
import java.nio.charset.Charset;

/**
 * Project action that allows changing the encoding of a text file.
 *
 * @author Chris Jennings <https://cgjennings.ca/contact>
 */
public class ChangeEncodingAction extends TaskAction {

    public static final String ACTION_NAME = "Change Text Encoding";

    @Override
    public String getLabel() {
        return ACTION_NAME + "…";
    }

    @Override
    public boolean perform(Project project, Task task, Member member) {
        Charset cs = member.getMetadataSource().getDefaultCharset(member);
        ChangeEncodingDialog d = new ChangeEncodingDialog(StrangeEons.getWindow(), member.getFile(), cs);
        d.setVisible(true);
        return true;
    }

    @Override
    public boolean appliesTo(Project project, Task task, Member member) {
        if (member == null) {
            return false;
        }
        return guessCodeType(member.getFile()) != null;
    }

    static CodeType guessCodeType(File f) {
        if (f != null) {
            String ext = ProjectUtilities.getFileExtension(f);
            for (int i = 0; i < types.length; ++i) {
                if (types[i].getExtension().equals(ext)) {
                    return types[i];
                }
            }
        }
        return null;
    }
    private static final CodeType[] types = CodeType.values();
}
