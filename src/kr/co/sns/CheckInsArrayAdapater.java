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
		TextView nameTV = (TextView)item.findViewById(R.id.place);
		ImageView checkinView = (ImageView)item.findViewById(R.id.checkin);
		// 엘리먼트에 값을 set해준다,
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
		ViewGroup item = (ViewGroup)inflater.inflate(R.layout.checkin_list, null);
		return item;
	}
 	
}