package ca.cgjennings.apps.arkham.plugins;

import ca.cgjennings.apps.arkham.StrangeEons;
import java.io.IOException;
import java.util.logging.Level;
import resources.Settings;

/**
 * Used to display default settings in the Setting Explorer. This only
 * implements enough of the <code>Setting</code> API to be used by the Setting
 * Explorer.
 *
 * @author Christopher G. Jennings (<https://cgjennings.ca/contact>)
 * @since 3.0
 */
final class DefaultSettingsProxy extends Settings {

    public DefaultSettingsProxy() {
        try {
            addSettingsFrom("default.settings");
        } catch (IOException ex) {
            StrangeEons.log.log(Level.SEVERE, null, ex);
        }
    }
//	@Override
//	public Settings getParent() {
//		return super.getParent();
//	}

    @Override
    public void set(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset(String key) {
        throw new UnsupportedOperationException();
    }
}
