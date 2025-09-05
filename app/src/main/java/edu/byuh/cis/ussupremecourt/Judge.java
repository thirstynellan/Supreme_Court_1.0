package edu.byuh.cis.ussupremecourt;

import android.graphics.Bitmap;

public class Judge {
	
	private String name;
	private String birth;
	private String death;
	private Bitmap photo ;
	private String bio;
	private int id;
	int resID;

	public Judge() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Judge) {
			Judge o = (Judge)other;
			return (this.name.equals(o.name) && this.birth.equals(o.birth));
		}
		return false;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBirth() {
		return birth;
	}

	public void setBirth(String birth) {
		this.birth = birth;
	}

	public String getDeath() {
		return death;
	}

	public void setDeath(String death) {
		this.death = death;
	}

	public Bitmap getPhoto() {
		return photo;
	}

	public void setPhoto(Bitmap photo) {
		this.photo = photo;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public int getId() {
		return id;
	}

	public void setId(String id) {
		this.id = Integer.parseInt(id);
	}


}
