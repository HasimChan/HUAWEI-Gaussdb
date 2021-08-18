package com.hasim;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * 现存问题：
 * 1. 数据库持久化采用什么格式的文件？json、Excel、txt？
 * 初步采用2种文件，一种定义数据库表规格，另一种负责存储数据
 * 2. 加载数据采用什么数据结构以提高查找效率？B+树
 * 3. 查找方式如何实现
 */
public class Gaussdb {
    private static Type type;

    public static void main(String[] args) {
        type = Type.CREATE_TABLE;
//        String input;
//
//        if (args.length != 0)
//            input = args[0];
//        else
//            throw new RuntimeException("请输入有效命令！");
        String s = sqlAnalysis("G:\\Project\\Java\\Study\\case.sql");
        System.out.println(s);

    }

    /**
     * 读取sql文件，返回sql语句
     *
     * @param path
     * @return
     */
    private static String sqlAnalysis(String path) {
        BufferedReader reader = null;
        StringBuilder sb = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * 从持久化数据库表中将数据载入内存
     *
     * @param tableName
     */
    public void loadTable(String tableName) {

    }

    /**
     * 新建数据库表
     *
     * @param tableName
     * @param field
     */
    private void createTable(String tableName, Map<String, Boolean> field) {

    }

    /**
     * 插入数据库表
     *
     * @param tableName
     * @param values
     */
    private void insert(String tableName, Object[] values) {

    }

    /**
     * 查找数据库表
     *
     * @param tableName
     * @param columns
     * @param condition
     */
    private void select(String tableName, String[] columns, Map<String, String> condition) {

    }
}

/**
 * sql命令类型枚举
 */
enum Type {
    CREATE_TABLE,
    INSERT,
    SELECT
}
