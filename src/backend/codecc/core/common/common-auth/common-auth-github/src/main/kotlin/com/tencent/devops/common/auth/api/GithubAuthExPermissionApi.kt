package com.tencent.devops.common.auth.api

import com.tencent.devops.auth.api.service.ServicePermissionAuthResource
import com.tencent.devops.common.auth.api.external.AbstractAuthExPermissionApi
import com.tencent.devops.common.auth.api.external.AuthTaskService
import com.tencent.devops.common.auth.api.pojo.external.CodeCCAuthAction
import com.tencent.devops.common.auth.api.pojo.external.CodeCCAuthResourceType
import com.tencent.devops.common.auth.api.pojo.external.model.BkAuthExResourceActionModel
import com.tencent.devops.common.auth.pojo.GithubAuthProperties
import com.tencent.devops.common.client.Client
import org.springframework.data.redis.core.RedisTemplate

class GithubAuthExPermissionApi(client: Client,
                                redisTemplate: RedisTemplate<String, String>,
                                private val properties : GithubAuthProperties)
    : AbstractAuthExPermissionApi(
    client,
    redisTemplate) {

    override fun queryPipelineListForUser(user: String, projectId: String, actions: Set<String>): Set<String> {
        return emptySet()
    }

    override fun queryTaskListForUser(user: String, projectId: String, actions: Set<String>): Set<String> {
        val result = client.getDevopsService(ServicePermissionAuthResource::class.java)
            .getUserResourcesByPermissions(user, properties.token ?: "", actions.toList(), projectId,
                CodeCCAuthResourceType.CODECC_TASK.value)
        if (result.isNotOk() || result.data.isNullOrEmpty()) {
            return emptySet()
        }
        val taskIds = mutableSetOf<String>()
        result.data!!.forEach { entry -> taskIds.addAll(entry.value) }
        return taskIds
    }

    override fun queryTaskUserListForAction(taskId: String, projectId: String, actions: Set<String>): List<String> {
        return emptyList()
    }

    override fun validatePipelineBatchPermission(user: String, pipelineId: String, projectId: String, actions: Set<String>): List<BkAuthExResourceActionModel> {
        return listOf(BkAuthExResourceActionModel("", "", listOf(), true))
    }

    override fun validateTaskBatchPermission(user: String, taskId: String, projectId: String, actions: Set<String>): List<BkAuthExResourceActionModel> {
        return listOf(BkAuthExResourceActionModel(isPass = true))
    }

    override fun validateGongfengPermission(user: String, taskId: String, projectId: String, actions: List<CodeCCAuthAction>): Boolean {
        return true
    }

    override fun authProjectManager(projectId: String, user: String): Boolean {
        return true
    }

}