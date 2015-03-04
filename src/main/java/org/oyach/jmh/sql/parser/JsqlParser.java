package org.oyach.jmh.sql.parser;

import com.foundationdb.sql.parser.CursorNode;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.SelectUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuzhenyuan
 * @version Last modified 15/3/4
 * @since 0.0.1
 */
public class JsqlParser {
    private static final Map<String, Statement> sqls = new HashMap<String, Statement>();

    public static String select(String sql, String tableName){
        try {
            Statement statement = sqls.get(sql);
            if (statement == null){
                statement = CCJSqlParserUtil.parse(sql);
                sqls.put(sql, statement);
            }


            Select selectStatement = (Select) statement;
            PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();

            Table table = (Table) plainSelect.getFromItem();
            table.setName(tableName);


            return selectStatement.toString();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }
}
