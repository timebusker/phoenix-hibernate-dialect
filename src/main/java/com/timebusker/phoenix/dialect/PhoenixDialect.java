package com.timebusker.phoenix.dialect;

import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.MappingException;
import org.hibernate.boot.Metadata;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.pagination.AbstractLimitHandler;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitHelper;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.invesdwin.instrument.DynamicInstrumentationLoader;

/**
 *
 */
public class PhoenixDialect extends Dialect {

    public static final String HINT_SECONDARY_INDEX = "phoenix.secondary.index";

    public static class SecondaryIndexHint {
        private final String table;
        private final String index;

        public SecondaryIndexHint(String table, String index) {
            this.table = table;
            this.index = index;
        }

        public SecondaryIndexHint(Class<?> annotated, String index) {
            javax.persistence.Table table = (javax.persistence.Table) annotated.getAnnotation(javax.persistence.Table.class);
            String name = table.name();
            String schema = table.schema();
            this.table = (schema != null ? schema + "." : "") + name;
            this.index = index;
        }

        public String build() {
            return "/*+ INDEX(" + table + " " + index + ") */";
        }
    }

    private static ClassPathXmlApplicationContext ctx;

    static {
        register();
    }

    public static synchronized void register() {
        if (ctx == null) {
            DynamicInstrumentationLoader.waitForInitialized();
            DynamicInstrumentationLoader.initLoadTimeWeavingContext();
            ctx = new ClassPathXmlApplicationContext("/META-INF/phoenix-spring-context.xml");
        }
    }

