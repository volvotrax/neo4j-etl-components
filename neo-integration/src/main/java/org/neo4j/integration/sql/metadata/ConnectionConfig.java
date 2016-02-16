package org.neo4j.integration.sql.metadata;

import java.net.URI;

import org.neo4j.integration.sql.DatabaseType;
import org.neo4j.integration.util.Preconditions;

public class ConnectionConfig
{
    public static Builder.SetHost forDatabase(DatabaseType databaseType)
    {
        return new ConnectionConfigBuilder(databaseType);
    }

    private final DatabaseType databaseType;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    ConnectionConfig( ConnectionConfigBuilder builder )
    {
        this.databaseType = Preconditions.requireNonNull( builder.databaseType, "DatabaseType" );
        this.host = Preconditions.requireNonNullString( builder.host, "Host" );
        this.port = builder.port;
        this.database = Preconditions.requireNonNullString( builder.database, "Database" );
        this.username = Preconditions.requireNonNullString( builder.username, "Username" );
        this.password = Preconditions.requireNonNullString( builder.password, "Password" );
    }

    public String driverClassName()
    {
        return databaseType.driverClassName();
    }

    public URI uri()
    {
        return databaseType.createUri( host, port, database );
    }

    public String username()
    {
        return username;
    }

    public String password()
    {
        return password;
    }

    public interface Builder
    {
        interface SetHost
        {
            SetPort host( String host );
        }

        interface SetPort
        {
            SetDatabase port( int port );
        }

        interface SetDatabase
        {
            SetUsername database( String database );
        }

        interface SetUsername
        {
            SetPassword username(String username);
        }

        interface SetPassword
        {
            Builder password(String password);
        }

        ConnectionConfig build();
    }
}
