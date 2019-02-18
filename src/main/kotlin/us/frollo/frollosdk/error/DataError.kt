package us.frollo.frollosdk.error

import com.google.gson.annotations.SerializedName

/**
 * Error caused by an issue with data or data storage
 */
data class DataError(
        /** Data error type */
        @SerializedName("type") val type: DataErrorType,
        /** More detailed sub type of the error */
        @SerializedName("sub_type") val subType: DataErrorSubType
) : FrolloSDKError() {

    /** Localized description */
    override val localizedDescription : String?
        get() = if (subType.type == type) subType.toLocalizedString(context)
                else type.toLocalizedString(context)

    /** Debug description */
    override val debugDescription: String?
        get() = "DataError: ${ type.name }.${ subType.name }: $localizedDescription"
}