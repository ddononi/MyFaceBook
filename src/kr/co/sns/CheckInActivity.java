package kr.co.sns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.co.sns.MyProfile.MyFeedListener;
import kr.co.sns.MyProfile.MyProfileRequestListener;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

public class CheckInActivity extends BaseActivity{
	private AsyncFacebookRunner mAsyncRunner; 						// 비동기 요청 처리를 위한 객체
	private ProgressDialog progressDialog; 							// 진행상태 다이얼로그
	private SharedPreferences mPrefs; 								// 공유환경설정
	private Handler mHandler = new Handler();						// UI 업데이트를 위한 핸들러
	private ArrayList<CheckIn> checkins = new ArrayList<CheckIn>(); // 체크인정보를 담을 list
    private CheckInsArrayAdapater ciaAdapater;						// 커스텀 array adapter	
	// 엘리먼트
	private ListView listView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkin_layout);
		mLockScreenRotation();
		/*
		 * 공유 환경 설정에서 액세스 토큰 가져오기
		 */
		mPrefs = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);
		if (access_token != null) {
			facebook.setAccessToken(access_token); // 토큰 설정
		}
		if (expires != 0) {
			facebook.setAccessExpires(expires); // 토큰 만료 설정
		}
		
		// 엘리먼트 후킹
		listView = (ListView)findViewById(R.id.checkins);
		ImageButton placeBTN = (ImageButton)findViewById(R.id.place);
		placeBTN.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 모바일 형식으로 url을 구성한다.
				String url = "http://m.facebook.com/places/nearbyfriends.php?ref=bookmark&_user=" + mPrefs.getString("id","");

				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW); // 웹페이즈를 뛰울 인텐트 설정
				intent.setData(Uri.parse(url)); // url 설정
				startActivity(intent);
			}
		});
		
		// 데이터를 가져올동안 진행상태 다이얼로그를 표시
		progressDialog = new ProgressDialog(this);
		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage("로딩중...");		
		progressDialog.show();
        mAsyncRunner = new AsyncFacebookRunner(facebook);
		// graph API 요청 
		mAsyncRunner.request("me/checkins", new CheckInRequestListener());        
	}
	
    /**
     *	"me/checkins" 체크인 callback class
     */
    public class CheckInRequestListener implements
    com.facebook.android.AsyncFacebookRunner.RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			// TODO Auto-generated method stub
            try {
                // process the response here: executed in background thread
                Log.d("myfacebook", "response.length(): " + response.length());
                Log.d("myfacebook", "Response: checkin " + response);

                final JSONObject json = new JSONObject(response);
                JSONArray d = json.getJSONArray("data");
                final int l = (d != null ? d.length() : 0);
                Log.d("myfacebook", "d.length(): " + l);
                String place, id, name;	
                Bitmap picBitmap = null;			
                CheckIn checkin;
                JSONObject jsond;
                for (int i=0; i<l; i++) {
                    JSONObject object = d.getJSONObject(i);
                    checkin = new CheckIn();
                    place = object.getString("place");
                    Log.d("myfacebook", place);
                    jsond = new JSONObject(place);
                    name = jsond.getString("name");
                    Log.d("myfacebook",  name);
                    JSONObject objectd = new JSONObject(jsond.getString("location"));
                    String lat = objectd.getString("latitude");
                    Log.d("myfacebook",  lat);
                    String lon = objectd.getString("longitude");
                    Log.d("myfacebook",  lon);
            		//checkin.setId(id);
            		checkin.setName(name);  
            		checkin.setLat(lat); 
            		checkin.setLon(lon); 
            		Log.d("myfacebook", lon + "~~~~~~~~~~~~~~~~" + lat);
            		checkins.add(checkin);	// 추출된 정보는 리스트에 담는다.
                }
                
                // ui 변경은 쓰레드로 처리해야한다.
                CheckInActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                    	// 타이틀 바 변경
                        getWindow().setTitle("내 체크인 리스트(" + l + ")");
                    	ciaAdapater = new CheckInsArrayAdapater(checkins);
                        listView.setAdapter(ciaAdapater);
                        ciaAdapater.notifyDataSetChanged();
                    }
                });
            } catch (JSONException e) {
                Log.w("myfacebook", "json error");
            }finally{
            	progressDialog.dismiss();	// 다이얼로그를 닫는다.
            	unLockScreenRotation();		// 화면 잠금 해제
            }
		}

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			Log.d("myfacebook", "ioerror");
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			unLockScreenRotation();	
			Log.d("myfacebook", "filerror");
		}  

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			unLockScreenRotation();	
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			unLockScreenRotation();	
			Log.d("myfacebook", "filerro2r");
		}
    }

}
