package com.starsky.database.gateways

import com.starsky.api.responses.TeamResponse
import com.starsky.models.Team
import com.starsky.models.TeamMembers
import com.starsky.models.Teams
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select

object TeamGateway : Gateway() {
    fun getByOwnerId(id: Int): Collection<TeamResponse> {
        val teams = mutableListOf<TeamResponse>()
        transaction {
            val results = Team.find { Teams.owner eq id }.map { it.toResponse() }
            teams.addAll(results)
        }
        return teams
    }

    fun getByEmployeeId(id: Int): Collection<TeamResponse> {
        val teams = mutableListOf<TeamResponse>()
        transaction {
            val result =
                Teams
                    .join(TeamMembers, JoinType.INNER, Teams.id, TeamMembers.team)
                    .slice(Teams.id, Teams.name)
                    .select { TeamMembers.user eq id }
                    .map { TeamResponse(it[Teams.id].value, it[Teams.name]) }
            teams.addAll(result)
        }
        return teams
    }
}