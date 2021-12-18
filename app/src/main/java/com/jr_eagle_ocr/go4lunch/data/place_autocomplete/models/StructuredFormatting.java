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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StructuredFormatting that = (StructuredFormatting) o;

		if (mainTextMatchedSubstrings != null ? !mainTextMatchedSubstrings.equals(that.mainTextMatchedSubstrings) : that.mainTextMatchedSubstrings != null)
			return false;
		if (secondaryText != null ? !secondaryText.equals(that.secondaryText) : that.secondaryText != null)
			return false;
		return mainText != null ? mainText.equals(that.mainText) : that.mainText == null;
	}

	@Override
	public int hashCode() {
		int result = mainTextMatchedSubstrings != null ? mainTextMatchedSubstrings.hashCode() : 0;
		result = 31 * result + (secondaryText != null ? secondaryText.hashCode() : 0);
		result = 31 * result + (mainText != null ? mainText.hashCode() : 0);
		return result;
	}
}