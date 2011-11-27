package kr.co.sns;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.FacebookError;


/**
 *	single sign on (��������) �α��� ó�� Ŭ����
 */
public class StartActivity extends BaseActivity implements OnClickListener {
	private AsyncFacebookRunner mAsyncRunner; // �񵿱� ��û ó���� ���� ��ü

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_layout);

		mAsyncRunner = new AsyncFacebookRunner(facebook);

		// ������Ʈ ��ŷ
		Button loginBtn = (Button) findViewById(R.id.login);
		loginBtn.setOnClickListener(this);	// �α��ι�ư �̺�Ʈ ó��
	}

	/** �α��� ��ư ó�� */
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		// �α��� ���ó��
		facebook.authorize(StartActivity.this, PERMISSIONS, new LoginDialogListener());
		Log.d("myfacebook", "�α��� ó����!");

	}
	
	/** 
	 * �α��� ������ ó���� �ݹ� ó��  
	 * sso ó���� ��ȯ�Ǵ� requestCode ������ ����ó���� �Ѵ�.
	 */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("myfacebook", "onActivityResult(): " + requestCode);
        facebook.authorizeCallback(requestCode, resultCode, data);
      }	

	/**
	 * �α��δ��̾�α� ������ Ŭ����
	 */
	private final class LoginDialogListener implements
			com.facebook.android.Facebook.DialogListener {

		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			Log.d("myfacebook", "�α��� �Ϸ�~~!");
			doStartService();	// �˶����� ����
           
            // ���� ȯ�� ������ AccessToken �� AccessExpires�� �����Ѵ�.
            SharedPreferences mPrefs = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString("access_token", facebook.getAccessToken());
            editor.putLong("access_expires", facebook.getAccessExpires());
            Log.d("myfacebook", "access_token------->" + facebook.getAccessToken());
            editor.commit();  	// Ŀ������  ���� �Ϸ�          
            
			Intent intent = new Intent(StartActivity.this, MyProfile.class);
			startActivity(intent);	
			finish();	// ���� ��Ƽ��Ƽ�� ����

		}

		@Override
		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub
			Log.d("myfacebook", "�α��� ����!");
			new AlertDialog.Builder(StartActivity.this)
			.setMessage("�α��� ����").create().show();				
		}

		@Override
		public void onError(DialogError e) {
			// TODO Auto-generated method stub
			Log.d("myfacebook", "�α��� ����!");
			new AlertDialog.Builder(StartActivity.this)
			.setMessage("�α��� ����").create().show();				
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			Log.d("myfacebook", "�α��� ���!");
			new AlertDialog.Builder(StartActivity.this)
			.setMessage("�α��� ���").create().show();				
		}

		/**
		 * �㺭�� ������Ʈ�� �˸��� ���� ����
		 */
		private void doStartService() {
			// TODO Auto-generated method stub
			// ���񽺷� �˶�����
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(StartActivity.this);
			boolean isSetAlarm = sp.getBoolean("alarm", true);	
			if(isSetAlarm){
				Intent serviceIntent = new Intent(StartActivity.this, MyService.class);
				stopService(serviceIntent);
				startService(serviceIntent);
				Log.i("myfacebook", "service start!!");
			}	
		}		

	}
    
	/**
     * ����ȭ�鿡���� �ɼ� �޴� ó�� ����
     */
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		//return super.onCreateOptionsMenu(menu);
    	return false;
	}

    
}
