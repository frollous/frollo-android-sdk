package us.frollo.frollosdk.network

internal interface IApiProvider {
    fun <T> create(service: Class<T>): T
}