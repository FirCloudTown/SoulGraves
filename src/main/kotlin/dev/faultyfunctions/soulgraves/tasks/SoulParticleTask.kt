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
    private var interval = ConfigManager.particleInterval
    private var amount = ConfigManager.particleAmount
    private var duration = ConfigManager.particleDuration
    private var followRadius = ConfigManager.particlesFollowRadius

    val nearbyPlayerFilter: (Entity, UUID) -> Boolean = { entity, uuid ->
        entity is Player && entity.uniqueId == uuid
    }

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
            object : BukkitRunnable() {
                var tick: Int = 0
                var locations: MutableList<org.bukkit.Location> = mutableListOf()

                override fun run() {
                    if (!nearbyPlayerFilter(nearbyPlayer, uuid)) cancel()

                    if (tick == 0) {
                        // 计算粒子轨迹
                        val direction = nearbyPlayer.eyeLocation.direction
                        val origin = nearbyPlayer.eyeLocation.add(direction)
                        val unit = soul.location.clone().toVector().subtract(origin.toVector()).normalize()
                        locations = MutableList(amount) { i ->
                            origin.clone().add(unit.clone().multiply(i * space))
                        }
                    }

                    // 控制粒子的生成
                    if (tick < amount) {
                        world.spawnParticle(particle, locations[tick], 1, 0.1, 0.1, 0.1, 0.0)
                        tick++
                    } else if (tick < duration + amount) {
                        // 跳过粒子的生成
                        tick++
                    }

                    // 重置计时
                    if (tick >= duration + amount) tick = 0
                }
            }.runTaskTimerAsynchronously(SoulGraves.plugin, 1, interval)
        }
    }
}
