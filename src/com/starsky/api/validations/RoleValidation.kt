package com.starsky.api.validations

import com.starsky.api.security.UserPrincipal
import com.starsky.models.UserRoleEnum

object RoleValidation {
    fun hasAnyRole(user: UserPrincipal, roles: Collection<UserRoleEnum>): Boolean {
        var userRole: UserRoleEnum? = UserRoleEnum.valueOf(user.roleId) ?: return false
        return roles.contains(userRole)
    }
}