package us.frollo.frollosdk.model.api.aggregation.tags

import com.google.gson.annotations.SerializedName

data class SuggestedTagsResponse(
        @SerializedName("name") val name: String,
        @SerializedName("count") val count: Long,
        @SerializedName("last_used_at") val lastUsedAt: String?, // ISO8601 format Eg: 2011-12-03T10:15:30+01:00
        @SerializedName("created_at") val createdAt: String? )// ISO8601 format Eg: 2011-12-03T10:15:30+01:00