    public PhoenixDialect() {
        super();
        // Phoenix datetypes (https://phoenix.apache.org/language/datatypes.html)
        registerColumnType(Types.BIT, "boolean");
        registerColumnType(Types.BIGINT, "bigint");
        registerColumnType(Types.SMALLINT, "smallint");
        registerColumnType(Types.TINYINT, "tinyint");
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType(Types.FLOAT, "float");
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType(Types.NUMERIC, "decimal($p,$s)");
        registerColumnType(Types.DECIMAL, "decimal($p,$s)");
        registerColumnType(Types.DATE, "date");
        registerColumnType(Types.TIME, "time");
        registerColumnType(Types.TIMESTAMP, "timestamp");
        registerColumnType(Types.BOOLEAN, "boolean");
        registerColumnType(Types.VARCHAR, 255, "varchar($l)");
        registerColumnType(Types.CHAR, "char(1)");
        registerColumnType(Types.BINARY, "binary($l)");
        registerColumnType(Types.VARBINARY, "varbinary");
        registerColumnType(Types.ARRAY, "array[$l]");

        // Phoenix functions (https://phoenix.apache.org/language/functions.html)
        registerFunction("percentile_cont_asc", new SQLFunctionTemplate(StandardBasicTypes.DOUBLE, "PERCENTILE_CONT (?1) WITHIN GROUP (ORDER BY ?2 ASC)"));
        registerFunction("percentile_cont_desc", new SQLFunctionTemplate(StandardBasicTypes.DOUBLE, "PERCENTILE_CONT (?1) WITHIN GROUP (ORDER BY ?2 DESC)"));
        registerFunction("percentile_disc_asc", new SQLFunctionTemplate(StandardBasicTypes.DOUBLE, "PERCENTILE_DISC (?1) WITHIN GROUP (ORDER BY ?2 ASC)"));
        registerFunction("percentile_disc_desc", new SQLFunctionTemplate(StandardBasicTypes.DOUBLE, "PERCENTILE_DISC (?1) WITHIN GROUP (ORDER BY ?2 DESC)"));
        registerFunction("percent_rank_asc", new SQLFunctionTemplate(StandardBasicTypes.DOUBLE, "PERCENT_RANK (?1) WITHIN GROUP (ORDER BY ?2 ASC)"));
        registerFunction("percent_rank_desc", new SQLFunctionTemplate(StandardBasicTypes.DOUBLE, "PERCENT_RANK (?1) WITHIN GROUP (ORDER BY ?2 DESC)"));
        registerFunction("stddev_pop", new StandardSQLFunction("stddev_pop", StandardBasicTypes.DOUBLE));
        registerFunction("stddev_samp", new StandardSQLFunction("stddev_samp", StandardBasicTypes.DOUBLE));

        registerFunction("upper", new StandardSQLFunction("upper", StandardBasicTypes.STRING));
        registerFunction("lower", new StandardSQLFunction("lower", StandardBasicTypes.STRING));
        registerFunction("reverse", new StandardSQLFunction("reverse", StandardBasicTypes.STRING));
        registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
        registerFunction("instr", new StandardSQLFunction("instr", StandardBasicTypes.INTEGER));
        registerFunction("trim", new StandardSQLFunction("trim", StandardBasicTypes.STRING));
        registerFunction("ltrim", new StandardSQLFunction("ltrim", StandardBasicTypes.STRING));
        registerFunction("rtrim", new StandardSQLFunction("rtrim", StandardBasicTypes.STRING));
        registerFunction("lpad", new StandardSQLFunction("lpad", StandardBasicTypes.STRING));
        registerFunction("length", new StandardSQLFunction("length", StandardBasicTypes.INTEGER));
        registerFunction("regexp_substr", new StandardSQLFunction("regexp_substr", StandardBasicTypes.STRING));
        registerFunction("regexp_replace", new StandardSQLFunction("regexp_replace", StandardBasicTypes.STRING));
        registerFunction("to_char", new StandardSQLFunction("to_char", StandardBasicTypes.STRING));

        registerFunction("to_date", new StandardSQLFunction("to_date", StandardBasicTypes.DATE));
        registerFunction("to_time", new StandardSQLFunction("to_time", StandardBasicTypes.TIME));
        registerFunction("to_timestamp", new StandardSQLFunction("to_timestamp", StandardBasicTypes.TIMESTAMP));
        registerFunction("current_date", new NoArgSQLFunction("current_date", StandardBasicTypes.TIMESTAMP));
        registerFunction("current_time", new NoArgSQLFunction("current_time", StandardBasicTypes.TIME));
        registerFunction("convert_tz", new StandardSQLFunction("convert_tz", StandardBasicTypes.TIMESTAMP));
        registerFunction("timezone_offset", new StandardSQLFunction("timezone_offset", StandardBasicTypes.INTEGER));
        registerFunction("now", new NoArgSQLFunction("now", StandardBasicTypes.TIMESTAMP));
        registerFunction("year", new StandardSQLFunction("year", StandardBasicTypes.INTEGER));
        registerFunction("month", new StandardSQLFunction("month", StandardBasicTypes.INTEGER));
        registerFunction("day", new StandardSQLFunction("dayofmonth", StandardBasicTypes.INTEGER));
        registerFunction("week", new StandardSQLFunction("week", StandardBasicTypes.INTEGER));
        registerFunction("dayofyear", new StandardSQLFunction("dayofyear", StandardBasicTypes.INTEGER));
        registerFunction("dayofmonth", new StandardSQLFunction("dayofmonth", StandardBasicTypes.INTEGER));
        registerFunction("dayofweek", new StandardSQLFunction("dayofweek", StandardBasicTypes.INTEGER));
        registerFunction("hour", new StandardSQLFunction("hour", StandardBasicTypes.INTEGER));
        registerFunction("minute", new StandardSQLFunction("minute", StandardBasicTypes.INTEGER));
        registerFunction("second", new StandardSQLFunction("second", StandardBasicTypes.INTEGER));

        registerFunction("md5", new StandardSQLFunction("md5", StandardBasicTypes.BINARY));

        registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
        registerFunction("abs", new StandardSQLFunction("abs", StandardBasicTypes.DOUBLE));
        registerFunction("sqrt", new StandardSQLFunction("sqrt", StandardBasicTypes.DOUBLE));
        registerFunction("cbrt", new StandardSQLFunction("cbrt", StandardBasicTypes.DOUBLE));
        registerFunction("exp", new StandardSQLFunction("exp", StandardBasicTypes.DOUBLE));
        registerFunction("power", new StandardSQLFunction("power", StandardBasicTypes.DOUBLE));
        registerFunction("ln", new StandardSQLFunction("ln", StandardBasicTypes.DOUBLE));
        registerFunction("log", new StandardSQLFunction("log", StandardBasicTypes.DOUBLE));
        registerFunction("round", new StandardSQLFunction("round", StandardBasicTypes.DOUBLE));
        registerFunction("ceil", new StandardSQLFunction("ceil", StandardBasicTypes.DOUBLE));
        registerFunction("floor", new StandardSQLFunction("floor", StandardBasicTypes.DOUBLE));
        registerFunction("to_number", new StandardSQLFunction("to_number", StandardBasicTypes.DOUBLE));
        registerFunction("rand", new NoArgSQLFunction("rand", StandardBasicTypes.DOUBLE));
    }

