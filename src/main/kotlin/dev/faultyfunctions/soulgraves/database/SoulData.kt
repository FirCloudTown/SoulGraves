package dev.faultyfunctions.soulgraves.database

import org.bukkit.inventory.ItemStack
import kotlin.collections.List

data class SoulData(
    val uuid: String,
    val markerUUID: String,
    val serverName: String,
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val inventory: List<ItemStack>,
    val xp: Int,
    val expireTime: Long
)