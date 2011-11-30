package kr.co.sns;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.facebook.android.DialogError;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendsArrayAdapater extends BaseAdapter {
	private ArrayList<?> list;
	private String id;	// 내 아이디값
	public FriendsArrayAdapater(ArrayList<?> list, String id){
		this.list = list;
		this.id = id;
	}

	
	/** 전체갯수 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/** list 의 각  view 설정 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewGroup item = getViewGroup(convertView, parent);
		// 엘리먼트 후킹
		TextView friendTV = (TextView)item.findViewById(R.id.friend);
		ImageView imageView = (ImageView)item.findViewById(R.id.image);
		ImageView writeView = (ImageView)item.findViewById(R.id.write);
		// 엘리먼트에 값을 set해준다,
		friendTV.setText(((Friend)getItem(position)).getName());	
		// 이미지가 있으면 넣어주고 디스플레이를 해준다.
		Bitmap bitmap  = ((Friend)getItem(position)).getPicture();
		imageView.setImageBitmap(bitmap);
	
		final int pos = position;
		final Context context = parent.getContext();
		// 친구 이름을 클릭하면 친구의 프로필 웹 페이지로 이동한다.
		friendTV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				StringBuilder link = new StringBuilder("http://m.facebook.com/profile.php");
				String id = ((Friend)getItem(pos)).getId();
				link.append("?id=");	
				link.append(id);	// id를 붙여준다.
				link.append("&_user=");	
				link.append(FriendsArrayAdapater.this.id);	// 내 아아디
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW); // 웹페이즈를 뛰울 인텐트 설정
				intent.setData(Uri.parse(link.toString())); // url 설정
				context.startActivity(intent);
			}
		});

		// 담벼락이미지 이벤트 설정
		writeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Bundle params = new Bundle();
				// alert에 넣어주기 위해 받은 사람 이름 저장
				// 파라미터로 친구의 아이를 넣어준다.
				params.putString("to", ((Friend)getItem(pos)).getId() );
				// 친구에게 담벼락 남기기 다이얼로그를띄운다.
				BaseActivity.facebook.dialog(context, "feed", params, new AppRequestsListener());					
			}
		});
		// 친구 앨범보기
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				StringBuilder link = new StringBuilder("http://m.facebook.com/media/albums/?id=");
				String id = ((Friend)getItem(pos)).getId();
				link.append(id);	// id를 붙여준다.
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW); // 웹페이즈를 뛰울 인텐트 설정
				intent.setData(Uri.parse(link.toString())); // url 설정
				context.startActivity(intent);
			}
		});
		return item;	
	}
	
	/**
	 * 뷰의 재사용 체크후 custom list로 뷰 반환
	 * @param reuse 변환될 뷰
	 * @param parent 부모뷰
	 * @return 전개후 얻어진 뷰
	 */
	private ViewGroup getViewGroup(View reuse, ViewGroup parent){
		/*
		if(reuse instanceof ViewGroup){	// 재사용이 가능하면 뷰를 재사용한다.
			return (ViewGroup)reuse;
		}
		*/
		Context context = parent.getContext();	// 부모뷰로부터 컨택스트를 얻어온다.
		LayoutInflater inflater = LayoutInflater.from(context);
		// custom list를 위해 인플레이터로 뷰를 가져온다
		ViewGroup item = (ViewGroup)inflater.inflate(R.layout.friend_list, null);
		return item;
	}
	
    public class AppRequestsListener implements
		com.facebook.android.Facebook.DialogListener {
	
		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			//Util.showAlert(context, "알림", "님에게 담벼락을 남겼습니다."  );
		}
	
		@Override
		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub
			//Toast.makeText(context, "Sdf", Toast.LENGTH_SHORT).show();
		}
	
		@Override
		public void onError(DialogError e) {
			// TODO Auto-generated method stub
		//	Toast.makeText(context, "Sdf", Toast.LENGTH_SHORT).show();
		}
	
		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
		//	Toast.makeText(context, "Sdf", Toast.LENGTH_SHORT).show();
		}

    }	 	
}