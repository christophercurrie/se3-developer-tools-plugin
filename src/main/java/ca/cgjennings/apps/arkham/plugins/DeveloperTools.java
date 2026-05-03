package ca.cgjennings.apps.arkham.plugins;

import ca.cgjennings.apps.arkham.ContextBar;
import ca.cgjennings.apps.arkham.StrangeEons;
import ca.cgjennings.apps.arkham.StrangeEonsAppWindow;
import ca.cgjennings.apps.arkham.commands.AbstractCommand;
import ca.cgjennings.apps.arkham.commands.Commands;
import static ca.cgjennings.apps.arkham.plugins.Plugin.INJECTED;
import ca.cgjennings.apps.arkham.project.Actions;
import ca.cgjennings.apps.arkham.project.NewTaskType;
import ca.cgjennings.apps.arkham.project.TaskAction;
import gamedata.Expansion;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import resources.ResourceKit;

/**
 * The plug-in instance that installs/uninstalls the developer tool suites.
 *
 * @author Christopher G. Jennings (<https://cgjennings.ca/contact>)
 */
public class DeveloperTools extends AbstractPlugin {

    @Override
    public String getPluginName() {
        return "Developer Tools";
    }

    @Override
    public String getPluginDescription() {
        return "A suite of tools that support plug-in development";
    }

    @Override
    public float getPluginVersion() {
        return 3.0f;
    }

    @Override
    public int getPluginType() {
        return INJECTED;
    }

    @Override
    public void showPlugin(PluginContext context, boolean show) {
        if (loaded) {
            return;
        }

        // force the expansion symbol MIP map cache to register itself
        ca.cgjennings.apps.arkham.sheet.Sheet.getExpansionSymbol(Expansion.getBaseGameExpansion(), "0", 100);

        registerChangeEncodingAction(true);
        registerCatalogTask(true);
        registerRegionBoxesCommand(true);
        registerToolWindows(true);

        loaded = true;
    }

    @Override
    public void unloadPlugin() {
        registerChangeEncodingAction(false);
        registerCatalogTask(false);
        registerRegionBoxesCommand(false);
        registerToolWindows(false);

        loaded = false;
    }
    private boolean loaded;

    private void registerChangeEncodingAction(boolean register) {
        if (register) {
            if (changeEncodingAction == null) {
                changeEncodingAction = new ChangeEncodingAction();
                Actions.register(changeEncodingAction, Actions.PRIORITY_IMPORT_EXPORT);
            }
        } else {
            if (changeEncodingAction != null) {
                try {
                    Actions.unregister(changeEncodingAction);
                } finally {
                    changeEncodingAction = null;
                }
            }
        }
    }
    private TaskAction changeEncodingAction;

    private void registerCatalogTask(boolean register) {
        if (register) {
            if (catalogTask == null) {
                catalogTask = new CatToolsTaskType();
                NewTaskType.register(catalogTask);
            }
        } else {
            if (catalogTask != null) {
                try {
                    NewTaskType.unregister(catalogTask);
                } finally {
                    catalogTask = null;
                }
            }
        }
    }
    private NewTaskType catalogTask;

    private void registerRegionBoxesCommand(boolean register) {
        if (register) {
            if (regionBoxMenuItem != null) {
                registerRegionBoxesCommand(false);
            }

            Icon icon = ResourceKit.getIcon("/resources/cgj/devtools/region-boxes.png");
            Commands.VIEW_REGION_BOXES.putValue(AbstractCommand.SMALL_ICON, icon);
            regionBoxMenuItem = new JCheckBoxMenuItem(Commands.VIEW_REGION_BOXES);
            findViewMenu().add(regionBoxMenuItem);

            regionBoxButton = new ContextBar.CommandButton("REGIONS", Commands.VIEW_REGION_BOXES);
            ContextBar.registerButton(regionBoxButton);
        } else {
            if (regionBoxMenuItem != null) {
                findViewMenu().remove(regionBoxMenuItem);
                regionBoxMenuItem = null;
            }
            if (regionBoxButton != null) {
                ContextBar.unregisterButton(regionBoxButton);
                regionBoxButton = null;
            }
        }
    }
    private JCheckBoxMenuItem regionBoxMenuItem;
    private ContextBar.Button regionBoxButton;

    private JMenu findViewMenu() {
        StrangeEonsAppWindow w = StrangeEons.getWindow();
        JMenuBar bar = w.getJMenuBar();
        for (int i = 0; i < bar.getMenuCount(); ++i) {
            if ("viewMenu".equals(bar.getMenu(i).getName())) {
                return bar.getMenu(i);
            }
        }
        throw new AssertionError();
    }

    private void registerToolWindows(boolean register) {
        if (register) {
            if (toolWindows != null) {
                registerToolWindows(false);
            }

            final String[] toolTypes = new String[]{
                "Setting Explorer",
                "Cache Manager",
                "Memory Use",
                "Log Viewer",
                StrangeEons.getBuildNumber() > 4204 ? "Cut Corners" : null
            };

            toolWindows = new DevToolProxy[toolTypes.length];
            for (int p = 0; p < toolWindows.length; ++p) {
                if (toolTypes[p] != null) {
                    toolWindows[p] = new DevToolProxy(toolTypes[p], p);
                    StrangeEons.getWindow().startTracking(toolWindows[p]);
                } else {
                    toolWindows[p] = null;
                }
            }
        } else {
            if (toolWindows != null) {
                for (int i = 0; i < toolWindows.length; ++i) {
                    if (toolWindows[i] != null) {
                        StrangeEons.getWindow().stopTracking(toolWindows[i]);
                        toolWindows[i].unload();
                    }
                }
                toolWindows = null;
            }
        }
    }
    private DevToolProxy[] toolWindows;
}
