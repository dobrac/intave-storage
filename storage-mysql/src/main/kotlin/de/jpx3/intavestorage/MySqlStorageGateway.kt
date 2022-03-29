package de.jpx3.intavestorage

import com.mysql.cj.jdbc.Driver
import java.sql.DriverManager
import java.sql.PreparedStatement

class MySqlStorageGateway(config: MySqlConfiguration) : JdbcBackedStorage {
    private val connection = run {
        DriverManager.registerDriver(Driver())
        DriverManager.getConnection(config.url, config.user, config.password)
    }

    init {
        prepareTable()
    }

    override fun prepareTable() {
        connection.createStatement().execute(
            """
            CREATE TABLE IF NOT EXISTS intave_storage (
                id CHAR(36) PRIMARY KEY NOT NULL,
                data MEDIUMBLOB NOT NULL,
                last_used BIGINT NOT NULL
            )
            """
        )
    }

    override fun requestStorageQuery(): PreparedStatement {
        return connection.prepareStatement(
            """
            SELECT data
            FROM intave_storage
            WHERE id = ?
            """
        )
    }

    override fun saveStorageQuery(): PreparedStatement {
        return connection.prepareStatement(
            """
            INSERT INTO intave_storage
            VALUES(?, ?, ?) as excluded
            ON DUPLICATE KEY UPDATE 
                data = excluded.data,
                last_used = excluded.last_used
            """
        )
    }

    override fun clearEntriesQuery(): PreparedStatement {
        return connection.prepareStatement(
            """
            DELETE FROM intave_storage
            WHERE last_used < ?
            """
        )
    }
}