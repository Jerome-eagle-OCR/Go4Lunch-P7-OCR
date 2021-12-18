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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PlaceAutocompleteApiResponse that = (PlaceAutocompleteApiResponse) o;

		if (predictions != null ? !predictions.equals(that.predictions) : that.predictions != null)
			return false;
		return status != null ? status.equals(that.status) : that.status == null;
	}

	@Override
	public int hashCode() {
		int result = predictions != null ? predictions.hashCode() : 0;
		result = 31 * result + (status != null ? status.hashCode() : 0);
		return result;
	}
}