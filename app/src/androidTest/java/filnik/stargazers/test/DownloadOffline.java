package filnik.stargazers.test;

public class DownloadOffline extends DownloadActivity {

	@Override
	protected boolean isOnline() {
		return false;
	}
}
