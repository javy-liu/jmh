package org.oyach.jmh.server;

import org.junit.Test;
import org.oyach.jmh.server.mysql.MysqlProxyServer;

import java.nio.charset.Charset;
import java.sql.*;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/21
 * @since 0.0.1
 */
public class ProxyServerTest {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3307/ceit";
//    static final String DB_URL = "jdbc:mysql://localhost:3306/mybatis";

    //  Database credentials
    static final String USER = "root";
    static final String PASS = "root";


    @Test
    public void test01() throws Exception {

        MysqlProxyServer mysqlProxyServer = new MysqlProxyServer();

        new Thread(mysqlProxyServer).start();

        Thread.sleep(1000 * 60 * 60);
    }


    @Test
    public void test02() throws Exception {


        Connection conn = null;
        Statement stmt = null;
        try{
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();

            String sql = "SELECT id, first, last, age FROM student";
            ResultSet rs = stmt.executeQuery(sql);
            //STEP 5: Extract data from result set
            while(rs.next()){
                //Retrieve by column name
                int id  = rs.getInt("id");
                int age = rs.getInt("age");
                String first = rs.getString("first");
                String last = rs.getString("last");

                //Display values
                System.out.print("ID: " + id);
                System.out.print(", Age: " + age);
                System.out.print(", First: " + first);
                System.out.println(", Last: " + last);
            }
            rs.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");

    }

    @Test
    public void test03() throws Exception {
        int max = 1048576;

        byte[] bytes = new byte[]{11,22,33,44};
        System.out.println((3 & 255) << 8);
        System.out.println((0 & 255) << 16);

        byte[] bytes1 = ByteArrayUtil.toBytes(78);

        System.out.println();
    }

    @Test
    public void test04() throws Exception {
        String head = "\n5.6.23\n";

        byte[] bytes = new byte[]{74, 0, 0, 0, 10, 53, 46, 54, 46, 50, 51, 0, 10, 0, 0, 0, 40, 55, 123, 41, 87, 91, 69,
                38, 0, -1, -9, 33, 2, 0, 127, -128, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 69, 90, 117, 96, 93, 53, 48,
                46, 53, 96, 102, 0, 109, 121, 115, 113, 108, 95, 110, 97, 116, 105, 118, 101, 95, 112, 97, 115,
                115, 119, 111, 114, 100, 0};

        String str = new String(bytes);

        byte[] bytes1 = new byte[]{0, 0, 0, 2, 0, 0, 0};
        String str2 =  new String(bytes1);
        System.out.println();
    }
}