package filnik.stargazers.test;

/**
 * Created by fil on 06/02/17.
 */

import filnik.stargazers.*;
import filnik.stargazers.R;

public abstract class DownloadActivity extends BaseTestActivity {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testRun() {
        assertTrue("wrong Activity", solo.waitForActivity(MainActivity.class, 2000));
        setWifiEnable(isOnline());
        solo.waitForLogMessage("setWifiEnabled: " + (isOnline() ? "true" : "false"));
        solo.clearEditText((android.widget.EditText) solo.getView(filnik.stargazers.R.id.nickname));
        solo.enterText((android.widget.EditText) solo.getView(filnik.stargazers.R.id.nickname), "filnik");
        solo.clearEditText((android.widget.EditText) solo.getView(filnik.stargazers.R.id.repository));
        solo.enterText((android.widget.EditText) solo.getView(filnik.stargazers.R.id.repository), "AndroidSmoothAccordion");
        solo.clickOnView(solo.getView(filnik.stargazers.R.id.download_button));
        assertTrue(solo.waitForText(solo.getString(isOnline() ? R.string.download_done : R.string.error_downloading), 1, 2000));
        solo.clickOnView(solo.getView(filnik.stargazers.R.id.action_refresh));
        assertTrue(solo.waitForText(solo.getString(isOnline() ? R.string.download_done : R.string.error_downloading), 1, 2000));
        if (isOnline()){
            assertTrue("data hasn't been downloaded properly", solo.searchText("1144077")); // it actually has downloaded my profile
        }
    }

    protected abstract boolean isOnline();
}
