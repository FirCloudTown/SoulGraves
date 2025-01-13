package dev.faultyfunctions.soulgraves.managers

import dev.faultyfunctions.soulgraves.SoulGraves
import dev.faultyfunctions.soulgraves.database.MySQLDatabase
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object DatabaseManager {
    private lateinit var file: File
    val config: YamlConfiguration = YamlConfiguration()

    // DATABASE VALUES
    lateinit var mysqlDatabase: MySQLDatabase

    init {
        initialize()
    }

    private fun initialize() {
        // GRAB FILE
        file = File(SoulGraves.plugin.dataFolder, "database.yml")

        // CREATE FILE IF IT DOESN'T EXIST
        if (!file.exists())
            SoulGraves.plugin.saveResource("database.yml", false)

        // MAKE SURE WE KEEP COMMENTS
        config.options().parseComments(true)

        // LOAD FILE
        try {
            config.load(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // LOAD VALUES
        val type = config.getString("data-storage-method", "Null")
        when (type?.lowercase()) {
            "MySQL" -> mysqlDatabase = MySQLDatabase().init()
        }

    }
}