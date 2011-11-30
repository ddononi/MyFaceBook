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
	// 내 담벼락 게시물 정보가 들어있는 uri
	private String requestUrl = "https://graph.facebook.com/me/feed?access_token=";
	private SharedPreferences mPrefs;			// 공유환경 설정
	private Handler handler = new Handler();	// 업데이트 핸들러	
	private int repeatTime;						// 업데이트 주기
	
	// url source
	private URL url;
	private InputStream is = null;
	private DataInputStream dis = null;	
	
	// 담벼락내용을 담을  json
	private JSONObject object = null;
	private JSONArray dataArr = null;	
	
	private String oldId;	// 이전 아이디값
	/** 서비스가 실행될때  */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		 * 공유 환경 설정에서 액세스 토큰 가져오기
		 */
		mPrefs = getSharedPreferences(BaseActivity.SHARED_PREFERENCE, MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		
		// setting 에서 업데이트 주기를 가져온다.
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
		// 일정간격으로 아이디값 추출
		handler.postDelayed(this, 1000 * 60 * repeatTime );
		Log.d("myfacebook", "starting service!!");
		return 0;
	}
	
	/**
	 * url 의 내용을 가져온다.
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
			
			// 아이디값이 같은면 게시물의 추가나 삭제가 없다고 판단
			if( oldId.equals(checkUpdateWall())){
				handler.postDelayed(this, 1000 * 60 * repeatTime );
			}else{
				oldId = checkUpdateWall();
				Log.d("myfacebook","do receiver");
				// 브로드케스트 리시버에 보낼 팬딩인텐트, 이전 팬딩인텐트가 있으면 취소하고 새로 실행
				Intent i = new Intent(getBaseContext(), MyReceiver.class);
				//i.putExtra("id",checkUpdateWall());	// 아이디값도 같이 보낸다.
				PendingIntent sender = PendingIntent.getBroadcast(getBaseContext(),
						0,  i, PendingIntent.FLAG_CANCEL_CURRENT);				
				sender.send();		// 브로드케스팅
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
	 *  내 담벼락변경사항 체크
	 * @return
	 * 	첫번째 게시물 아이디값
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
		// 배열의 첫번째 값을 추출
        JSONObject object = dataArr.getJSONObject(0);
        return object.getString("id");	// 아이디값 반환
	}

}
