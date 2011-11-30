package kr.co.sns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

public class MyProfile extends BaseActivity {
	private AsyncFacebookRunner mAsyncRunner; 	// �񵿱� ��û�� ���� ��ü
	private ProgressDialog progressDialog; 		// ������� ���̾�α�
	private SharedPreferences mPrefs; 			// ����ȯ�漳��
	private Handler mHandler = new Handler();	// UI ������Ʈ�� ���� �ڵ鷯
	private ArrayList<MeFeed> meFeeds = new ArrayList<MeFeed>();// �� �㺭������ ���� list
    private MeFeedArrayAdapater meFeedArrayAdapater;			// Ŀ���� array adapter
    
	// ������Ʈ
	private ImageButton profileIV; 	 // ������ �̹�����
	private TextView nameTV;		 // �̸� �ؽ�Ʈ��
	private ImageButton wallPostBtn; // wall post ��ư
    private ListView listView;		 // �� �㺭�� ����Ʈ
	/** Called when the activity is first created. */
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myprofile_layout);
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
		mAsyncRunner = new AsyncFacebookRunner(facebook);

		// ������Ʈ ��ŷ
		profileIV = (ImageButton) findViewById(R.id.profile_image);
		nameTV = (TextView) findViewById(R.id.name);
		listView = (ListView) findViewById(R.id.list);
		wallPostBtn = (ImageButton) findViewById(R.id.write);
		
		// �۾��� ��ư ó��
		wallPostBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				facebook.dialog(MyProfile.this, "stream.publish",
						new WallPostDialogListener());
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				// link�ּҰ� ������ �׳� �ѱ��.
				if(TextUtils.isEmpty(((MeFeed)meFeeds.get(position)).getLink())){	
					return;
				}
				// ����� �������� url�� �����Ѵ�.
				StringBuilder link = new StringBuilder("http://m.facebook.com/story.php?story_fbid=");
				String id = ((MeFeed)meFeeds.get(position)).getId();
				String[] arr = TextUtils.split(id, "_");
				link.append(arr[1]);	// fbid�� �ٿ��ش�.
				link.append("&id=");	
				link.append(arr[0]);	// id�� �ٿ��ش�.
				link.append("&_user=");	
				link.append(arr[0]);	// id�� �ٿ��ش�.
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW); // ������� �ٿ� ����Ʈ ����
				intent.setData(Uri.parse(link.toString())); // url ����
				startActivity(intent);
		
			}
		});
		
		// ���÷� ���� �ø���....
		nameTV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(progressDialog.isShowing() != true){
					progressDialog.show();
				}
				
				// �ٷ��̸� �̿��� ���� url�� �Ǿ� �����ָ� �˴ϴ�. photo_upload permission�� �߰��Ǿ� �մϴ�.
            	Bundle params = new Bundle();
            	// ���⼭ �̹��� �ּҸ� �־��ֽø� �˴ϴ�.
            	params.putString("url", "http://www.breaktheillusion.com/wp-content/uploads" +
            			"/2011/05/smiley-face-photo-co-theoutsourcingcompanycom.jpg");
                params.putString("caption", "���� ���ÿø���");
                mAsyncRunner.request("me/photos", params, "POST",
                        new PhotoUploadListener(), null);
			}
		});
		
		// �����͸� �����õ��� ������� ���̾�α׸� ǥ��
		progressDialog = new ProgressDialog(this);
		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage("�ε���...");		
		progressDialog.show();
		// mSpinner.show();
		// ��û �� �۹̼� ���� (������ ��û)
		mAsyncRunner.request("me", new MyProfileRequestListener());
		mAsyncRunner.request("me/feed", new MyFeedListener());


	}

	/**
	 * ������ ������ ó�� Ŭ����
	 */
	public class MyProfileRequestListener implements
			com.facebook.android.AsyncFacebookRunner.RequestListener {

		/**
		 * ���������� ���� ��ȯ�Ǹ� response�� json ��ü�� ��ȯ���� ����� ������ �����Ѵ�.
		 */
		@Override
		public void onComplete(String response, Object state) {
			// TODO Auto-generated method stub
			try {
				// process the response here: executed in background thread
				Log.d("myfacebook", "response.length(): " + response.length());
				Log.d("myfacebook", "Response: " + response);
				// ���� �޽����� json ���·� ��ȯ�Ѵ�.
				final JSONObject json = new JSONObject(response);
				final String name = json.getString("name"); // �̸� ����
				String id = json.getString("id"); // ���̵� ����
				// ���̵��� ������� �ʾ����� ���̵� ����
				if( TextUtils.isEmpty(mPrefs.getString("id", ""))  ){
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("id", id);	// ���̵� ����
					editor.commit();
				}

				
				final String link = "http://m.facebook.com/profile.php"  ;//json.getString("link"); // ���� ������ URL
				Log.d("myfacebook", "name------>" + name);

				// ���̵����� ���� �̹��� ���� url
				final String imageUrl = "http://graph.facebook.com/" + id
						+ "/picture";
				// ui ������Ʈ ó��
				MyProfile.this.runOnUiThread(new Runnable() {
					public void run() {
						URL url = null;
						try {
							url = new URL(imageUrl); // url �� ��ȯ
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							// url�ּ��� �̹����� ��Ʈ������ ��ȯ�� imageView�� ����
							profileIV.setImageBitmap(BitmapFactory
									.decodeStream(url.openStream()));
							// ���� ������ ��ũ ����
							profileIV.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									Intent intent = new Intent();
									intent.setAction(Intent.ACTION_VIEW); 
									intent.setData(Uri.parse(link)); // url ����
									startActivity(intent);
								}
							});
							nameTV.setText(name);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			} catch (JSONException e) {
				Log.w("myfacebook", "JSON ����");
			}

			
		}

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			Util.showAlert(MyProfile.this, "�˸�",  "����� ����!");	
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "�˸�",  "������ ã���� �����ϴ�.");				
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "�˸�",  "����!");				
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "�˸�",  "����!");				
		}
	}
	

	public class MyFeedListener implements
			com.facebook.android.AsyncFacebookRunner.RequestListener {

		/**
		 * ���������� ���� ��ȯ�Ǹ� response�� json ��ü�� ��ȯ���� ����� ������ �����Ѵ�.
		 */
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
                // ���̵�, ������, �޼��� or ���丮, ��ũ�ּ�, ������Ʈ ��¥
                String id, from, message, link, updatedTime;
                Bitmap picture;	//	�̹��� ��Ʈ��
                for (int i=0; i<l; i++) {	// data ������ŭ
                    MeFeed meFeed = new MeFeed();
                    JSONObject object = d.getJSONObject(i);
                    id = object.getString("id");
                    // from ���� jsonarary�� �̾ƿ´�.
                    JSONObject fromObject = object.getJSONObject("from");
                    from = fromObject.getString("name"); 
                    link = "";
                    if(!object.isNull("actions")){	//link �̾ƿ���
                    	JSONArray actionArray = object.getJSONArray("actions");
                    	JSONObject actionObject = actionArray.getJSONObject(0);
                    	link = actionObject.getString("link"); 
                    }
                    from = fromObject.getString("name");                     
                    message = "";
                    if(!object.isNull("message")){	// messageȤ�� story�� ������ �׳� �ѱ��.
                    	message = object.getString("message"); 
                    }else if(!object.isNull("story")){
                    	message = object.getString("story"); 
                    }else{
                    	continue;
                    }
                    
                    picture = null;
                    if(!object.isNull("picture")){	// �̹����� ������ �߰�
                    	String urlStr = object.getString("picture"); 
            			URL url = null;
            			try {
            				url = new URL(urlStr);
            				// ��Ʈ������ ��ȯ�� �̸̹� �־��ش�.
            				picture = (BitmapFactory.decodeStream(url.openStream()));	
            			} catch (MalformedURLException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			} catch (IOException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			}
                 	
                    }                    
                    updatedTime = object.getString("updated_time").substring(0, 10);
                    meFeed.setId(id);
                    meFeed.setFrom(from);
                    meFeed.setLink(link);
                    meFeed.setMessage(message);
                    meFeed.setPicture(picture);
                    meFeed.setUpdatedTime(updatedTime);
                    meFeeds.add(meFeed);
                    Log.d("myfacebook", id);
                }

                MyProfile.this.runOnUiThread(new Runnable() {
                    public void run() {
                    	// Ÿ��Ʋ�� ����
                    	getWindow().setTitle("�� �㺭�����(" + l + "��)");
                        meFeedArrayAdapater = new MeFeedArrayAdapater(meFeeds);
                        listView.setAdapter(meFeedArrayAdapater);
                        meFeedArrayAdapater.notifyDataSetChanged();
                    }
                });
            } catch (JSONException e) {
                Log.w("myfacebook", "JSON Error in response");
            }finally{
				progressDialog.dismiss();	// �ε� ���̾�α׸� �ݾ��ش�.
				unLockScreenRotation();		// ȭ�� ��� ����
            }
            

			
		}

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			Util.showAlert(MyProfile.this, "�˸�",  "����� ����!");	
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "�˸�",  "������ ã���� �����ϴ�.");				
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "�˸�",  "����!");				
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "�˸�",  "����!");				
		}
	}	

	/**
	 * �㺭�� ���̾�α�
	 */
	public class WallPostDialogListener implements
			com.facebook.android.Facebook.DialogListener {

		/**
		 * Called when the dialog has completed successfully
		 */
		public void onComplete(Bundle values) {
			final String postId = values.getString("post_id");
			if (postId != null) {
				Log.d("myfacebook", "���� ����Ʈ ���̵�=" + postId);
				Util.showAlert(MyProfile.this, "�˸�",  "�㺭���� ������ϴ�.");
				mAsyncRunner.request(postId, new WallPostRequestListener());
			} else {
				Log.d("myfacebook", "����");
				//Util.showAlert(MyProfile.this, "�˸�",  "���");
			}
		}

		@Override
		public void onCancel() {
			// No special processing if dialog has been canceled
	            Util.showAlert(MyProfile.this, "�˸�",  "�ε����");
		}

		@Override
		public void onError(DialogError e) {
			// No special processing if dialog has been canceled
            Util.showAlert(MyProfile.this, "�˸�",  "����");			
		}

		@Override
		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub
            Util.showAlert(MyProfile.this, "�˸�",  "����!");				

		}

	}

	/**
	 *	�㺭�� ���̾�α׿� �� ��û ������
	 */
	public class WallPostRequestListener implements
			com.facebook.android.AsyncFacebookRunner.RequestListener {

		/**
		 * Called when the wall post request has completed
		 */

		@Override
		public void onComplete(String response, Object state) {
			// TODO Auto-generated method stub
			Log.d("myfacebook", "response-->" + response);
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
	
	/**
	 *	���� ���ε� ���̾�α׿� �� ��û ������
	 */
	public class PhotoUploadListener implements
			com.facebook.android.AsyncFacebookRunner.RequestListener {

		/**
		 * Called when the wall post request has completed
		 */

		@Override
		public void onComplete(String response, Object state) {
			// TODO Auto-generated method stub
			Log.d("myfacebook", "response-->" + response);
			progressDialog.dismiss();
			MyProfile.this.runOnUiThread(new Runnable() {
                public void run() {
                	Util.showAlert(MyProfile.this, "����",  "������ ���ε��߽��ϴ�.");	
                }
            });			
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
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
		}
	}	
	
	
}
