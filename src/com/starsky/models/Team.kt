package com.starsky.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Teams : IntIdTable("team") {
    val name = text("name")
    val owner = reference("owner_user_id", Users)
}

class Team(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Team>(Teams)

    var name by Teams.name
    var owner by User referencedOn Teams.owner
}