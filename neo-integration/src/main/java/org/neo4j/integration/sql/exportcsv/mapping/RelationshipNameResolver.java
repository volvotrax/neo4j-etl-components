package org.neo4j.integration.sql.exportcsv.mapping;

import org.neo4j.integration.RelationshipNameFrom;

public class RelationshipNameResolver
{
    private RelationshipNameFrom relationshipNameFrom;

    public RelationshipNameResolver( RelationshipNameFrom relationshipNameFrom )
    {
        this.relationshipNameFrom = relationshipNameFrom;
    }

    public String resolve( String tableName, String columnName )
    {
        if ( RelationshipNameFrom.COLUMN_NAME == relationshipNameFrom )
        {
            return columnName;
        }
        return tableName;
    }
}
