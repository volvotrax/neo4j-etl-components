package org.neo4j.integration.sql.exportcsv.mysql.schema;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.neo4j.integration.io.AwaitHandle;
import org.neo4j.integration.sql.MySqlDatabaseClient;
import org.neo4j.integration.sql.QueryResults;
import org.neo4j.integration.sql.StubQueryResults;
import org.neo4j.integration.sql.exportcsv.ColumnUtil;
import org.neo4j.integration.sql.metadata.Column;
import org.neo4j.integration.sql.metadata.ColumnRole;
import org.neo4j.integration.sql.metadata.Join;
import org.neo4j.integration.sql.metadata.JoinTableInfo;
import org.neo4j.integration.sql.metadata.TableName;
import org.neo4j.integration.sql.metadata.TableNamePair;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JoinMetadataProducerTest
{
    private final ColumnUtil columnUtil = new ColumnUtil();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldReturnJoinMetadataForTwoWayRelationships() throws Exception
    {
        // given
        QueryResults results = StubQueryResults.builder()
                .columns( "SOURCE_TABLE_SCHEMA",
                        "SOURCE_TABLE_NAME",
                        "SOURCE_COLUMN_NAME",
                        "SOURCE_COLUMN_TYPE",
                        "TARGET_TABLE_SCHEMA",
                        "TARGET_TABLE_NAME",
                        "TARGET_COLUMN_NAME",
                        "TARGET_COLUMN_TYPE" )
                .addRow( "test", "Person", "id", "PrimaryKey", "test", "Person", "id", "PrimaryKey" )
                .addRow( "test", "Person", "addressId", "ForeignKey", "test", "Address", "id", "PrimaryKey" )
                .addRow( "test", "Address", "id", "PrimaryKey", "test", "Address", "id", "PrimaryKey" )
                .addRow( "test", "Address", "ownerId", "ForeignKey", "test", "Person", "id", "PrimaryKey" )
                .build();

        MySqlDatabaseClient databaseClient = mock( MySqlDatabaseClient.class );
        when( databaseClient.executeQuery( any( String.class ) ) ).thenReturn( AwaitHandle.forReturnValue( results ) );

        JoinMetadataProducer getJoinMetadata = new JoinMetadataProducer( databaseClient );

        // when
        TableName address = new TableName( "test.Address" );
        Collection<Join> joinCollection = getJoinMetadata
                .createMetadataFor( new TableNamePair(
                        new TableName( "test.Person" ),
                        address ) );

        // then
        ArrayList<Join> joins = new ArrayList<>( joinCollection );
        Join livesIn = joins.get( 1 );

        assertEquals( columnUtil.keyColumn( new TableName( "test.Person" ), "id", ColumnRole.PrimaryKey ), livesIn
                .keyOneSourceColumn() );

        assertEquals( columnUtil.keyColumn( new TableName( "test.Person" ), "addressId", ColumnRole.ForeignKey
        ), livesIn.keyTwoSourceColumn() );

        assertEquals( address, livesIn.keyTwoTargetColumn().table() );

        Join ownedBy = joins.get( 0 );

        assertEquals( columnUtil.keyColumn( address, "id", ColumnRole.PrimaryKey ), ownedBy.keyOneSourceColumn() );

        assertEquals( columnUtil.keyColumn( address, "ownerId", ColumnRole.ForeignKey ), ownedBy
                .keyTwoSourceColumn() );

        assertEquals( new TableName( "test.Person" ), ownedBy.keyTwoTargetColumn().table() );

        assertTrue( joins.size() == 2 );
    }

    @Test
    @Ignore
    public void shouldReturnJoinMetadataForCompositeWithPrimaryKey() throws Exception
    {
        // given
        QueryResults results = StubQueryResults.builder()
                .columns( "SOURCE_TABLE_SCHEMA",
                        "SOURCE_TABLE_NAME",
                        "SOURCE_COLUMN_NAME",
                        "SOURCE_COLUMN_TYPE",
                        "TARGET_TABLE_SCHEMA",
                        "TARGET_TABLE_NAME",
                        "TARGET_COLUMN_NAME",
                        "TARGET_COLUMN_TYPE" )
                .addRow( "test", "dbmirror_pendingdata", "SeqId", "ForeignKey", "test", "dbmirror_pending", "SeqId",
                        "PrimaryKey" )
                .addRow( "test", "dbmirror_pendingdata", "SeqId", "ForeignKey", "test", "dbmirror_pendingdata", "SeqId",
                        "ForeignKey" )
                .addRow( "test", "dbmirror_pendingdata", "IsKey", "PrimaryKey", "test", "dbmirror_pendingdata", "IsKey",
                        "PrimaryKey" )
                .build();

        MySqlDatabaseClient databaseClient = mock( MySqlDatabaseClient.class );
        when( databaseClient.executeQuery( any( String.class ) ) ).thenReturn( AwaitHandle.forReturnValue( results ) );

        JoinMetadataProducer getJoinMetadata = new JoinMetadataProducer( databaseClient );

        // when
        TableName book = new TableName( "test.Book" );
        TableName author = new TableName( "test.Author" );
        Collection<Join> joinCollection = getJoinMetadata.createMetadataFor(
                new TableNamePair( book, author ) );

        // then
        ArrayList<Join> joins = new ArrayList<>( joinCollection );
        Join writtenBy = joins.get( 0 );

        assertEquals( columnUtil.keyColumn( book, "id", ColumnRole.PrimaryKey ), writtenBy.keyOneSourceColumn() );

        assertEquals(
                columnUtil.compositeKeyColumn( book, asList( "author_first_name", "author_last_name" ), ColumnRole
                        .ForeignKey ),
                writtenBy.keyTwoSourceColumn() );

        assertEquals(
                columnUtil.compositeKeyColumn( author, asList( "first_name", "last_name" ), ColumnRole.PrimaryKey ),
                writtenBy.keyTwoTargetColumn() );

        assertEquals( author, writtenBy.keyTwoTargetColumn().table() );

        assertTrue( joins.size() == 1 );
    }

    @Test
    public void shouldReturnJoinMetadataForRelationshipThroughAJoinTable() throws Exception
    {
        // given
        QueryResults results = StubQueryResults.builder()
                .columns( "SOURCE_TABLE_SCHEMA",
                        "SOURCE_TABLE_NAME",
                        "SOURCE_COLUMN_NAME",
                        "SOURCE_COLUMN_TYPE",
                        "TARGET_TABLE_SCHEMA",
                        "TARGET_TABLE_NAME",
                        "TARGET_COLUMN_NAME",
                        "TARGET_COLUMN_TYPE" )
                .addRow( "test", "Student_Course", "studentId", "ForeignKey", "test", "Student", "id", "PrimaryKey" )
                .addRow( "test", "Student_Course", "courseId", "ForeignKey", "test", "Course", "id", "PrimaryKey" )
                .build();

        MySqlDatabaseClient databaseClient = mock( MySqlDatabaseClient.class );
        when( databaseClient.executeQuery( any( String.class ) ) ).thenReturn( AwaitHandle.forReturnValue( results ) );

        JoinMetadataProducer getJoinMetadata = new JoinMetadataProducer( databaseClient );

        // when
        TableName joinTableName = new TableName( "test.Student_Course" );
        Collection<Join> joinCollection = getJoinMetadata.createMetadataFor(
                new JoinTableInfo( joinTableName, new TableNamePair(
                        new TableName( "test.Course" ),
                        new TableName( "test.Student" ) ) ) );

        // then
        ArrayList<Join> joins = new ArrayList<>( joinCollection );
        assertJoinTableKeyMappings( joinTableName, joins.get( 0 ) );
    }

    @Test
    public void shouldReturnJoinMetadataRelationshipCompositeKey() throws Exception
    {
        // given
        QueryResults results = StubQueryResults.builder()
                .columns( "SOURCE_TABLE_SCHEMA",
                        "SOURCE_TABLE_NAME",
                        "SOURCE_COLUMN_NAME",
                        "SOURCE_COLUMN_TYPE",
                        "TARGET_TABLE_SCHEMA",
                        "TARGET_TABLE_NAME",
                        "TARGET_COLUMN_NAME",
                        "TARGET_COLUMN_TYPE" )
                .addRow( "test", "Book", "author_first_name", "ForeignKey", "test", "Author", "first_name",
                        "PrimaryKey" )
                .addRow( "test", "Book", "author_last_name", "ForeignKey", "test", "Author", "last_name", "PrimaryKey" )
                .addRow( "test", "Book", "id", "PrimaryKey", "test", "Book", "id", "PrimaryKey" )
                .build();

        MySqlDatabaseClient databaseClient = mock( MySqlDatabaseClient.class );
        when( databaseClient.executeQuery( any( String.class ) ) ).thenReturn( AwaitHandle.forReturnValue( results ) );

        JoinMetadataProducer getJoinMetadata = new JoinMetadataProducer( databaseClient );

        // when
        TableName book = new TableName( "test.Book" );
        TableName author = new TableName( "test.Author" );
        Collection<Join> joinCollection = getJoinMetadata.createMetadataFor(
                new TableNamePair( book, author ) );

        // then
        ArrayList<Join> joins = new ArrayList<>( joinCollection );
        Join writtenBy = joins.get( 0 );

        assertEquals( columnUtil.keyColumn( book, "id", ColumnRole.PrimaryKey ), writtenBy.keyOneSourceColumn() );

        assertEquals(
                columnUtil.compositeKeyColumn( book, asList( "author_first_name", "author_last_name" ), ColumnRole
                        .ForeignKey ),
                writtenBy.keyTwoSourceColumn() );

        assertEquals(
                columnUtil.compositeKeyColumn( author, asList( "first_name", "last_name" ), ColumnRole.PrimaryKey ),
                writtenBy.keyTwoTargetColumn() );

        assertEquals( author, writtenBy.keyTwoTargetColumn().table() );

        assertTrue( joins.size() == 1 );
    }

    @Test
    public void shouldThrowExceptionIfJoinDoesNotExistBetweenSuppliedTables() throws Exception
    {
        thrown.expect( IllegalStateException.class );
        thrown.expectMessage( "Unable to find 2 keys (found 1 primary key(s) and 0 foreign key(s))" );

        // given
        QueryResults results = StubQueryResults.builder()
                .columns( "SOURCE_TABLE_SCHEMA",
                        "SOURCE_TABLE_NAME",
                        "SOURCE_COLUMN_NAME",
                        "SOURCE_COLUMN_TYPE",
                        "TARGET_TABLE_SCHEMA",
                        "TARGET_TABLE_NAME",
                        "TARGET_COLUMN_NAME",
                        "TARGET_COLUMN_TYPE" )
                .addRow( "test", "Person", "id", "PrimaryKey", "test", "Person", "id", "PrimaryKey" )
                .build();

        MySqlDatabaseClient databaseClient = mock( MySqlDatabaseClient.class );
        when( databaseClient.executeQuery( any( String.class ) ) ).thenReturn( AwaitHandle.forReturnValue( results ) );

        JoinMetadataProducer getJoinMetadata = new JoinMetadataProducer( databaseClient );

        // when
        getJoinMetadata.createMetadataFor(
                new TableNamePair( new TableName( "test.Person" ), new TableName( "test.Course" ) ) );
    }

    private void assertJoinTableKeyMappings( TableName studentCourse, Join join )
    {
        Column expectedStudentId = columnUtil.keyColumn( studentCourse, "studentId", ColumnRole.ForeignKey );
        Column expectedCourseId = columnUtil.keyColumn( studentCourse, "courseId", ColumnRole.ForeignKey );

        assertEquals( expectedCourseId, join.keyOneSourceColumn() );

        assertEquals( columnUtil.keyColumn( new TableName( "test.Course" ), "id", ColumnRole.PrimaryKey ),
                join.keyOneTargetColumn() );

        assertEquals( expectedStudentId, join.keyTwoSourceColumn() );

        assertEquals( columnUtil.keyColumn( new TableName( "test.Student" ), "id", ColumnRole.PrimaryKey ),
                join.keyTwoTargetColumn() );

    }
}
