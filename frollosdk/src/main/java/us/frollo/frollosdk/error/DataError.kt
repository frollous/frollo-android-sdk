package us.frollo.frollosdk.error

import com.google.gson.annotations.SerializedName

data class DataError(
        @SerializedName("type") val type: DataErrorType,
        @SerializedName("sub_type") val subType: DataErrorSubType
) : FrolloSDKError() {

    override val localizedDescription : String
        get() = if (subType.type == type) subType.toLocalizedString(context)
                else type.toLocalizedString(context)

    override val debugDescription: String
        get() = "DataError: ${ type.name }.${ subType.name }: $localizedDescription"
}