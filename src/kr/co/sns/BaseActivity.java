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
 * 상수 및 기본 메소드 정의를 정의하며
 * FACEBOOK API 사용시 항상 사용되는 facebook
 */
public class BaseActivity extends Activity {
	/*
	 * 페이스북 앱 사용 인증에 사용될 앱 아이디 키 생성 방법은 아래 형식과 같다. 실제 릴리즈할때는 디버그 키가 아니라 릴리즈 배포키를
	 * 사용해야된다. example) $ keytool -exportcert -alias androiddebugkey -keystore
	 * "C:\Documents and Settin gs\ddononi\.android\debug.keystore" | openssl
	 * sha1 -binary | openssl base64 생성된 키값을 페이스북 개발자 페이지
	 * https://developers.facebook.com/apps 에 등록하면 앱아이디를 얻을수있다.
	 */								
	public static final String APP_ID = "102239696559238"; // 페이스북 인증에 사용될 앱 아이디
	public static final String SHARED_PREFERENCE = "mypreference";
	// 페이스북을 사용할려면 반드시 생성되어야할 기본 객체
	public static final Facebook facebook = new Facebook(APP_ID);
	private boolean isTwoClickBack = false; // 두번클릭 종료여부
	public static final int ZOOM_DEEP = 17; // 지도 줌 레벨

	/*
	 * 접근에 사용될 퍼미션
	 * http://developers.facebook.com/docs/reference/api/permissions/
	 */
	public static final String[] PERMISSIONS = new String[] { "publish_stream",
			"read_stream", "offline_access", "user_checkins", "user_photos", "publish_checkins", "photo_upload" };

	/**
	 * 메뉴키를 누를때 발생되는 옵션메뉴 처리
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

		case R.id.logout:		// 로그아웃
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(facebook);
			asyncRunner.logout(this.getBaseContext(),
					new LogoutRequestListener());
			Log.d("myfacebook", "로그아웃!");
			return true;

		case R.id.mefeed:		// 내 담벼락
			intent = new Intent(getBaseContext(), MyProfile.class);
			startActivity(intent);
			finish();
			return true;
		case R.id.getfriends:	// 친구목록
			intent = new Intent(getBaseContext(), MyFriends.class);
			startActivity(intent);
			finish();
			return true;
		case R.id.findfriends:	// 친구찿기
			SharedPreferences mPrefs = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
			String url = "http://m.facebook.com/profile.php#!/findfriends.php?ref=bookmark&_user="
					+ mPrefs.getString("id","");
			// 모바일 형식으로 url을 구성한다.
			intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW); // 웹페이즈를 뛰울 인텐트 설정
			intent.setData(Uri.parse(url)); // url 설정
			startActivity(intent);
			return true;

		case R.id.checkins:		// 체크인
			intent = new Intent(getBaseContext(), CheckInActivity.class);
			startActivity(intent);
			return true;

		case R.id.setting:		// 설정
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
			// ui thread로 처리해야 한다.
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(getBaseContext(), "로그아웃 되었습니다.",
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

	/** 백버튼 두번이면 앱 종료 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * back 버튼이면 타이머(2초)를 이용하여 다시한번 뒤로 가기를 누르면 어플리케이션이 종료 되도록한다.
		 */
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (!isTwoClickBack) {
					Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르면 종료됩니다.",
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

	// 뒤로가기 종료를 위한 타이머
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
	 * 로딩중에 화면을 회전하면 에러가 발생하기 때문에
	 * 완료가 될때까지 화면을 잠근다.
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
	 * 화면 잠금 해제
	 */
	public void unLockScreenRotation() {
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

}
