package com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class StructuredFormatting{

	@SerializedName("main_text_matched_substrings")
	private List<MainTextMatchedSubstringsItem> mainTextMatchedSubstrings;

	@SerializedName("secondary_text")
	private String secondaryText;

	@SerializedName("main_text")
	private String mainText;

	public List<MainTextMatchedSubstringsItem> getMainTextMatchedSubstrings(){
		return mainTextMatchedSubstrings;
	}

	public String getSecondaryText(){
		return secondaryText;
	}

	public String getMainText(){
		return mainText;
	}
}