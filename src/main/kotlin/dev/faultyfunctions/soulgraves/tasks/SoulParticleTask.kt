package dev.faultyfunctions.soulgraves.tasks

import dev.faultyfunctions.soulgraves.SoulGraves
import dev.faultyfunctions.soulgraves.managers.ConfigManager
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class SoulParticleTask : BukkitRunnable() {

    val nearbyPlayerFilter: (Entity, UUID) -> Boolean = { entity, uuid ->
        entity is Player && entity.uniqueId == uuid
    }

    override fun run() {
        if (!ConfigManager.enableParticles) return

        val soulIterator = SoulGraves.soulList.iterator()
        while (soulIterator.hasNext()) {
            val soul = soulIterator.next()
            val world = soul.location.world
            val uuid = soul.ownerUUID

            if (world?.isChunkLoaded(soul.location.chunk) != true) continue

            val nearbyPlayer = world.getNearbyEntities(soul.location, ConfigManager.particlesFollowRadius, ConfigManager.particlesFollowRadius, ConfigManager.particlesFollowRadius)
                .filterIsInstance<Player>()
                .firstOrNull { it.uniqueId == uuid }

            if (nearbyPlayer == null) continue

            // 检查灵魂是否已被拾取或玩家状态是否满足条件
            if (!SoulGraves.soulList.contains(soul) ||
                !nearbyPlayerFilter(nearbyPlayer, uuid) ||
                !nearbyPlayer.isOnline ||
                nearbyPlayer.location.distance(soul.location) > ConfigManager.particlesFollowRadius) {
                return
            }

            // 生成粒子
            val direction = nearbyPlayer.eyeLocation.direction.setY(0)
            val origin = nearbyPlayer.eyeLocation.add(direction.multiply(ConfigManager.particlesInitDistance))
            val line = soul.location.clone().toVector().subtract(origin.toVector()) // 从origin指向soul的向量，不是单位向量

            // 参数：粒子类型，起始位置，粒子数量（为0时为向量模式）， 粒子生成随机偏移xyz（向量模式时为粒子方向），速度，特殊数据，强制显示
            val random = Random()
            val offsetBound = ConfigManager.particleOffsetBound
            repeat(random.nextInt(1, ConfigManager.particleMaxAmount + 1)) {
                world.spawnParticle(
                    ConfigManager.particleType,
                    origin.clone().add(
                        random.nextDouble(-offsetBound, offsetBound),
                        random.nextDouble(-offsetBound, offsetBound),
                        random.nextDouble(-offsetBound, offsetBound)),
                    0,  // 向量模式
                    line.x, line.y,line.z,
                    ConfigManager.particleSpeed,
                    null,true
                )
            }
        }
    }
}
