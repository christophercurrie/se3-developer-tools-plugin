package ca.cgjennings.apps.arkham.plugins;

import ca.cgjennings.apps.arkham.StrangeEons;
import ca.cgjennings.apps.arkham.StrangeEonsAppWindow;
import ca.cgjennings.apps.arkham.ToolWindow;
import ca.cgjennings.apps.arkham.TrackedWindowProxy;
import ca.cgjennings.ui.JUtilities;
import java.awt.Dialog;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import resources.ResourceKit;
import resources.Settings;

/**
 * A tracked window proxy that creates its target window on demand. The window
 * is created automatically using the supplied window title as the basis for the
 * name of the Panel class to load for the window content.
 *
 * @author Christopher G. Jennings (<https://cgjennings.ca/contact>)
 */
class DevToolProxy extends TrackedWindowProxy {

    protected ToolWindow tw;
    private float offsetMultiplier;

    public DevToolProxy(String windowTitle, float defaultWindowOffsetMultiplier) {
        super(windowTitle, ResourceKit.getIcon(
                "/resources/cgj/devtools/" + windowTitle.toLowerCase(Locale.ROOT).replace(' ', '-') + ".png")
        );
        offsetMultiplier = defaultWindowOffsetMultiplier;
    }

    @Override
    public Object createWindow() {
        final String className = getClass().getPackage().getName() + '.' + getTitle().replace(" ", "") + "Panel";
        try {
            tw = new ToolWindow(StrangeEons.getWindow(), getTitle(), Dialog.ModalityType.MODELESS);
            Class panelClass = Class.forName(className);
            JPanel content = (JPanel) panelClass.getConstructor(ToolWindow.class).newInstance(tw);
            tw.setBody(content);
            tw.setIcon(getIcon());
            if (!Settings.getUser().applyWindowSettings(windowPrefix(), tw)) {
                applyDefaultBounds();
            }
            return tw;
        } catch (InvocationTargetException | NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new AssertionError("failed to instantiate panel " + getTitle() + " (" + className + ')', ex);
        }
    }

    /**
     * Called when unloading the plug-in to allow it to perform any needed final
     * unload.
     */
    public void unload() {
        if (tw != null) {
            Settings.getUser().storeWindowSettings(windowPrefix(), tw);
            final JPanel body = tw.getBodyPanel();
            if (body instanceof UnloadablePanel) {
                ((UnloadablePanel) body).onUnload();
            }
            tw.close();
        }
    }

    private void applyDefaultBounds() {
        StrangeEonsAppWindow app = StrangeEons.getWindow();
        GraphicsConfiguration gc = app.getGraphicsConfiguration();
        Rectangle bounds = gc.getBounds();
        Insets i = app.getToolkit().getScreenInsets(gc);
        int w = BASE_WIN_WIDTH;
        int h = BASE_WIN_HEIGHT;
        int y = bounds.y + i.top + (int) (BASE_WIN_HEIGHT * offsetMultiplier) + 8;
        int x = bounds.x + bounds.width - i.right - w - 72;
        tw.setBounds(x, y, w, h);
        JUtilities.snapToDesktop(tw);
    }

    private static final int BASE_WIN_WIDTH = 500;
    private static final int BASE_WIN_HEIGHT = 200;

    private String windowPrefix() {
        return "devtools-" + getTitle().replace(" ", "").toLowerCase(Locale.CANADA);
    }

    /**
     * Interface which can be implemented by content panels if they need to know
     * when they are unloaded.
     */
    public static interface UnloadablePanel {

        void onUnload();
    }
}
