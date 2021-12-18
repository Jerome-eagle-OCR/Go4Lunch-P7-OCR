package com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models;

import com.google.gson.annotations.SerializedName;

public class MatchedSubstringsItem{

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MatchedSubstringsItem that = (MatchedSubstringsItem) o;

		if (offset != that.offset) return false;
		return length == that.length;
	}

	@Override
	public int hashCode() {
		int result = offset;
		result = 31 * result + length;
		return result;
	}
}