/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.base

import org.junit.Assert
import org.junit.Test
import java.lang.Exception

class SimpleSQLiteQueryBuilderTest {

    @Test
    fun testGeneratedSQLQueries() {
        var query = SimpleSQLiteQueryBuilder("message")
            .create()
        Assert.assertEquals("SELECT  *  FROM message", query.sql)

        query = SimpleSQLiteQueryBuilder("message")
            .distinct()
            .create()
        Assert.assertEquals("SELECT DISTINCT  *  FROM message", query.sql)

        query = SimpleSQLiteQueryBuilder("message")
            .columns(arrayOf("msg_id", "placement", "title"))
            .create()
        Assert.assertEquals("SELECT msg_id, placement, title  FROM message", query.sql)

        query = SimpleSQLiteQueryBuilder("message")
            .groupBy("placement")
            .create()
        Assert.assertEquals("SELECT  *  FROM message GROUP BY placement", query.sql)

        query = SimpleSQLiteQueryBuilder("message")
            .limit("1")
            .create()
        Assert.assertEquals("SELECT  *  FROM message LIMIT 1", query.sql)

        query = SimpleSQLiteQueryBuilder("message")
            .orderBy("placement ASC")
            .create()
        Assert.assertEquals("SELECT  *  FROM message ORDER BY placement ASC", query.sql)

        query = SimpleSQLiteQueryBuilder("message")
            .appendSelection("((message_types LIKE '%|survey|%') OR (message_types LIKE '%|event|%'))")
            .appendSelection("read = 0")
            .appendSelection("content_type = 'VIDEO'")
            .create()
        Assert.assertEquals("SELECT  *  FROM message WHERE ((message_types LIKE '%|survey|%') OR (message_types LIKE '%|event|%')) AND read = 0 AND content_type = 'VIDEO' ", query.sql)

        query = SimpleSQLiteQueryBuilder("message")
            .groupBy("placement")
            .having("placement = 1")
            .create()
        Assert.assertEquals("SELECT  *  FROM message GROUP BY placement HAVING placement = 1", query.sql)
    }

    @Test
    fun testGeneratedHavingSQLQueryFail() {
        try {
            SimpleSQLiteQueryBuilder("message")
                .having("placement = 1")
                .create()
        } catch (e: Exception) {
            Assert.assertEquals("HAVING clauses are only permitted when using a GROUP BY clause", e.localizedMessage)
        }
    }

    @Test
    fun testGeneratedLimitSQLQueryFail() {
        try {
            SimpleSQLiteQueryBuilder("message")
                .limit("XYZ")
                .create()
        } catch (e: Exception) {
            Assert.assertEquals("invalid LIMIT clauses:XYZ", e.localizedMessage)
        }
    }
}
