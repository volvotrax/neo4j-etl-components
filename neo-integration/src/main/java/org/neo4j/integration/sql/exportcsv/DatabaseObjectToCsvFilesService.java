package org.neo4j.integration.sql.exportcsv;

import org.neo4j.integration.neo4j.importcsv.config.ManifestEntry;
import org.neo4j.integration.neo4j.importcsv.io.HeaderFileWriter;
import org.neo4j.integration.sql.exportcsv.io.CsvFileWriter;

public interface DatabaseObjectToCsvFilesService
{
    ManifestEntry exportToCsv( DatabaseExportSqlSupplier sqlSupplier,
                               HeaderFileWriter headerFileWriter,
                               CsvFileWriter csvFileWriter,
                               ExportToCsvConfig config ) throws Exception;
}