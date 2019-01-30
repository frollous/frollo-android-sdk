package us.frollo.frollosdk.data.remote

interface IApiProvider {
    fun <T> create(service: Class<T>): T
}