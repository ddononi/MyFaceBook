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

public class CheckInsArrayAdapater extends BaseAdapter {
	private ArrayList<?> list;
	private String name;
	public CheckInsArrayAdapater(ArrayList<?> list ){
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
		TextView nameTV = (TextView)item.findViewById(R.id.place);
		ImageView checkinView = (ImageView)item.findViewById(R.id.checkin);
		// ������Ʈ�� ���� set���ش�,
		nameTV.setText(((CheckIn)getItem(position)).getName());	

		final int pos = position;
		final Context context = parent.getContext();

		nameTV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		checkinView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckIn checkin = (CheckIn)getItem(pos);
				String geoURI = String.format("geo:%f,%f?z=%d", Float.valueOf(checkin.getLat()),
						Float.valueOf(checkin.getLon()), BaseActivity.ZOOM_DEEP);
				Uri geo = Uri.parse(geoURI);
				Intent geoMap = new Intent(Intent.ACTION_VIEW, geo);
				context.startActivity(geoMap);					
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
		ViewGroup item = (ViewGroup)inflater.inflate(R.layout.checkin_list, null);
		return item;
	}
 	
}