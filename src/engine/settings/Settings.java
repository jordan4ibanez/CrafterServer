package engine.settings;
import static engine.disk.Disk.loadSettingsFromDisk;
import static engine.disk.Disk.saveSettingsToDisk;

public class Settings {

    private static SettingsObject settings;

    public static void loadSettings(){

        SettingsObject loadedSettings = loadSettingsFromDisk();

        if (loadedSettings == null) {
            //default if no settings set

            //instantiate new object
            settings = new SettingsObject();

            //save default values
            saveSettingsToDisk(settings);

        } else {

            //dump new settings in
            settings = loadedSettings;
        }
    }

    //settings saving
    public static void saveSettings(){
        saveSettingsToDisk(settings);
    }
}
