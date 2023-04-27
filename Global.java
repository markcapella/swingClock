import java.util.HashMap;
import java.util.Map;

/** *********************************************************************
 * App State manager.
 */
public class Global {

    enum APPSTATE {
        NEVER_ACTIVE("NEVER_ACTIVE"),
        ALARM_NOT_SET("ALARM_NOT_SET"),
        SETTING_ALARM("SETTING_ALARM"),
        ALARM_SET("ALARM_SET"),
        ALARM_RINGING("ALARM_RINGING");

        final private String mState;
        APPSTATE(String state) {
            mState = state;
        }

        private static Map<String, APPSTATE> buildMap() {
            Map<String, APPSTATE> mapto = new HashMap<>();
            for (APPSTATE appState : APPSTATE.values()) {
                mapto.put(appState.mState, appState);
            }
            return mapto;
        }

        private static Map<String, APPSTATE> APP_STATE_MAP = buildMap();
        static APPSTATE appStateValueOf(String name) {
            return APP_STATE_MAP.get(name);
        }

        public String getStringValue() {
            return mState;
        }
    }
}
