package kr.co.sns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * ��� �� �⺻ �޼ҵ� ���Ǹ� �����ϸ�
 * FACEBOOK API ���� �׻� ���Ǵ� facebook
 */
public class BaseActivity extends Activity {
	/*
	 * ���̽��� �� ��� ������ ���� �� ���̵� Ű ���� ����� �Ʒ� ���İ� ����. ���� �������Ҷ��� ����� Ű�� �ƴ϶� ������ ����Ű��
	 * ����ؾߵȴ�. example) $ keytool -exportcert -alias androiddebugkey -keystore
	 * "C:\Documents and Settin gs\ddononi\.android\debug.keystore" | openssl
	 * sha1 -binary | openssl base64 ������ Ű���� ���̽��� ������ ������
	 * https://developers.facebook.com/apps �� ����ϸ� �۾��̵� �������ִ�.
	 */								
	public static final String APP_ID = "102239696559238"; // ���̽��� ������ ���� �� ���̵�
	public static final String SHARED_PREFERENCE = "mypreference";
	// ���̽����� ����ҷ��� �ݵ�� �����Ǿ���� �⺻ ��ü
	public static final Facebook facebook = new Facebook(APP_ID);
	private boolean isTwoClickBack = false; // �ι�Ŭ�� ���Ῡ��
	public static final int ZOOM_DEEP = 17; // ���� �� ����

	/*
	 * ���ٿ� ���� �۹̼�
	 * http://developers.facebook.com/docs/reference/api/permissions/
	 */
	public static final String[] PERMISSIONS = new String[] { "publish_stream",
			"read_stream", "offline_access", "user_checkins" };

	/**
	 * �޴�Ű�� ������ �߻��Ǵ� �ɼǸ޴� ó��
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub

		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {

		case R.id.logout:		// �α׾ƿ�
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(facebook);
			asyncRunner.logout(this.getBaseContext(),
					new LogoutRequestListener());
			Log.d("myfacebook", "�α׾ƿ�!");
			return true;

		case R.id.mefeed:		// �� �㺭��
			intent = new Intent(getBaseContext(), MyProfile.class);
			startActivity(intent);
			finish();
			return true;
		case R.id.getfriends:	// ģ�����
			intent = new Intent(getBaseContext(), MyFriends.class);
			startActivity(intent);
			finish();
			return true;
		case R.id.findfriends:	// ģ���O��
			SharedPreferences mPrefs = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
			String url = "http://m.facebook.com/profile.php#!/findfriends.php?ref=bookmark&_user="
					+ mPrefs.getString("id","");
			// ����� �������� url�� �����Ѵ�.
			intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW); // ������� �ٿ� ����Ʈ ����
			intent.setData(Uri.parse(url)); // url ����
			startActivity(intent);
			return true;

		case R.id.checkins:		// üũ��
			intent = new Intent(getBaseContext(), CheckInActivity.class);
			startActivity(intent);
			return true;

		case R.id.setting:		// ����
			intent = new Intent(getBaseContext(), SettingActivity.class);
			startActivity(intent);
			return true;

		}
		return false;
	}	

	private class LogoutRequestListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			// TODO Auto-generated method stub
			// ui thread�� ó���ؾ� �Ѵ�.
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(getBaseContext(), "�α׾ƿ� �Ǿ����ϴ�.",
							Toast.LENGTH_SHORT).show();
				}

			});
			Intent intent = new Intent(getBaseContext(), StartActivity.class);
			startActivity(intent);
			finish();
		}

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub

		}

	}

	/** ���ư �ι��̸� �� ���� */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * back ��ư�̸� Ÿ�̸�(2��)�� �̿��Ͽ� �ٽ��ѹ� �ڷ� ���⸦ ������ ���ø����̼��� ���� �ǵ����Ѵ�.
		 */
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (!isTwoClickBack) {
					Toast.makeText(this, "'�ڷ�' ��ư�� �ѹ� �� ������ ����˴ϴ�.",
							Toast.LENGTH_SHORT).show();
					CntTimer timer = new CntTimer(2000, 1);
					timer.start();
				} else {
					moveTaskToBack(true);
					finish();
					return true;
				}

			}
		}
		return false;
	}

	// �ڷΰ��� ���Ḧ ���� Ÿ�̸�
	class CntTimer extends CountDownTimer {
		public CntTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			isTwoClickBack = true;
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			isTwoClickBack = false;
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			Log.i("Test", " isTwoClickBack " + isTwoClickBack);
		}

	}

	/**
	 * �ε��߿� ȭ���� ȸ���ϸ� ������ �߻��ϱ� ������
	 * �Ϸᰡ �ɶ����� ȭ���� ��ٴ�.
	 */
	public void mLockScreenRotation() {
		// Stop the screen orientation changing during an event
		switch (this.getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		}
	}

	/**
	 * ȭ�� ��� ����
	 */
	public void unLockScreenRotation() {
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

}
