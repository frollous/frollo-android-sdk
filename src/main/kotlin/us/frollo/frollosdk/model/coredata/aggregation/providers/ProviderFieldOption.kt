package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName

/** Details for display of an option if the field contains a list of options */
data class ProviderFieldOption(

        /** Text to be displayed to the user */
        @SerializedName("displayText") val displayText: String,

        /** Selected indicator. Updated when a user selects an option (optional) */
        @SerializedName("optionValue") val optionValue: String,

        /** Value of the option */
        @SerializedName("isSelected") var isSelected: Boolean?
) {

    /** String representation of the object. Useful if the field is a Spinner. */
    override fun toString(): String = displayText
}