package us.frollo.frollosdk.model.api.aggregation.tags

import com.google.gson.annotations.SerializedName

internal data class TransactionTagsResponse(
        @SerializedName("name") val name: String,
        @SerializedName("count") val count: Long,
        @SerializedName("last_used_at") val lastUsedAt: String? = null, // yyyy-MM or yyyy-MM-dd
        @SerializedName("created_at") val createdAt: String? = null)