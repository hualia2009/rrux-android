package com.ucredit.dream.contact;

import android.graphics.Bitmap;

public class ContactModel
{
	private String id;
	private String name;
	private String info;
	private Bitmap photo;
	private String number;
	
	private String sortLetters;
	
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getSortLetters()
	{
		return sortLetters;
	}
	public void setSortLetters(String sortLetters)
	{
		this.sortLetters = sortLetters;
	}
	public String getInfo()
	{
		return info;
	}
	public void setInfo(String info)
	{
		this.info = info;
	}
    public Bitmap getPhoto() {
        return photo;
    }
    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
	
}
