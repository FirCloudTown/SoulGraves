package dev.faultyfunctions.soulgraves.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.faultyfunctions.soulgraves.SoulGraves
import dev.faultyfunctions.soulgraves.managers.DatabaseManager
import dev.faultyfunctions.soulgraves.utils.Soul

class MySQLDatabase {

    lateinit var dataSource: HikariDataSource


    // DATABASE VALUES
    lateinit var jdbc_url: String
    lateinit var jdbc_driver: String
    lateinit var username: String
    lateinit var password: String

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

        SoulGraves.plugin.logger.info("Connected to MySQL Database!")
        return this
    }

    // Create Table
    fun createTable() {
        val connection = dataSource.connection
        val statement = connection.createStatement()
        // TODO: Create Table
        val sql = ""
        statement.executeUpdate(sql)
    }

    // Save Soul to Database
    fun saveSoul(soul: Soul) {

    }

    // Read Soul form Database
    fun readSoul(uuid: String) : MutableList<Soul> {
        val souls = ArrayList<Soul>()

        val connection = dataSource.connection
        val statement = connection.createStatement()


        return mutableListOf()
    }



}