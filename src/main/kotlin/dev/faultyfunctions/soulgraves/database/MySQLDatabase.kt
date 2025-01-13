package dev.faultyfunctions.soulgraves.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.faultyfunctions.soulgraves.SoulGraves
import dev.faultyfunctions.soulgraves.managers.ConfigManager
import dev.faultyfunctions.soulgraves.managers.DatabaseManager
import dev.faultyfunctions.soulgraves.utils.Soul
import java.util.UUID

class MySQLDatabase {

    lateinit var dataSource: HikariDataSource

    // DATABASE VALUES
    lateinit var jdbc_url: String
    lateinit var jdbc_driver: String
    lateinit var username: String
    lateinit var password: String
    val databaseName: String = "soul_grave"

    fun init() : MySQLDatabase {
        val config = DatabaseManager.config

        jdbc_url = config.getString("MySQL.jdbc-url")!!
        jdbc_driver = config.getString("MySQL.jdbc-class")!!
        username = config.getString("MySQL.properties.user")!!
        password = config.getString("MySQL.properties.password")!!

        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = jdbc_url
        hikariConfig.driverClassName = jdbc_driver
        hikariConfig.username = username
        hikariConfig.password = password

        dataSource = HikariDataSource(hikariConfig)
        createTable()

        SoulGraves.plugin.logger.info("Connected to MySQL Database!")
        return this
    }

    // Create Table
    fun createTable() {
        val connection = dataSource.connection
        val sql = "CREATE TABLE IF NOT EXISTS $databaseName (" +
                "uuid VARCHAR(255) PRIMARY KEY, " +
                "markerUUID VARCHAR(255), " +
                "serverName VARCHAR(255), " +
                "world VARCHAR(255), " +
                "x INT, " +
                "y INT, " +
                "z INT, " +
                "inventory TEXT, " +
                "xp INT, " +
                "expireTime BIGINT)"
        val statement = connection.prepareStatement(sql)
        try {
            statement.executeUpdate(sql)
            println("Table '$databaseName' created successfully.")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while creating table: ${e.message}")
        } finally {
            statement.close()
            connection.close()
        }
    }


    // Save Soul to Database
    fun saveSoul(soul: Soul) {
        var now = System.currentTimeMillis()
        val connection = dataSource.connection
        val sql = "INSERT INTO $databaseName (uuid, markerUUID, serverName, world, x, y, z, inventory, xp, expireTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        val statement = connection.prepareStatement(sql)

        statement.setString(1, soul.ownerUUID.toString())
        statement.setString(2, soul.markerUUID.toString())
        statement.setString(3, DatabaseManager.serverName)
        statement.setString(4, soul.location.world?.name)
        statement.setInt(5, soul.location.x.toInt())
        statement.setInt(6, soul.location.y.toInt())
        statement.setInt(7, soul.location.z.toInt())
        statement.setString(8, "test inventory")
        statement.setInt(9, soul.xp)
        statement.setLong(10, (ConfigManager.timeStable + ConfigManager.timeUnstable) * 1000L + now)

        statement.executeUpdate()
    }

    // Read Soul form Database
    fun readSoul(uuid: String) : MutableList<SoulData> {
        val souls = ArrayList<SoulData>()

        val connection = dataSource.connection
        val sql = "SELECT * FROM $databaseName WHERE uuid = ?"
        val statement = connection.prepareStatement(sql)
        statement.setString(1, uuid)

        try {
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val soul = SoulData(
                    uuid = resultSet.getString("uuid"),
                    markerUUID = resultSet.getString("markerUUID"),
                    serverName = resultSet.getString("serverName"),
                    world = resultSet.getString("world"),
                    x = resultSet.getDouble("x"),
                    y = resultSet.getDouble("y"),
                    z = resultSet.getDouble("z"),
                    inventory = mutableListOf(), // TODO 有一个方法来解析 inventory 字符串
                    xp = resultSet.getInt("xp"),
                    expireTime = resultSet.getLong("expireTime")
                )
                souls.add(soul)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            statement.close()
            connection.close()
        }
        return souls
    }

    // Delete Soul from Database
    fun deleteSoul(soul: Soul) {
        val uuid = soul.markerUUID.toString()
        val connection = dataSource.connection
        val sql = "DELETE FROM $databaseName WHERE markerUUID =?"
        val statement = connection.prepareStatement(sql)
        statement.setString(1, uuid)
        statement.executeUpdate()
    }



}