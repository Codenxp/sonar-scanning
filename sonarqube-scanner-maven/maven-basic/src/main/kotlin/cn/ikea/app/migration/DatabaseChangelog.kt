package cn.ikea.app.migration

import cn.ikea.app.content.domain.model.entity.TopCatalog
import com.github.mongobee.changeset.ChangeLog
import com.github.mongobee.changeset.ChangeSet
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.VariableOperators.mapItemsOf
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.schema.JsonSchemaObject

@ChangeLog
class DatabaseChangelog {
    val logger: Logger = LoggerFactory.getLogger("DatabaseChangelog")

    @ChangeSet(order = "001", id = "add top catalog image", author = "guofan")
    fun addTopCatalogIcon(mongoTemplate: MongoTemplate, environment: Environment) {
        val topCatalogs = mongoTemplate.find(
            Query.query(Criteria.where("icon").exists(false)),
            TopCatalog::class.java
        )
        logger.info("find ${topCatalogs.size} top catalog need to add icon")
        val env = environment.getProperty("ikea.env", "dev")
        topCatalogs.forEach { mongoTemplate.save(it) }
    }

    @ChangeSet(order = "002", id = "modify filter attributes", author = "guofan")
    fun modifyFilterAttributes(mongoTemplate: MongoTemplate) {
        val match = match(Criteria.where("filterAttributes").exists(true).and("filterAttributes.id").exists(false))
        val project = project("_id", "filterAttributes")
        val map = mapItemsOf("filterAttributes").`as`("row").andApply {
            Document().apply {
                append("_id", "$\$row.filter._id")
                append("name", "$\$row.filter.name")
                append("values", "$\$row.values")
            }
        }
        val addFields = AggregationOperation {
            Document("\$addFields", Document("filterAttributes", map.toDocument(it)))
        }
        val agg = newAggregation(match, project, addFields)
        val result = mongoTemplate.aggregate(agg, "content_product", HashMap::class.java)
        logger.info("find ${result.mappedResults.size} product need to update filter attributes")
        result.mappedResults.forEach {
            mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").`is`(it["_id"].toString())),
                Update.update("filterAttributes", it["filterAttributes"]!!),
                "content_product"
            )
            logger.info("update ${it["_id"]} done")
        }
        logger.info("update filter attributes finished")
    }

    @ChangeSet(order = "003", id = "modify sub catalog id", author = "guofan")
    fun modifySubCatalogId(mongoTemplate: MongoTemplate) {
        val all = mongoTemplate.find(
            Query.query(
                Criteria.where("parentId").type(JsonSchemaObject.Type.STRING)
            ),
            Document::class.java,
            "content_sub_catalog"
        )
        logger.info("find ${all.size} sub catalog need to update id")
        all.forEach {
            it["parentIds"] = listOf(it["parentId"])
            it.remove("parentId")
            mongoTemplate.save(it, "content_sub_catalog")
            logger.info("update sub catalog ${it["_id"]}")
        }
        logger.info("update sub catalog id finished")
    }
}
