package dev.faultyfunctions.soulgraves.tasks

import dev.faultyfunctions.soulgraves.SoulGraves
import dev.faultyfunctions.soulgraves.managers.ConfigManager
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

class ParticleLineTask : BukkitRunnable() {

    private var enableParticles = ConfigManager.enableParticles // 是否启用粒子

    private var particle = Particle.SOUL_FIRE_FLAME // 粒子配置
    private var space = ConfigManager.particleSpace // 粒子间的距离倍数
    private var interval = ConfigManager.particleInterval // 每隔多少 tick 运行一次
    private var amount = ConfigManager.particleAmount // 粒子线中的点数量
    private var duration = ConfigManager.particleDuration // 粒子束间的间隔时间 (tick 数)
    private var followRadius = ConfigManager.particlesFollowRadius // 跟随半径

    val nearbyPlayerFilter: (Entity, UUID) -> Boolean = { entity, uuid ->
        entity is Player && entity.uniqueId == uuid
    }

    override fun run() {
        if (!enableParticles) return

        val soulIterator = SoulGraves.soulList.iterator()
        while (soulIterator.hasNext()){
            val soul = soulIterator.next()
            val world = soul.location.world
            val uuid = soul.ownerUUID

            if (!world?.isChunkLoaded(soul.location.chunk)!!) continue

            val nearbyPlayer =
                world.getNearbyEntities(soul.location, followRadius, followRadius, followRadius) {
                    it is Player
                    it.uniqueId == uuid
                }

            if (nearbyPlayer.isEmpty()) continue
            val player: Player = nearbyPlayer.toTypedArray().first() as Player



            object : BukkitRunnable() {
                var tick: Int = 0 // 计时变量

                override fun run() {
                    if (!nearbyPlayerFilter(player, uuid)) cancel()

                    if (tick == 0) {
                        val direction = player.eyeLocation.direction // 代表玩家视线方向的单位向量
                        val origin = player.eyeLocation.add(direction) // 发射源,在玩家视线往前1格处
                        val unit = soul.location.clone().toVector().subtract(origin.toVector()).normalize() // 从发射源指向目的地的单位方向向量
                        val locations = MutableList(amount) { i ->
                            origin.clone().add(unit.clone().multiply(i * space))
                        }
                    }

                    // 控制粒子生成或跳过
                    if (tick < amount) {
                        // 生成粒子
                        object : BukkitRunnable() {
                            val p: Int = tick
                            override fun run() {
                                world.spawnParticle(particle, locations!![p], 1, 0.1, 0.1, 0.1, 0.0)
                            }
                        }.runTask(SoulGraves.plugin)
                        tick++
                    } else if (tick < duration + amount) {
                        // 跳过
                        tick++
                        return
                    }

                    // 重置，开始准备新的粒子束
                    if (tick >= duration + amount) tick = 0


                }
            }.runTaskTimerAsynchronously(SoulGraves.plugin, 1, interval)
        }
    }

}