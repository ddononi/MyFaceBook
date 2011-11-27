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

public class MyFriends extends BaseActivity{
	private AsyncFacebookRunner mAsyncRunner; // �񵿱� ��û ó���� ���� ��ü
	private ProgressDialog progressDialog; 		// ������� ���̾�α�
	private SharedPreferences mPrefs; 			// ����ȯ�漳��
	private Handler mHandler = new Handler();	// UI ������Ʈ�� ���� �ڵ鷯
	private ArrayList<Friend> friends = new ArrayList<Friend>();// �� �㺭������ ���� list
    private FriendsArrayAdapater friendsArrayAdapater;			// Ŀ���� array adapter	
	
	// ������Ʈ
	private ListView listView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_layout);
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
		listView = (ListView)findViewById(R.id.friends);

		
		// �����͸� �����õ��� ������� ���̾�α׸� ǥ��
		progressDialog = new ProgressDialog(this);
		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage("�ε���...");		
		progressDialog.show();
        mAsyncRunner = new AsyncFacebookRunner(facebook);
		// graph API ��û 
		mAsyncRunner.request("me/friends", new FriendsRequestListener());        
	}
	
    /**
     *	me/friends ģ����û �ݹ� Ŭ����
     */
    public class FriendsRequestListener implements
    com.facebook.android.AsyncFacebookRunner.RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			// TODO Auto-generated method stub
            try {
                // process the response here: executed in background thread
                Log.d("myfacebook", "response.length(): " + response.length());
                Log.d("myfacebook", "Response: " + response);

                final JSONObject json = new JSONObject(response);
                JSONArray d = json.getJSONArray("data");
                final int l = (d != null ? d.length() : 0);
                Log.d("myfacebook", "d.length(): " + l);
                String name, id, picture;	// �̸�, ���̵�, ������
                Bitmap picBitmap = null;			// ���ڵ��� ��Ʈ��
                Friend friend;
                for (int i=0; i<l; i++) {
                    JSONObject object = d.getJSONObject(i);
                    friend = new Friend();
                    name = object.getString("name");
                    id = object.getString("id");
                    // ���̵� �̿��Ͽ� ģ���� ������ �̹��� url�� �����Ѵ�.
                    picture = "http://graph.facebook.com/" + id + "/picture";
                    picBitmap = null;
            		if( !TextUtils.isEmpty(picture) ){	
            			URL url = null;
            			try {
            				url = new URL(picture);
            				// ��Ʈ������ ��ȯ�� �̸̹� �־��ش�.
            				picBitmap = BitmapFactory.decodeStream(url.openStream());	
            			} catch (MalformedURLException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			} catch (IOException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			}
            		}
            		
                    friend.setId(id);
                    friend.setName(name);                    
                    friend.setPicture(picBitmap);
                    friends.add(friend);	// ����� ������ ����Ʈ�� ��´�.
                }
                
                // ui ������ ������� ó���ؾ��Ѵ�.
                MyFriends.this.runOnUiThread(new Runnable() {
                    public void run() {
                    	// Ÿ��Ʋ �� ����
                        getWindow().setTitle("ģ�����(" + l + "��)");
                    	friendsArrayAdapater = new FriendsArrayAdapater(friends);
                        listView.setAdapter(friendsArrayAdapater);
                        friendsArrayAdapater.notifyDataSetChanged();
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
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			unLockScreenRotation();		
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
		}
    }

}
