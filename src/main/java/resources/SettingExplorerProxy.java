package resources;

/**
 * Defines a helper method needed by the setting explorer tool. The method needs
 * to be defined in a separate class because it relies on a package-private
 * method in <code>RawSettings</code>.
 */
public final class SettingExplorerProxy {

    private SettingExplorerProxy() {
    }

    /**
     * If a setting has been explicitly defined at the top (i.e., it is
     * different from that setting's default value), global level, and it is not
     * overridden by the user settings, then this returns the explicitly set
     * value. Otherwise it returns <code>null</code>.
     *
     * @param key the key to look up
     * @param defaultValue the default value for the key defined in the default
     * settings file
     * @return the explicitly set value of the key, or <code>null</code>
     */
    public static String globalOverride(String key, String defaultValue) {
        String user = RawSettings.getUserSetting(key);
        String global = RawSettings.getGlobalSetting(key);
        if (global != null && !global.equals(user) && !global.equals(defaultValue)) {
            return global;
        }
        return null;
    }
}
