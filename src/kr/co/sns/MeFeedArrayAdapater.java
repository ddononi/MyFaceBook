package kr.co.sns;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MeFeedArrayAdapater extends BaseAdapter {
	private ArrayList<MeFeed> list;
	public MeFeedArrayAdapater(ArrayList<MeFeed> list ){
		this.list = list;
	}
	/** 전체 리스트 */
	public ArrayList<?> getList(){
		return list;
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

		TextView messageTV = (TextView)item.findViewById(R.id.message);
		TextView fromTV = (TextView)item.findViewById(R.id.from);
		TextView updatedTV = (TextView)item.findViewById(R.id.updated_time);
		ImageView imageView = (ImageView)item.findViewById(R.id.image);
		// 엘리먼트에 값을 set해준다,
		messageTV.setText(((MeFeed)getItem(position)).getMessage());	
		fromTV.setText(((MeFeed)getItem(position)).getFrom());
		String updatedTime = ((MeFeed)getItem(position)).getUpdatedTime();
		updatedTV.setText(updatedTime);
		// 이미지가 있으면 넣어주고 디스플레이를 해준다.
		Bitmap bitmap = ((MeFeed)getItem(position)).getPicture();
		if(bitmap != null){
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageBitmap(bitmap);
		}

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
		ViewGroup item = (ViewGroup)inflater.inflate(R.layout.mefeed_list, null);
		return item;
	}	

}