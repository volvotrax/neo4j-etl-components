package org.neo4j.integration.neo4j.importcsv.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.neo4j.integration.neo4j.importcsv.config.Formatting;
import org.neo4j.integration.neo4j.importcsv.fields.CsvField;
import org.neo4j.integration.util.Loggers;

import static java.lang.String.format;

public class HeaderFileWriter
{
    private final Path directory;
    private final Formatting formatting;

    public HeaderFileWriter( Path directory, Formatting formatting )
    {
        this.directory = directory;
        this.formatting = formatting;
    }

    public Path writeHeaderFile( Collection<CsvField> fields, String filenamePrefix ) throws IOException
    {
        Loggers.Default.log( Level.INFO, format( "Writing headers for %s", filenamePrefix ) );
        String headers = fields.stream()
                .map( CsvField::value )
                .collect( Collectors.joining( formatting.delimiter().value() ) );

        Path headerFile = directory.resolve( format( "%s_headers.csv", filenamePrefix ) );
        Files.write( headerFile, headers.getBytes() );

        return headerFile;
    }
}
