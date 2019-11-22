package cn.ikea.app.migration

import cn.ikea.app.masterdata.domain.entity.PolicyVersion
import com.github.mongobee.changeset.ChangeLog
import com.github.mongobee.changeset.ChangeSet
import org.springframework.data.mongodb.core.MongoTemplate

@ChangeLog
class CreatePolicyVersionCollection005 {
    @ChangeSet(order = "005", id = "create app_policy_version collection", author = "yifei")
    fun createAppVersionCollection(mongoTemplate: MongoTemplate) {
        if (!mongoTemplate.collectionExists("app_policy_version")) {
            mongoTemplate.createCollection(PolicyVersion::class.java)
        }
    }
}
