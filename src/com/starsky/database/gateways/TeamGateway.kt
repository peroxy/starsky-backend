package com.starsky.database.gateways

import com.starsky.models.Team
import com.starsky.models.TeamMembers
import com.starsky.models.Teams
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select

object TeamGateway : Gateway() {
    fun getByOwnerId(id: Int): Collection<Team> {
        var teams = listOf<Team>()
        transaction {
            val results = Team.find { Teams.owner eq id }.toList()
            teams = results
        }
        return teams
    }

    fun getByEmployeeId(id: Int): Collection<Team> {
        var teams = listOf<Team>()
        transaction {
            val query =
                Teams
                    .join(TeamMembers, JoinType.INNER, Teams.id, TeamMembers.team)
                    .slice(Teams.id, Teams.name)
                    .select { TeamMembers.user eq id }
            teams = Team.wrapRows(query).toList()
        }
        return teams
    }
}