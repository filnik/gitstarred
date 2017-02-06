package filnik.stargazers.test;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import filnik.stargazers.MainActivity;

public abstract class BaseTestActivity extends ActivityInstrumentationTestCase2<MainActivity> {
  	protected Solo solo;

  	public BaseTestActivity() {
		super(MainActivity.class);
  	}

  	public void setUp() throws Exception {
        super.setUp();
		solo = new Solo(getInstrumentation());
		getActivity();
  	}

   	@Override
   	public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
  	}

	protected void setMobileDataEnabled(boolean enabled) {
		try {
			TelephonyManager tm = (TelephonyManager) solo.getCurrentActivity().getSystemService(Context.TELEPHONY_SERVICE);
			Method methodSet = Class.forName(tm.getClass().getName()).getDeclaredMethod( "setDataEnabled", Boolean.TYPE);
			methodSet.invoke(tm, enabled);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected void setWifiEnable(boolean enabled){
		WifiManager wifiManager = (WifiManager) solo.getCurrentActivity().getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(enabled);
	}
}
