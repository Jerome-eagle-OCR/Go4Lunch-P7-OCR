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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TermsItem termsItem = (TermsItem) o;

		if (offset != termsItem.offset) return false;
		return value != null ? value.equals(termsItem.value) : termsItem.value == null;
	}

	@Override
	public int hashCode() {
		int result = offset;
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}
}