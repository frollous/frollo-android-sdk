package us.frollo.frollosdk.data.remote

internal interface IApiProvider {
    fun <T> create(service: Class<T>): T
}