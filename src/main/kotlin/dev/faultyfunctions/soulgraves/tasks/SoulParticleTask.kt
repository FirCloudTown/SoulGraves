package dev.faultyfunctions.soulgraves.tasks

import dev.faultyfunctions.soulgraves.SoulGraves
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class SoulParticleTask: BukkitRunnable() {
    // TODO("Not yet implemented")

    override fun run() {
        val soulIterator = SoulGraves.soulList.iterator()
        while (soulIterator.hasNext()) {
            val soul = soulIterator.next()
            val world = soul.location.world


        }

    }
}