package org.oyach.jmh.sql.parser;

import org.junit.Test;

import static org.junit.Assert.*;

public class FDBParserTest {

    private static final String select = "select" +
            "\t\t\tid,wm_order_id,wm_food_id,food_price,unit,count,box_num,box_price,food_name,order_time,origin_food_price\n" +
            "\t\tfrom \n" +
            "\t\t\twm_order_detail\n" +
            "\t\twhere\n" +
            "			wm_order_id in (?,?,?) ORDER BY id DESC limit 0,100";

    @Test
    public void testSelect() throws Exception {
        String sql = FDBParser.select(select, "user");
        System.out.println(sql);
    }
}