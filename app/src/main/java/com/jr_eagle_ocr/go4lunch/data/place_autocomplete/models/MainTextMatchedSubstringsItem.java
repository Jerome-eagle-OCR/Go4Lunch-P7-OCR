package com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models;

import com.google.gson.annotations.SerializedName;

public class MainTextMatchedSubstringsItem{

	@SerializedName("offset")
	private int offset;

	@SerializedName("length")
	private int length;

	public int getOffset(){
		return offset;
	}

	public int getLength(){
		return length;
	}
}