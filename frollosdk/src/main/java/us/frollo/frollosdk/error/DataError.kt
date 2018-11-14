package us.frollo.frollosdk.error

class DataError(val type: DataErrorType, val subType: DataErrorSubType) : FrolloSDKError() {

    override val localizedDescription : String
        get() = if (subType.type == type) subType.toLocalizedString(context)
                else type.toLocalizedString(context)

    override val debugDescription: String
        get() = "DataError: ${ type.name }.${ subType.name }: $localizedDescription"
}