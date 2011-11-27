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
	/** ��ü ����Ʈ */
	public ArrayList<?> getList(){
		return list;
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

		TextView messageTV = (TextView)item.findViewById(R.id.message);
		TextView fromTV = (TextView)item.findViewById(R.id.from);
		TextView updatedTV = (TextView)item.findViewById(R.id.updated_time);
		ImageView imageView = (ImageView)item.findViewById(R.id.image);
		// ������Ʈ�� ���� set���ش�,
		messageTV.setText(((MeFeed)getItem(position)).getMessage());	
		fromTV.setText(((MeFeed)getItem(position)).getFrom());
		String updatedTime = ((MeFeed)getItem(position)).getUpdatedTime();
		updatedTV.setText(updatedTime);
		// �̹����� ������ �־��ְ� ���÷��̸� ���ش�.
		Bitmap bitmap = ((MeFeed)getItem(position)).getPicture();
		if(bitmap != null){
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageBitmap(bitmap);
		}

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
		ViewGroup item = (ViewGroup)inflater.inflate(R.layout.mefeed_list, null);
		return item;
	}	

}