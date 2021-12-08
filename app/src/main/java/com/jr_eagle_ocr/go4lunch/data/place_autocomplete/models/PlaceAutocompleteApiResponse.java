package com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class PlaceAutocompleteApiResponse {

	@SerializedName("predictions")
	private List<PredictionsItem> predictions;

	@SerializedName("status")
	private String status;

	public List<PredictionsItem> getPredictions(){
		return predictions;
	}

	public String getStatus(){
		return status;
	}
}