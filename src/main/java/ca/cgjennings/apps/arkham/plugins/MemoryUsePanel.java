package ca.cgjennings.apps.arkham.plugins;

import ca.cgjennings.apps.arkham.ToolWindow;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * The panel that combines the memory graph and readings bar to create the
 * memory use window.
 *
 * @author Christopher G. Jennings (<https://cgjennings.ca/contact>)
 */
final class MemoryUsePanel extends JPanel implements DevToolProxy.UnloadablePanel {

    public MemoryUsePanel(ToolWindow tw) {
        tw.setSize(400, 100);
        BorderLayout bl = new BorderLayout();
        setLayout(bl);
        MemoryReadings readings = new MemoryReadings();
        readings.updateStats();
        add(new MemoryGraph(tw, readings), BorderLayout.CENTER);
        add(readings, BorderLayout.SOUTH);
    }

    @Override
    public void onUnload() {
        ((MemoryGraph) getComponent(0)).dispose();
    }
}
