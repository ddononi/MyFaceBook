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
	private AsyncFacebookRunner mAsyncRunner; 	// 비동기 요청을 위한 객체
	private ProgressDialog progressDialog; 		// 진행상태 다이얼로그
	private SharedPreferences mPrefs; 			// 공유환경설정
	private Handler mHandler = new Handler();	// UI 업데이트를 위한 핸들러
	private ArrayList<MeFeed> meFeeds = new ArrayList<MeFeed>();// 내 담벼락들을 담을 list
    private MeFeedArrayAdapater meFeedArrayAdapater;			// 커스텀 array adapter
    
	// 엘리먼트
	private ImageButton profileIV; 	 // 프로필 이미지뷰
	private TextView nameTV;		 // 이름 텍스트뷰
	private ImageButton wallPostBtn; // wall post 버튼
    private ListView listView;		 // 내 담벼락 리스트
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
		mAsyncRunner = new AsyncFacebookRunner(facebook);

		// 엘리먼트 후킹
		profileIV = (ImageButton) findViewById(R.id.profile_image);
		nameTV = (TextView) findViewById(R.id.name);
		listView = (ListView) findViewById(R.id.list);
		wallPostBtn = (ImageButton) findViewById(R.id.write);
		
		// 글쓰기 버튼 처리
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
				// link주소가 없으면 그냥 넘긴다.
				if(TextUtils.isEmpty(((MeFeed)meFeeds.get(position)).getLink())){	
					return;
				}
				// 모바일 형식으로 url을 구성한다.
				StringBuilder link = new StringBuilder("http://m.facebook.com/story.php?story_fbid=");
				String id = ((MeFeed)meFeeds.get(position)).getId();
				String[] arr = TextUtils.split(id, "_");
				link.append(arr[1]);	// fbid를 붙여준다.
				link.append("&id=");	
				link.append(arr[0]);	// id를 붙여준다.
				link.append("&_user=");	
				link.append(arr[0]);	// id를 붙여준다.
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW); // 웹페이즈를 뛰울 인텐트 설정
				intent.setData(Uri.parse(link.toString())); // url 설정
				startActivity(intent);
		
			}
		});
		
		// 샘플로 사진 올리기....
		nameTV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(progressDialog.isShowing() != true){
					progressDialog.show();
				}
				
				// 꾸러미를 이용해 사진 url을 실어 보내주면 됩니다. photo_upload permission도 추가되야 합니다.
            	Bundle params = new Bundle();
            	// 여기서 이미지 주소를 넣어주시면 됩니다.
            	params.putString("url", "http://www.breaktheillusion.com/wp-content/uploads" +
            			"/2011/05/smiley-face-photo-co-theoutsourcingcompanycom.jpg");
                params.putString("caption", "사진 샘플올리기");
                mAsyncRunner.request("me/photos", params, "POST",
                        new PhotoUploadListener(), null);
			}
		});
		
		// 데이터를 가져올동안 진행상태 다이얼로그를 표시
		progressDialog = new ProgressDialog(this);
		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage("로딩중...");		
		progressDialog.show();
		// mSpinner.show();
		// 요청 및 퍼미션 설정 (내정보 요청)
		mAsyncRunner.request("me", new MyProfileRequestListener());
		mAsyncRunner.request("me/feed", new MyFeedListener());


	}

	/**
	 * 프로필 리스너 처리 클레스
	 */
	public class MyProfileRequestListener implements
			com.facebook.android.AsyncFacebookRunner.RequestListener {

		/**
		 * 정상적으로 값이 반환되면 response를 json 객체로 변환시켜 사용자 정보를 추출한다.
		 */
		@Override
		public void onComplete(String response, Object state) {
			// TODO Auto-generated method stub
			try {
				// process the response here: executed in background thread
				Log.d("myfacebook", "response.length(): " + response.length());
				Log.d("myfacebook", "Response: " + response);
				// 응답 메시지를 json 형태로 변환한다.
				final JSONObject json = new JSONObject(response);
				final String name = json.getString("name"); // 이름 추출
				String id = json.getString("id"); // 아이디 추출
				// 아이디값이 저장되지 않았으면 아이디 저장
				if( TextUtils.isEmpty(mPrefs.getString("id", ""))  ){
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("id", id);	// 아이디값 저장
					editor.commit();
				}

				
				final String link = "http://m.facebook.com/profile.php"  ;//json.getString("link"); // 유저 프로필 URL
				Log.d("myfacebook", "name------>" + name);

				// 아이디값으로 유저 이미지 추출 url
				final String imageUrl = "http://graph.facebook.com/" + id
						+ "/picture";
				// ui 업데이트 처리
				MyProfile.this.runOnUiThread(new Runnable() {
					public void run() {
						URL url = null;
						try {
							url = new URL(imageUrl); // url 로 변환
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							// url주소의 이미지를 비트맵으로 변환후 imageView에 설정
							profileIV.setImageBitmap(BitmapFactory
									.decodeStream(url.openStream()));
							// 유저 프로필 링크 설정
							profileIV.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									Intent intent = new Intent();
									intent.setAction(Intent.ACTION_VIEW); 
									intent.setData(Uri.parse(link)); // url 설정
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
				Log.w("myfacebook", "JSON 에러");
			}

			
		}

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			Util.showAlert(MyProfile.this, "알림",  "입출력 에러!");	
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "알림",  "파일을 찾을수 없습니다.");				
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "알림",  "에러!");				
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "알림",  "에러!");				
		}
	}
	

	public class MyFeedListener implements
			com.facebook.android.AsyncFacebookRunner.RequestListener {

		/**
		 * 정상적으로 값이 반환되면 response를 json 객체로 변환시켜 사용자 정보를 추출한다.
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
                // 아이디, 남긴이, 메세지 or 스토리, 링크주소, 업데이트 날짜
                String id, from, message, link, updatedTime;
                Bitmap picture;	//	이미지 비트맵
                for (int i=0; i<l; i++) {	// data 갯수만큼
                    MeFeed meFeed = new MeFeed();
                    JSONObject object = d.getJSONObject(i);
                    id = object.getString("id");
                    // from 에서 jsonarary를 뽑아온다.
                    JSONObject fromObject = object.getJSONObject("from");
                    from = fromObject.getString("name"); 
                    link = "";
                    if(!object.isNull("actions")){	//link 뽑아오기
                    	JSONArray actionArray = object.getJSONArray("actions");
                    	JSONObject actionObject = actionArray.getJSONObject(0);
                    	link = actionObject.getString("link"); 
                    }
                    from = fromObject.getString("name");                     
                    message = "";
                    if(!object.isNull("message")){	// message혹은 story가 없으면 그냥 넘긴다.
                    	message = object.getString("message"); 
                    }else if(!object.isNull("story")){
                    	message = object.getString("story"); 
                    }else{
                    	continue;
                    }
                    
                    picture = null;
                    if(!object.isNull("picture")){	// 이미지가 있으면 추가
                    	String urlStr = object.getString("picture"); 
            			URL url = null;
            			try {
            				url = new URL(urlStr);
            				// 비트맵으로 변환후 이미를 넣어준다.
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
                    	// 타이틀바 변경
                    	getWindow().setTitle("내 담벼락목록(" + l + "개)");
                        meFeedArrayAdapater = new MeFeedArrayAdapater(meFeeds);
                        listView.setAdapter(meFeedArrayAdapater);
                        meFeedArrayAdapater.notifyDataSetChanged();
                    }
                });
            } catch (JSONException e) {
                Log.w("myfacebook", "JSON Error in response");
            }finally{
				progressDialog.dismiss();	// 로딩 다이얼로그를 닫아준다.
				unLockScreenRotation();		// 화면 잠금 해제
            }
            

			
		}

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			Util.showAlert(MyProfile.this, "알림",  "입출력 에러!");	
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "알림",  "파일을 찾을수 없습니다.");				
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "알림",  "에러!");				
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
            Util.showAlert(MyProfile.this, "알림",  "에러!");				
		}
	}	

	/**
	 * 담벼락 다이얼로그
	 */
	public class WallPostDialogListener implements
			com.facebook.android.Facebook.DialogListener {

		/**
		 * Called when the dialog has completed successfully
		 */
		public void onComplete(Bundle values) {
			final String postId = values.getString("post_id");
			if (postId != null) {
				Log.d("myfacebook", "성공 포스트 아이디=" + postId);
				Util.showAlert(MyProfile.this, "알림",  "담벼락을 남겼습니다.");
				mAsyncRunner.request(postId, new WallPostRequestListener());
			} else {
				Log.d("myfacebook", "실패");
				//Util.showAlert(MyProfile.this, "알림",  "취소");
			}
		}

		@Override
		public void onCancel() {
			// No special processing if dialog has been canceled
	            Util.showAlert(MyProfile.this, "알림",  "로딩취소");
		}

		@Override
		public void onError(DialogError e) {
			// No special processing if dialog has been canceled
            Util.showAlert(MyProfile.this, "알림",  "에러");			
		}

		@Override
		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub
            Util.showAlert(MyProfile.this, "알림",  "에러!");				

		}

	}

	/**
	 *	담벼락 다이얼로그에 들어갈 요청 리스너
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
	 *	사진 업로드 다이얼로그에 들어갈 요청 리스너
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
                	Util.showAlert(MyProfile.this, "성공",  "사진을 업로드했습니다.");	
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
