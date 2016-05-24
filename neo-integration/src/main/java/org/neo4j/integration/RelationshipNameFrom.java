package org.neo4j.integration;

public enum RelationshipNameFrom
{
    TABLE_NAME, COLUMN_NAME;

    public static RelationshipNameFrom parse( String relationshipNameFrom )
    {
        if ( "column".equalsIgnoreCase( relationshipNameFrom ) )
        {
            return COLUMN_NAME;
        }
        return TABLE_NAME;
    }
}
