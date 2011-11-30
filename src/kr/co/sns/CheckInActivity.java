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
	private AsyncFacebookRunner mAsyncRunner; 						// �񵿱� ��û ó���� ���� ��ü
	private ProgressDialog progressDialog; 							// ������� ���̾�α�
	private SharedPreferences mPrefs; 								// ����ȯ�漳��
	private Handler mHandler = new Handler();						// UI ������Ʈ�� ���� �ڵ鷯
	private ArrayList<CheckIn> checkins = new ArrayList<CheckIn>(); // üũ�������� ���� list
    private CheckInsArrayAdapater ciaAdapater;						// Ŀ���� array adapter	
	// ������Ʈ
	private ListView listView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkin_layout);
		mLockScreenRotation();
		/*
		 * ���� ȯ�� �������� �׼��� ��ū ��������
		 */
		mPrefs = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);
		if (access_token != null) {
			facebook.setAccessToken(access_token); // ��ū ����
		}
		if (expires != 0) {
			facebook.setAccessExpires(expires); // ��ū ���� ����
		}
		
		// ������Ʈ ��ŷ
		listView = (ListView)findViewById(R.id.checkins);
		ImageButton placeBTN = (ImageButton)findViewById(R.id.place);
		placeBTN.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// ����� �������� url�� �����Ѵ�.
				String url = "http://m.facebook.com/places/nearbyfriends.php?ref=bookmark&_user=" + mPrefs.getString("id","");

				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW); // ������� �ٿ� ����Ʈ ����
				intent.setData(Uri.parse(url)); // url ����
				startActivity(intent);
			}
		});
		
		// �����͸� �����õ��� ������� ���̾�α׸� ǥ��
		progressDialog = new ProgressDialog(this);
		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage("�ε���...");		
		progressDialog.show();
        mAsyncRunner = new AsyncFacebookRunner(facebook);
		// graph API ��û 
		mAsyncRunner.request("me/checkins", new CheckInRequestListener());        
	}
	
    /**
     *	"me/checkins" üũ�� callback class
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
            		checkins.add(checkin);	// ����� ������ ����Ʈ�� ��´�.
                }
                
                // ui ������ ������� ó���ؾ��Ѵ�.
                CheckInActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                    	// Ÿ��Ʋ �� ����
                        getWindow().setTitle("�� üũ�� ����Ʈ(" + l + ")");
                    	ciaAdapater = new CheckInsArrayAdapater(checkins);
                        listView.setAdapter(ciaAdapater);
                        ciaAdapater.notifyDataSetChanged();
                    }
                });
            } catch (JSONException e) {
                Log.w("myfacebook", "json error");
            }finally{
            	progressDialog.dismiss();	// ���̾�α׸� �ݴ´�.
            	unLockScreenRotation();		// ȭ�� ��� ����
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
