package me.magicsu.onedayup

import me.magicsu.onedayup.model.Page
import org.jooq.*

/**
 * Author: sush
 * Date: 2018/02/24.
 * Function:
 */
abstract class AbstractCrudRepository<R : UpdatableRecord<*>, ID, P : IdentifiableDTO<ID>>(
        protected val dslContext: DSLContext,
        private val table: Table<R>,
        private val idField: Field<ID>,
        private val pojoClass: Class<P>) {

    fun getPage(page: Int?, pageSize: Int?, sortField: String, sortOrder: String, filterQuery: Map<String, Any>?): List<P> {
        val sortFieldName = sortField.replace("([A-Z])".toRegex(), "_$1").toLowerCase()
        return dslContext.selectFrom(table)
                .where(filter(filterQuery ?: HashMap()))
                .orderBy(table.field(sortFieldName).sort(SortOrder.valueOf(sortOrder)))
                .limit(pageSize!!)
                .offset((page!! - 1) * pageSize)
                .fetchInto(pojoClass)
    }

    open fun getOneById(id: ID): P {
        return dslContext.selectFrom(table)
                .where(idField.eq(id))
                .fetchOneInto(pojoClass)
    }

    fun getOne(condition: Condition): P? {
        return dslContext.selectFrom(table).where(condition).limit(1).fetchOne()?.into(pojoClass)
    }

    fun getRecordById(id: ID): R {
        return dslContext.selectFrom(table)
                .where(idField.eq(id))
                .fetchOne()
    }

    fun update(id: ID, pojo: P): P {
        val record = getRecordById(id)

        record.from(pojo)
        record.store()
        return record.into(pojoClass)
    }

    fun create(pojo: P): P {
        val record = newRecord(pojo)
        record.store()
        return record.into(pojoClass)
    }

    fun save(pojo: P) {
        newRecord(pojo).store()
    }

    fun delete(id: ID) {
        dslContext.delete(table).where(idField.eq(id)).execute()
    }

    open fun filter(filterQuery: Map<String, Any>): Condition? {
        return null
    }

    fun newRecord(obj: Any): R {
        return dslContext.newRecord(table, obj)
    }

    fun list(condition: Condition): List<P> {
        return dslContext.selectFrom(table)
                .where(condition)
                .orderBy(idField.asc())
                .fetchInto(pojoClass)
    }

    fun list(condition: Condition, sortField: SortField<*>): List<P> {
        return dslContext.selectFrom(table)
                .where(condition)
                .orderBy(sortField)
                .fetchInto(pojoClass)
    }

    fun list(condition: Condition, sortFields: Collection<SortField<*>>): List<P> {
        return dslContext.selectFrom(table)
                .where(condition)
                .orderBy(sortFields)
                .fetchInto(pojoClass)
    }

    @JvmOverloads
    fun list(pageNum: Int?, pageSize: Int?, condition: Condition, sortFields: List<SortField<*>>?): List<P> {
        return dslContext.selectFrom(table)
                .where(condition)
                .orderBy(sortFields)
                .limit(pageSize!!)
                .offset((pageNum!! - 1) * pageSize)
                .fetchInto(pojoClass)
    }

    @JvmOverloads
    fun page(pageNum: Int, pageSize: Int, condition: Condition, sortField: SortField<*> = idField.asc()): Page {
        return page(pageNum, pageSize, condition, listOf(sortField))
    }

    fun page(pageNum: Int, pageSize: Int, condition: Condition, sortFields: List<SortField<*>>): Page {
        val totalRow = getTotalRow(condition)

        if (totalRow == 0) {
            return Page(listOf(), pageNum!!, pageSize!!, 0, 0)
        } else {
            var totalPage = getTotalPage(totalRow, pageSize)
            return if (pageNum > totalPage) {
                Page(null, pageNum!!, pageSize, totalPage, totalRow)
            } else {
                val list = dslContext
                        .selectFrom(table)
                        .where(condition)
                        .orderBy(sortFields)
                        .limit(pageSize)
                        .offset((pageNum!! - 1) * pageSize)
                        .fetchInto(pojoClass)
                Page(list, pageNum, pageSize, totalPage, totalRow)
            }
        }
    }

    fun getTotalRow(condition: Condition): Int {
        val result = dslContext.selectCount()
                .from(table)
                .where(condition).fetch()
        return if (result == null || result.isEmpty()) 0 else result[0].value1()
    }

    fun getTotalPage(totalRow: Int, pageSize: Int): Int {
        var totalPage = totalRow / pageSize
        if ((totalRow % pageSize).toLong() != 0L) {
            ++totalPage
        }
        return totalPage
    }
}