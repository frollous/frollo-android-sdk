package us.frollo.frollosdk.model.coredata.aggregation.tags

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel

@Entity(tableName = "transaction_tags")
data class TransactionTags(
        @NonNull @PrimaryKey @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "count") val count: Long,
        @ColumnInfo(name = "lastUsedAt") @Nullable var lastUsedAt: String? = null,
        @ColumnInfo(name = "createdAt") @Nullable var createdAt: String? = null
) : IAdapterModel