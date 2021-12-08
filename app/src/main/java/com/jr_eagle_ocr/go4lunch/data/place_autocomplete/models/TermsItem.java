package com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models;

import com.google.gson.annotations.SerializedName;

public class TermsItem{

	@SerializedName("offset")
	private int offset;

	@SerializedName("value")
	private String value;

	public int getOffset(){
		return offset;
	}

	public String getValue(){
		return value;
	}
}