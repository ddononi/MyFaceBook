package kr.co.sns;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class MyService extends Service implements Runnable{
	// �� �㺭�� �Խù� ������ ����ִ� uri
	private String requestUrl = "https://graph.facebook.com/me/feed?access_token=";
	private SharedPreferences mPrefs;			// ����ȯ�� ����
	private Handler handler = new Handler();	// ������Ʈ �ڵ鷯	
	private int repeatTime;						// ������Ʈ �ֱ�
	
	// url source
	private URL url;
	private InputStream is = null;
	private DataInputStream dis = null;	
	
	// �㺭�������� ����  json
	private JSONObject object = null;
	private JSONArray dataArr = null;	
	
	private String oldId;	// ���� ���̵�
	/** ���񽺰� ����ɶ�  */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		 * ���� ȯ�� �������� �׼��� ��ū ��������
		 */
		mPrefs = getSharedPreferences(BaseActivity.SHARED_PREFERENCE, MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		
		// setting ���� ������Ʈ �ֱ⸦ �����´�.
		SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		repeatTime = Integer.valueOf(defaultSharedPref.getString("repeat", "10"));	
		//before.		
		if (access_token != null) {
			requestUrl += access_token;
		}
		try {
			oldId = checkUpdateWall();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ������������ ���̵� ����
		handler.postDelayed(this, 1000 * 60 * repeatTime );
		Log.d("myfacebook", "starting service!!");
		return 0;
	}
	
	/**
	 * url �� ������ �����´�.
	 */
	private String getUrlSource(){
		String s;
		StringBuilder sb = new StringBuilder();
		try {
			url = new URL(requestUrl);
			is = url.openStream(); // throws an IOException
			dis = new DataInputStream(new BufferedInputStream(is));
			while ((s = dis.readLine()) != null) {
				sb.append(s);
			}
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException ioe) {
			}
		}
		return sb.toString();
	}

	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("dservice", "stop!");
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			if(oldId == null){	
				oldId = checkUpdateWall();
				return;
			}
			
			// ���̵��� ������ �Խù��� �߰��� ������ ���ٰ� �Ǵ�
			if( oldId.equals(checkUpdateWall())){
				handler.postDelayed(this, 1000 * 60 * repeatTime );
			}else{
				oldId = checkUpdateWall();
				Log.d("myfacebook","do receiver");
				// ��ε��ɽ�Ʈ ���ù��� ���� �ҵ�����Ʈ, ���� �ҵ�����Ʈ�� ������ ����ϰ� ���� ����
				Intent i = new Intent(getBaseContext(), MyReceiver.class);
				//i.putExtra("id",checkUpdateWall());	// ���̵𰪵� ���� ������.
				PendingIntent sender = PendingIntent.getBroadcast(getBaseContext(),
						0,  i, PendingIntent.FLAG_CANCEL_CURRENT);				
				sender.send();		// ��ε��ɽ���
				handler.postDelayed(this, 1000 * 60 * repeatTime );
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 *  �� �㺭��������� üũ
	 * @return
	 * 	ù��° �Խù� ���̵�
	 * @throws JSONException 
	 */
	private String checkUpdateWall() throws JSONException{
		try {
			object = new JSONObject(getUrlSource());
			dataArr = object.getJSONArray("data");
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("myfacebook","error");
			return null;
		}

		if(dataArr == null){
			return null;
		}
		// �迭�� ù��° ���� ����
        JSONObject object = dataArr.getJSONObject(0);
        return object.getString("id");	// ���̵� ��ȯ
	}

}
