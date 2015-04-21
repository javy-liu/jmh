package org.oyach.jmh.server;

import org.junit.Test;
import org.oyach.jmh.server.mysql.MysqlProxyServer;

import java.sql.*;

import static org.junit.Assert.*;

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
}