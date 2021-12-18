package com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author jrigault
 */
public class PredictionsItem {

    @SerializedName("reference")
    private String reference;

    @SerializedName("types")
    private List<String> types;

    @SerializedName("matched_substrings")
    private List<MatchedSubstringsItem> matchedSubstrings;

    @SerializedName("terms")
    private List<TermsItem> terms;

    @SerializedName("structured_formatting")
    private StructuredFormatting structuredFormatting;

    @SerializedName("description")
    private String description;

    @SerializedName("place_id")
    private String placeId;

    public String getReference() {
        return reference;
    }

    public List<String> getTypes() {
        return types;
    }

    public List<MatchedSubstringsItem> getMatchedSubstrings() {
        return matchedSubstrings;
    }

    public List<TermsItem> getTerms() {
        return terms;
    }

    public StructuredFormatting getStructuredFormatting() {
        return structuredFormatting;
    }

    public String getDescription() {
        return description;
    }

    public String getPlaceId() {
        return placeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PredictionsItem item = (PredictionsItem) o;

        if (reference != null ? !reference.equals(item.reference) : item.reference != null)
            return false;
        if (types != null ? !types.equals(item.types) : item.types != null) return false;
        if (matchedSubstrings != null ? !matchedSubstrings.equals(item.matchedSubstrings) : item.matchedSubstrings != null)
            return false;
        if (terms != null ? !terms.equals(item.terms) : item.terms != null) return false;
        if (structuredFormatting != null ? !structuredFormatting.equals(item.structuredFormatting) : item.structuredFormatting != null)
            return false;
        if (description != null ? !description.equals(item.description) : item.description != null)
            return false;
        return placeId != null ? placeId.equals(item.placeId) : item.placeId == null;
    }

    @Override
    public int hashCode() {
        int result = reference != null ? reference.hashCode() : 0;
        result = 31 * result + (types != null ? types.hashCode() : 0);
        result = 31 * result + (matchedSubstrings != null ? matchedSubstrings.hashCode() : 0);
        result = 31 * result + (terms != null ? terms.hashCode() : 0);
        result = 31 * result + (structuredFormatting != null ? structuredFormatting.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (placeId != null ? placeId.hashCode() : 0);
        return result;
    }
}