    // SEQUENCE support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    public boolean supportsPooledSequences() {
        return true;
    }

    @Override
    public String getSequenceNextValString(String sequenceName) throws MappingException {
        return "SELECT " + getSelectSequenceNextValString(sequenceName);
    }

    @Override
    public String getSelectSequenceNextValString(String sequenceName) throws MappingException {
        return "NEXT VALUE FOR " + sequenceName;
    }

    @Override
    protected String getCreateSequenceString(String sequenceName) throws MappingException {
        return getCreateSequenceString(sequenceName, 1, 1);
    }

    @Override
    protected String getCreateSequenceString(String sequenceName, int initialValue, int incrementSize) throws MappingException {
        return "CREATE SEQUENCE " + sequenceName + " START WITH " + initialValue + " INCREMENT BY " + incrementSize;
    }

    @Override
    protected String getDropSequenceString(String sequenceName) throws MappingException {
        return "DROP SEQUENCE " + sequenceName;
    }

    // lock acquisition support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public boolean supportsLockTimeouts() {
        return false;
    }

    @Override
    public boolean supportsOuterJoinForUpdate() {
        return false;
    }


    // limit/offset support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public LimitHandler getLimitHandler() {
        return new AbstractLimitHandler() {
            @Override
            public boolean supportsLimit() {
                return true;
            }

            @Override
            public String processSql(String sql, RowSelection selection) {
                if (LimitHelper.useLimit(this, selection)) {
                    final boolean hasMaxRows = LimitHelper.hasMaxRows(selection);
                    final boolean hasOffset = LimitHelper.hasFirstRow(selection);
                    return sql + (hasMaxRows ? " limit ?" : "") + (hasOffset ? " offset ?" : "");
                }
                return sql;
            }
        };
    }


    // current timestamp support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public boolean supportsCurrentTimestampSelection() {
        return true;
    }

    @Override
    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }

    @Override
    public String getCurrentTimestampSelectString() {
        return "select current_date()";
    }

    @Override
    public String getCurrentTimestampSQLFunctionName() {
        return "current_date";
    }


    // union subclass support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public boolean supportsUnionAll() {
        return true;
    }


    // DDL support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    @Override
    public String[] getDropSchemaCommand(String schemaName) {
        return new String[]{"drop schema if exists " + schemaName};
    }

    @Override
    public String getAddColumnString() {
        return " add ";
    }

    @Override
    public boolean supportsColumnCheck() {
        return false;
    }

    @Override
    public boolean supportsTableCheck() {
        return false;
    }

    @Override
    public boolean supportsEmptyInList() {
        return false;
    }

    @Override
    public boolean supportsRowValueConstructorSyntax() {
        return true;
    }

    @Override
    public boolean supportsRowValueConstructorSyntaxInInList() {
        return true;
    }

    @Override
    public boolean supportsBindAsCallableArgument() {
        return false;
    }

    @Override
    public boolean supportsTupleDistinctCounts() {
        return false;
    }

    @Override
    public String getQueryHintString(String query, List<String> hints) {
        return QueryUtils.removeQueryComments(query).trim().replaceFirst("select", "select " + StringUtils.join(hints, " ") + " ");
    }

    @Override
    public NameQualifierSupport getNameQualifierSupport() {
        return NameQualifierSupport.SCHEMA;
    }

    @Override
    public boolean hasAlterTable() {
        return false;
    }

    @Override
    public boolean dropConstraints() {
        return false;
    }

    @Override
    public UniqueDelegate getUniqueDelegate() {
        return new UniqueDelegate() {
            @Override
            public String getTableCreationUniqueConstraintsFragment(Table table) {
                return "";
            }

            @Override
            public String getColumnDefinitionUniquenessFragment(Column column) {
                return "";
            }

            @Override
            public String getAlterTableToDropUniqueKeyCommand(UniqueKey uniqueKey, Metadata metadata) {
                final JdbcEnvironment jdbcEnvironment = metadata.getDatabase().getJdbcEnvironment();
                final String tableName = jdbcEnvironment.getQualifiedObjectNameFormatter().format(uniqueKey.getTable().getQualifiedTableName(), PhoenixDialect.this);
                final String constraintName = PhoenixDialect.this.quote(uniqueKey.getName());
                return "drop index if exists " + constraintName + " on " + tableName;
            }

            @Override
            public String getAlterTableToAddUniqueKeyCommand(UniqueKey uniqueKey, Metadata metadata) {
                final JdbcEnvironment jdbcEnvironment = metadata.getDatabase().getJdbcEnvironment();
                final String tableName = jdbcEnvironment.getQualifiedObjectNameFormatter().format(uniqueKey.getTable().getQualifiedTableName(), PhoenixDialect.this);
                final String constraintName = PhoenixDialect.this.quote(uniqueKey.getName());
                final StringBuilder columns = new StringBuilder();
                final Iterator<org.hibernate.mapping.Column> columnIterator = uniqueKey.columnIterator();
                while (columnIterator.hasNext()) {
                    final org.hibernate.mapping.Column column = columnIterator.next();
                    columns.append(column.getQuotedName(PhoenixDialect.this));
                    if (uniqueKey.getColumnOrderMap().containsKey(column)) {
                        columns.append(" ").append(uniqueKey.getColumnOrderMap().get(column));
                    }
                    if (columnIterator.hasNext()) {
                        columns.append(", ");
                    }
                }
                return "create index " + constraintName + " on " + tableName + " (" + columns.toString() + ")";
            }
        };
    }
}
