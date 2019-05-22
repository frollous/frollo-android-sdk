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

import androidx.sqlite.db.SimpleSQLiteQuery
import java.util.regex.Pattern

/**
 * A simple query builder to create SQL SELECT queries.
 */
class SimpleSQLiteQueryBuilder(
        /** The table name to query. */
        private val tableName : String
) {

    private val limitPattern = Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?")

    private var distinct = false
    private val selections = mutableListOf<String>()
    private var columns : Array<String>? = null
    private var groupBy : String? = null
    private var orderBy : String? = null
    private var having : String? = null
    private var limit : String? = null

    /**
     * Sets the given list of columns as the columns that will be returned.
     *
     * @param columns The list of column names that should be returned.
     */
    fun columns(columns : Array<String>) = apply {
        this.columns = columns
    }

    /**
     * Adds an ORDER BY statement.
     *
     * @param orderBy The order clause.
     */
    fun orderBy(orderBy : String) = apply {
        this.orderBy = orderBy
    }

    /**
     * Adds a GROUP BY statement.
     *
     * @param groupBy The value of the GROUP BY statement.
     */
    fun groupBy(groupBy : String) = apply {
        this.groupBy = groupBy
    }

    /**
     * Adds a HAVING statement. You must also provide [groupBy] String for this to work.
     *
     * @param having The having clause.
     */
    fun having(having : String) = apply {
        this.having = having
    }

    /**
     * Adds a LIMIT statement.
     *
     * @param limit The limit value.
     */
    fun limit(limit : String) = apply {
        if (!isEmpty(limit) && !limitPattern.matcher(limit).matches()) {
            throw IllegalArgumentException("invalid LIMIT clauses:$limit")
        }
        this.limit = limit
    }

    /**
     * Adds DISTINCT keyword to the query.
     */
    fun distinct() = apply {
        this.distinct = true
    }

    /**
     * Appends to the selection criteria for the WHERE clause.
     *
     * Note: These selection criteria strings will be joined together with "AND"
     *
     * @param selection The selection criteria. Eg: "name = 'Foo'"
     */
    fun appendSelection(selection: String) = apply {
        this.selections.add(selection)
    }

    /**
     * Creates the SimpleSQLiteQuery
     *
     * @return a new query
     */
    fun create() : SimpleSQLiteQuery {
        if (isEmpty(groupBy) && !isEmpty(having)) {
            throw IllegalArgumentException(
                    "HAVING clauses are only permitted when using a GROUP BY clause")
        }
        val query = StringBuilder()

        query.append("SELECT ")
        if (distinct) {
            query.append("DISTINCT ")
        }

        val columns = this.columns
        if (columns?.isNotEmpty() == true) {
            appendColumns(query, columns)
        } else {
            query.append(" * ")
        }
        query.append(" FROM ")
        query.append(tableName)
        appendSelections(query, " WHERE ", selections)
        appendClause(query, " GROUP BY ", groupBy)
        appendClause(query, " HAVING ", having)
        appendClause(query, " ORDER BY ", orderBy)
        appendClause(query, " LIMIT ", limit)

        return SimpleSQLiteQuery(query.toString())
    }

    private fun appendSelections(s: StringBuilder, name: String, selections: List<String>) {
        if (selections.isNotEmpty()) {
            s.append(name)
            selections.forEachIndexed { index, selection ->
                s.append(selection)
                if (index < selections.size - 1)
                    s.append(" AND ")
            }
            s.append(' ')
        }
    }

    private fun appendClause(s: StringBuilder, name: String, clause: String?) {
        if (!isEmpty(clause)) {
            s.append(name)
            s.append(clause)
        }
    }

    /**
     * Add the names that are non-null in columns to s, separating
     * them with commas.
     */
    private fun appendColumns(s: StringBuilder, columns: Array<String>) {
        columns.forEachIndexed { index, column ->
            s.append(column)
            if (index < columns.size - 1)
                s.append(", ")
        }
        s.append(' ')
    }

    private fun isEmpty(input: String?): Boolean {
        return input == null || input.isBlank()
    }
}