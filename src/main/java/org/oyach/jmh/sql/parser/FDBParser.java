package org.oyach.jmh.sql.parser;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.*;
import com.foundationdb.sql.unparser.NodeToString;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuzhenyuan
 * @version Last modified 15/3/4
 * @since 0.0.1
 *
 * foundationDB sql解析
 */
public class FDBParser {
    private static final Map<String, CursorNode> sqls = new HashMap<String, CursorNode>();

    public static String select(String sql, String tableName1) {
        CursorNode cursorNode;
        NodeToString nodeToStringForMybatis;
        String outSql = null;
        try {
            SQLParser parser = new SQLParser();

            cursorNode = sqls.get(sql);
            if (cursorNode == null){
                cursorNode = (CursorNode) parser.parseStatement(sql);
                sqls.put(sql, cursorNode);
            }

            SelectNode selectNode = (SelectNode) cursorNode.getResultSetNode();

            FromList fromList = selectNode.getFromList();

            for (int i = 0; i < fromList.size(); i++) {
                FromBaseTable fromBaseTable = (FromBaseTable) fromList.get(i);
                TableName tableName = fromBaseTable.getTableName();
                tableName.init(null, tableName1);
            }
            nodeToStringForMybatis = new NodeToString();
            outSql = nodeToStringForMybatis.toString(cursorNode);
        } catch (StandardException e) {
            e.printStackTrace();
        }
        return outSql;
    }
}
