package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName

/** Represents a regular expression and associated error to be performed on a field. */
data class ProviderFieldValidation(

        /** Regular expression to be evaluated on the field value */
        @SerializedName("regExp") val regExp: String,

        /** Error message to be displayed if the regex doesn't match */
        @SerializedName("errorMsg") val errorMsg: String
)