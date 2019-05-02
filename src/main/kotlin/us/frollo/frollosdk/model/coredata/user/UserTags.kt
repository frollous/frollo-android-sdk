package us.frollo.frollosdk.model.coredata.user

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel

@Entity(tableName = "user_tags")
data class UserTags(

        @NonNull
        @ColumnInfo(name = "tag") var tag: String
) : IAdapterModel {

    /** Unique ID of the report */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var tagId: Long = 0
}