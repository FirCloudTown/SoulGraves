package dev.faultyfunctions.soulgraves.tasks

import dev.faultyfunctions.soulgraves.SoulGraves
import dev.faultyfunctions.soulgraves.managers.ConfigManager
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

class SoulParticleTask : BukkitRunnable() {

    private var enableParticles = ConfigManager.enableParticles
    private var particle = Particle.SOUL_FIRE_FLAME
    private var space = ConfigManager.particleSpace
    private var interval = ConfigManager.particleInterval // 改成同步任务的话任务周期由主类控制了，这个会灰了
    private var maxAmount = ConfigManager.particleAmount
    private var duration = ConfigManager.particleDuration
    private var followRadius = ConfigManager.particlesFollowRadius
    val init_distance = 3
    val min_distanceSquared = 1

    val nearbyPlayerFilter: (Entity, UUID) -> Boolean = { entity, uuid ->
        entity is Player && entity.uniqueId == uuid
    }

    var tick: Int = 0
    var locations: MutableList<org.bukkit.Location> = mutableListOf()
    var amount = maxAmount // 当粒子距离目标过近时amount会减小以防止粒子穿过目标

    override fun run() {
        if (!enableParticles) return

        val soulIterator = SoulGraves.soulList.iterator()
        while (soulIterator.hasNext()) {
            val soul = soulIterator.next()
            val world = soul.location.world
            val uuid = soul.ownerUUID

            if (world?.isChunkLoaded(soul.location.chunk) != true) continue

            val nearbyPlayer = world.getNearbyEntities(soul.location, followRadius, followRadius, followRadius)
                .filterIsInstance<Player>()
                .firstOrNull { it.uniqueId == uuid }

            if (nearbyPlayer == null) continue

            // 创建粒子任务
            // 检查灵魂是否已被拾取或玩家状态是否满足条件
            if (!SoulGraves.soulList.contains(soul) ||
                !nearbyPlayerFilter(nearbyPlayer, uuid) ||
                !nearbyPlayer.isOnline ||
                nearbyPlayer.location.distance(soul.location) > followRadius) {
                cancel() // 取消粒子任务
                return
            }

            if (tick == 0) {
                // 计算粒子轨迹
                val direction = nearbyPlayer.eyeLocation.direction
                val origin = nearbyPlayer.eyeLocation.add(direction.multiply(init_distance))
                val unit = soul.location.clone().toVector().subtract(origin.toVector()).normalize()

                for (i in 1..maxAmount) {
                    val pos = origin.clone().add(unit.clone().multiply(i * space))
                    if (pos.distanceSquared(pos) >= min_distanceSquared) {
                        locations.add(pos)
                    } else {
                        // 当粒子距离目标过近时amount会减小以防止粒子穿过目标
                        amount--
                    }
                }
            }

            // 控制粒子的生成
            if (tick < amount) {
                world.spawnParticle(particle, locations[tick], 1, 0.1, 0.1, 0.1, 0.0)
                tick++
            } else if (tick < duration + maxAmount) {
                // 跳过粒子的生成
                tick++
            } else if (tick >= duration + maxAmount) {
                // 重置计时
                tick = 0
            }
        }
    }
}
}
