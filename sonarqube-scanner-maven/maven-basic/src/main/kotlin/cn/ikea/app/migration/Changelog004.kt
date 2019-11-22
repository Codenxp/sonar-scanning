package cn.ikea.app.migration

import com.github.mongobee.changeset.ChangeLog
import com.github.mongobee.changeset.ChangeSet
import org.springframework.data.mongodb.core.MongoTemplate

@ChangeLog
class Changelog004 {
    @ChangeSet(order = "004", id = "create app_version collection", author = "yifei")
    fun createAppVersionCollection(mongoTemplate: MongoTemplate) {
        mongoTemplate.createCollection("app_version")
    }
}
