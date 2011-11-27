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
	private String name;
	public FriendsArrayAdapater(ArrayList<?> list ){
		this.list = list;
	}

	
	/** ��ü���� */
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

	/** list �� ��  view ���� */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewGroup item = getViewGroup(convertView, parent);
		// ������Ʈ ��ŷ
		TextView friendTV = (TextView)item.findViewById(R.id.friend);
		ImageView imageView = (ImageView)item.findViewById(R.id.image);
		ImageView writeView = (ImageView)item.findViewById(R.id.write);
		// ������Ʈ�� ���� set���ش�,
		friendTV.setText(((Friend)getItem(position)).getName());	
		// �̹����� ������ �־��ְ� ���÷��̸� ���ش�.
		Bitmap bitmap  = ((Friend)getItem(position)).getPicture();
		imageView.setImageBitmap(bitmap);
	
		final int pos = position;
		final Context context = parent.getContext();
		// ģ�� �̸��� Ŭ���ϸ� ģ���� ������ �� �������� �̵��Ѵ�.
		friendTV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				StringBuilder link = new StringBuilder("http://m.facebook.com/profile.php");
				String id = ((Friend)getItem(pos)).getId();
				link.append("?id=");	
				link.append(id);	// id�� �ٿ��ش�.
				link.append("&_user=");	
				link.append(BaseActivity.APP_ID);	// �� �� ���̵�
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW); // ������� �ٿ� ����Ʈ ����
				intent.setData(Uri.parse(link.toString())); // url ����
				context.startActivity(intent);
			}
		});

		// �㺭���̹��� �̺�Ʈ ����
		writeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Bundle params = new Bundle();
				// alert�� �־��ֱ� ���� ���� ��� �̸� ����
				// �Ķ���ͷ� ģ���� ���̸� �־��ش�.
				params.putString("to", ((Friend)getItem(pos)).getId() );
				// ģ������ �㺭�� ����� ���̾�α׸�����.
				BaseActivity.facebook.dialog(context, "feed", params, new AppRequestsListener());					
			}
		});
		// ģ�� �ٹ�����
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				StringBuilder link = new StringBuilder("http://m.facebook.com/media/albums/?id=");
				String id = ((Friend)getItem(pos)).getId();
				link.append(id);	// id�� �ٿ��ش�.
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW); // ������� �ٿ� ����Ʈ ����
				intent.setData(Uri.parse(link.toString())); // url ����
				context.startActivity(intent);
			}
		});
		return item;	
	}
	
	/**
	 * ���� ���� üũ�� custom list�� �� ��ȯ
	 * @param reuse ��ȯ�� ��
	 * @param parent �θ��
	 * @return ������ ����� ��
	 */
	private ViewGroup getViewGroup(View reuse, ViewGroup parent){
		/*
		if(reuse instanceof ViewGroup){	// ������ �����ϸ� �並 �����Ѵ�.
			return (ViewGroup)reuse;
		}
		*/
		Context context = parent.getContext();	// �θ��κ��� ���ý�Ʈ�� ���´�.
		LayoutInflater inflater = LayoutInflater.from(context);
		// custom list�� ���� ���÷����ͷ� �並 �����´�
		ViewGroup item = (ViewGroup)inflater.inflate(R.layout.friend_list, null);
		return item;
	}
	
    public class AppRequestsListener implements
		com.facebook.android.Facebook.DialogListener {
	
		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			//Util.showAlert(context, "�˸�", "�Կ��� �㺭���� ������ϴ�."  );
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