package us.frollo.frollosdk.extensions

import us.frollo.frollosdk.error.DataError

internal fun DataError.toJson() : String {
    return "{\"type\":\"${this.type.name}\",\"sub_type\":\"${this.subType.name}\"}"
}