package com.starsky.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object TeamMembers : IntIdTable("team_member") {
    val team = reference("team_id", Teams)
    val user = reference("user_id", Users)
}

class TeamMember(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TeamMember>(TeamMembers)

    var user by User referencedOn TeamMembers.user
    var team by Team referencedOn TeamMembers.team
}