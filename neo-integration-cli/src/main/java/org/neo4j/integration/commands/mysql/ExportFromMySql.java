package org.neo4j.integration.commands.mysql;

import java.util.concurrent.Callable;

import org.neo4j.integration.environment.Environment;
import org.neo4j.integration.neo4j.importcsv.ImportFromCsvCommand;
import org.neo4j.integration.neo4j.importcsv.config.ImportConfig;
import org.neo4j.integration.neo4j.importcsv.config.Manifest;
import org.neo4j.integration.neo4j.importcsv.config.formatting.Formatting;
import org.neo4j.integration.neo4j.importcsv.fields.IdType;
import org.neo4j.integration.sql.ConnectionConfig;
import org.neo4j.integration.sql.exportcsv.ExportToCsvCommand;
import org.neo4j.integration.sql.exportcsv.ExportToCsvConfig;
import org.neo4j.integration.sql.exportcsv.io.TinyIntResolver;
import org.neo4j.integration.sql.exportcsv.mapping.MetadataMappings;

public class ExportFromMySql implements Callable<Void>
{

    private final ExportFromMySqlEvents events;
    private final MetadataMappings metadataMappings;
    private final ConnectionConfig connectionConfig;
    private final Formatting formatting;
    private final Environment environment;
    private TinyIntResolver tinyIntResolver;

    public ExportFromMySql( MetadataMappings metadataMappings,
                            ConnectionConfig connectionConfig,
                            Formatting formatting,
                            Environment environment,
                            TinyIntResolver tinyIntResolver )
    {
        this( ExportFromMySqlEvents.EMPTY,
                metadataMappings,
                connectionConfig,
                formatting,
                environment,
                tinyIntResolver );
    }

    public ExportFromMySql( ExportFromMySqlEvents events,
                            MetadataMappings metadataMappings,
                            ConnectionConfig connectionConfig,
                            Formatting formatting,
                            Environment environment,
                            TinyIntResolver tinyIntResolver )
    {
        this.events = events;
        this.metadataMappings = metadataMappings;
        this.connectionConfig = connectionConfig;
        this.formatting = formatting;
        this.environment = environment;
        this.tinyIntResolver = tinyIntResolver;
    }

    @Override
    public Void call() throws Exception
    {
        ExportToCsvConfig config = ExportToCsvConfig.builder()
                .destination( environment.csvDirectory() )
                .connectionConfig( connectionConfig )
                .formatting( formatting )
                .build();

        events.onExportingToCsv( environment.csvDirectory() );

        Manifest manifest = new ExportToCsvCommand( config, metadataMappings, tinyIntResolver ).execute();

        events.onCreatingNeo4jStore();

        doImport( formatting, manifest );

        events.onExportComplete( environment.destinationDirectory() );

        return null;
    }

    private void doImport( Formatting formatting, Manifest manifest ) throws Exception
    {
        ImportConfig.Builder builder = ImportConfig.builder()
                .importToolDirectory( environment.importToolDirectory() )
                .importToolOptions( environment.importToolOptions() )
                .destination( environment.destinationDirectory() )
                .formatting( formatting )
                .idType( IdType.String );

        manifest.addNodesAndRelationshipsToBuilder( builder );

        new ImportFromCsvCommand( builder.build() ).execute();
    }
}
