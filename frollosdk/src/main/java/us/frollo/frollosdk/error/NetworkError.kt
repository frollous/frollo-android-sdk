package us.frollo.frollosdk.error

class NetworkError(val type: NetworkErrorType) : FrolloSDKError() {

    // TODO: Include system network errors
    override val localizedDescription: String?
        get() = type.toLocalizedString(context)

    override val debugDescription: String?
        get() = "NetworkError: ${ type.name }: $localizedDescription"
}