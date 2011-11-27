package kr.co.sns;

import android.graphics.Bitmap;

/**
 * 친구정보를 담을 클래스
 */
public class Friend {
	private String id;
	private String name;
	private Bitmap picture;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Bitmap getPicture() {
		return picture;
	}

	public void setPicture(Bitmap picture) {
		this.picture = picture;
	}

}
