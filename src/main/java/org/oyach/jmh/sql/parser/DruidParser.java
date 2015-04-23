package org.oyach.jmh.sql.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuzhenyuan
 * @version Last modified 15/3/4
 * @since 0.0.1
 *
 * alibab的druid项目sql解析
 */
public class DruidParser {
    private static final Map<String, SQLStatement> sqls = new HashMap<String, SQLStatement>();

    public static String select(String sql, String tableName){
        StringBuilder out = new StringBuilder();
        MySqlOutputVisitor visitor = new MySqlOutputVisitor(out);
        MySqlStatementParser parser = new MySqlStatementParser(sql);

        SQLStatement sqlStatement = sqls.get(sql);
        if (sqlStatement == null){
            sqlStatement = parser.parseStatement();
            sqls.put(sql, sqlStatement);
        }


        SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
        SQLSelect sqlSelect = sqlSelectStatement.getSelect();
        MySqlSelectQueryBlock sqlSelectQuery = (MySqlSelectQueryBlock) sqlSelect.getQuery();
//        SQLJoinTableSource sqlTableSource = (SQLJoinTableSource) sqlSelectQuery.getFrom();

//        String alias = sqlTableSource.getAlias();
        SQLExprTableSource sqlTableSourceLeft = (SQLExprTableSource) sqlSelectQuery.getFrom();
        SQLIdentifierExpr table = (SQLIdentifierExpr) sqlTableSourceLeft.getExpr();
//        String name = table.getName();

        table.setName(tableName);
        sqlStatement.accept(visitor);
//        String newSql = out.toString();
        return out.toString();
    }
}
