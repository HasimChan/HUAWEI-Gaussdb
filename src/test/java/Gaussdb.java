import java.io.*;
import java.util.*;

/**
 * 非持久化版
 */
public class Gaussdb {

    private static HashMap<String, Table> tables = new HashMap<>();

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        StringBuilder builder = new StringBuilder();
        while (scan.hasNext()) {
            builder.append(scan.next() + " ");
        }

        String[] sqls = builder.toString().trim().split(";");
        for (String sql : sqls) {
            SqlParser sqlParser = new SqlParser();
            String subSql = sqlParser.sqlParse(sql);

            switch (sqlParser.getSqlType()) {
                case CREATE_TABLE:
                    HashMap<String, int[]> fieldsMap = sqlParser.createTableParse(subSql);
                    createTable(sqlParser.getTableName()[0], fieldsMap);
                    break;
                case CREATE_INDEX:
                    String fieldsName = sqlParser.createIndexParse(subSql);
                    createIndex(sqlParser.getTableName()[0], fieldsName);
                    break;
                case INSERT:
                    String[] contents = sqlParser.insertParse(subSql);
                    insert(sqlParser.getTableName()[0], contents);
                    break;
                case SELECT:
                    sqlParser.selectParse(subSql);
                    switch (sqlParser.getSqlType()) {
                        case SINGLE_TABLE_SELECT_ALL:
                            selectAll(sqlParser.getTableName()[0]);
                            break;
                    }
                    break;
//                    int[] targetIndex = sqlParser.selectParse(subSql);
//                    switch (sqlParser.getSqlType()) {
//                        case SINGLE_TABLE_SELECT_ALL:
//                            selectAll(sqlParser.getTableName()[0]);
//                            break;
//                        case SINGLE_TABLE_SELECT:
//                            singleTableSelect(sqlParser.getTableName()[0], targetIndex,
//                                    sqlParser.getOrEqualsKey(), sqlParser.getOrEqualsVal(),
//                                    sqlParser.getAndEqualsKey(), sqlParser.getAndEqualsVal(),
//                                    sqlParser.getOrNotEqualsKey(), sqlParser.getOrNotEqualsVal(),
//                                    sqlParser.getAndNotEqualsKey(), sqlParser.getAndNotEqualsVal());
//                            break;
//                        case SINGLE_TABLE_SELECT_INDEX:
//                            singleTableSelectIndex(sqlParser.getTableName()[0], targetIndex,
//                                    sqlParser.getOrEqualsKey()[0], sqlParser.getOrEqualsVal());
//                            break;
//                    }
//                    break;
            }
        }
    }

    private static void createTable(String tableName, Map<String, int[]> fieldsMap) {
        Table table = new Table(fieldsMap);
        tables.put(tableName, table);
        System.out.println("CREATE TABLE");
    }

    private static void createIndex(String tableName, String fieldName) {
        Table table = tables.get(tableName);
        table.setTableIndex(fieldName);
        System.out.println("CREATE INDEX");
    }

    /**
     * 插入数据库表
     *
     * @param tableName
     * @param contents
     */
    private static void insert(String tableName, String[] contents) {
        Table table = tables.get(tableName);
        table.insert(contents);
        System.out.println("INSERT 1");
    }

    private static void selectAll(String tableName) {
        Table table = tables.get(tableName);
        System.out.println(table.selectAll());
    }

//    private static void singleTableSelect(String tableName, int[] targetIndex,
//                                          Integer[] orEqualsKey, String[] orEqualsVal,
//                                          Integer[] andEqualsKey, String[] andEqualsVal,
//                                          Integer[] orNotEqualsKey, String[] orNotEqualsVal,
//                                          Integer[] andNotEqualsKey, String[] andNotEqualsVal) {
//        TableData tableData = new TableData();
//        String[][] table = tableData.getTableData(tableName);
//        StringBuilder builder = new StringBuilder();
//
//        for (String[] row : table) {
//            boolean isTargetRow = false;
//            if (orEqualsKey != null) {
//                int len = orEqualsKey.length;
//                for (int i = 0; i < len; i++) {
//                    if (row[orEqualsKey[i]].equals(orEqualsVal[i])) {
//                        builder.append(getResultStr(row, targetIndex));
//                        isTargetRow = true;
//                        break;
//                    }
//                }
//            }
//            if (isTargetRow)
//                continue;
//
//            if (orNotEqualsKey != null) {
//                int len = orNotEqualsKey.length;
//                for (int i = 0; i < len; i++) {
//                    if (!row[orNotEqualsKey[i]].equals(orNotEqualsVal[i])) {
//                        builder.append(getResultStr(row, targetIndex));
//                        isTargetRow = true;
//                        break;
//                    }
//                }
//            }
//            if (isTargetRow)
//                continue;
//
//            if (andEqualsKey != null) {
//                int len = andEqualsKey.length;
//                int count = 0;
//                for (int i = 0; i < len; i++) {
//                    if (row[andEqualsKey[i]].equals(andEqualsVal[i])) {
//                        count++;
//                    } else {
//                        break;
//                    }
//                }
//                if (count != len)
//                    continue;
//                if (andNotEqualsKey == null)
//                    builder.append(getResultStr(row, targetIndex));
//            }
//
//            if (andNotEqualsKey != null) {
//                int len = andNotEqualsKey.length;
//                int count = 0;
//                for (int i = 0; i < len; i++) {
//                    if (!row[andNotEqualsKey[i]].equals(andNotEqualsVal[i])) {
//                        count++;
//                    }
//                }
//                if (count != len)
//                    continue;
//                builder.append(getResultStr(row, targetIndex));
//            }
//        }
//        System.out.println(builder);
//    }

    private static String getResultStr(String[] row, int[] targetIndex) {
        StringBuilder builder = new StringBuilder();
        int len = targetIndex.length;
        for (int i = 0; i < len; i++) {
            if (i == len - 1)
                builder.append(row[i] + "\n");
            else
                builder.append(row[i] + "|");
        }
        return builder.toString();
    }

//    private static void singleTableSelectIndex(String tableName, int[] targetIndex,
//                                               int index, String[] orEqualsVal) {
//        TableData tableData = new TableData();
//        Map<String, String[]> table = tableData.getTableDataIndex(tableName, index);
//        StringBuilder builder = new StringBuilder();
//        for (String val : orEqualsVal) {
//            String[] target = table.get(val);
//            if (target != null)
//                builder.append(getResultStr(target, targetIndex));
//        }
//        System.out.println(builder);
//    }
}

/**
 * sql命令类型枚举
 */
enum SqlType {
    CREATE_TABLE,
    CREATE_INDEX,
    INSERT,
    SELECT,
    SINGLE_TABLE_SELECT,
    SINGLE_TABLE_SELECT_INDEX,
    SINGLE_TABLE_SELECT_ALL,
    TWO_TABLE_SELECT
}

/**
 * 考虑建立索引后的插入
 */
class Table {
    private int tableIndex = -1;
    private HashMap<String, int[]> tableFields; // 0: type 1: index
    private boolean[] typeArr;
    private ArrayList<String[]> tableDataArray;
    private HashMap<String, String[]> tableDataMap;

    public Table(Map<String, int[]> fieldsMap) {
        this.tableFields = (HashMap<String, int[]>) fieldsMap;
        this.typeArr = getTypeArr();
        tableDataArray = new ArrayList<>();
    }

    public boolean isIndex() {
        return tableIndex != -1;
    }

    public void setTableIndex(String fieldName) {
        int index = tableFields.get(fieldName)[1];
        this.tableIndex = index;
        if (tableDataArray.size() != 0) {
            makeMap();
        } else {
            tableDataMap = new HashMap<>();
        }
    }

    public void insert(String[] contents) {
        int len = contents.length;
        String[] row = new String[len];

        for (int i = 0; i < len; i++) {
            if (typeArr[i]) {
                row[i] = contents[i];
            } else {
                row[i] = contents[i].substring(1, contents[i].length() - 1);
            }
        }
        if (isIndex()) {
            tableDataMap.put(row[tableIndex], row);
        } else {
            tableDataArray.add(row);
        }
    }

    public String selectAll() {
        StringBuilder builder = new StringBuilder();
        if (isIndex()) {
            for (Map.Entry<String, String[]> entry : tableDataMap.entrySet()) {
                String[] row = entry.getValue();
                String result = printDeal(row, null, true);
                builder.append(result);
            }
        } else {
            Iterator<String[]> iterator = tableDataArray.iterator();
            while (iterator.hasNext()) {
                String[] row = iterator.next();
                String result = printDeal(row, null, true);
                builder.append(result);
            }
        }
        return builder.toString();
    }

    private void makeMap() {
        tableDataMap = new HashMap<>();
        int index = tableIndex;
        for (String[] row : tableDataArray) {
            tableDataMap.put(row[index], row);
        }
    }

    private boolean[] getTypeArr() {
        int len = tableFields.size();
        boolean[] typeArr = new boolean[len];

        for (Map.Entry<String, int[]> entry : tableFields.entrySet()) {
            int[] val = entry.getValue();
            typeArr[val[1]] = (val[0] == 1);
        }
        return typeArr;
    }

    private String printDeal(String[] row, int[] index, boolean isAll) {
        if (isAll) {
            int len = row.length;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < len; i++) {
                if (i == len - 1)
                    builder.append(row[i] + "\n");
                else
                    builder.append(row[i] + "|");
            }
            return builder.toString();
        } else {
            int len = index.length;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < len; i++) {
                if (i == len - 1)
                    builder.append(row[index[i]] + "\n");
                else
                    builder.append(row[index[i]] + "|");
            }
            return builder.toString();
        }
    }
}

/**
 * sql 解析器
 */
class SqlParser {
    private SqlType sqlType;
    private String[] tableName;
    private Integer[] orEqualsKey;
    private String[] orEqualsVal;
    private Integer[] andEqualsKey;
    private String[] andEqualsVal;
    private Integer[] orNotEqualsKey;
    private String[] orNotEqualsVal;
    private Integer[] andNotEqualsKey;
    private String[] andNotEqualsVal;

    public SqlType getSqlType() {
        return sqlType;
    }

    public String[] getTableName() {
        return tableName;
    }

    public Integer[] getOrEqualsKey() {
        return orEqualsKey.length == 0 ? null : orEqualsKey;
    }

    public String[] getOrEqualsVal() {
        return orEqualsVal.length == 0 ? null : orEqualsVal;
    }

    public Integer[] getAndEqualsKey() {
        return andEqualsKey.length == 0 ? null : andEqualsKey;
    }

    public String[] getAndEqualsVal() {
        return andEqualsVal.length == 0 ? null : andEqualsVal;
    }

    public Integer[] getOrNotEqualsKey() {
        return orNotEqualsKey.length == 0 ? null : orNotEqualsKey;
    }

    public String[] getOrNotEqualsVal() {
        return orNotEqualsVal.length == 0 ? null : orNotEqualsVal;
    }

    public Integer[] getAndNotEqualsKey() {
        return andNotEqualsKey.length == 0 ? null : andNotEqualsKey;
    }

    public String[] getAndNotEqualsVal() {
        return andNotEqualsVal.length == 0 ? null : andNotEqualsVal;
    }

    public String sqlParse(String sql) {
        sql = sql.trim();
        String type = sql.substring(0, 6);
        if ("create".equalsIgnoreCase(type)) {
            String subSql = sql.substring(6).trim();
            type = subSql.substring(0, 5);
            if ("table".equalsIgnoreCase(type)) {
                sqlType = SqlType.CREATE_TABLE;
                return subSql.substring(5);
            } else if ("index".equalsIgnoreCase(type)) {
                sqlType = SqlType.CREATE_INDEX;
                return subSql.substring(5);
            } else {
                throw new RuntimeException("sql maybe error");
            }
        } else if ("insert".equalsIgnoreCase(type)) {
            sqlType = SqlType.INSERT;
            return sql.substring(6);
        } else if ("select".equalsIgnoreCase(type)) {
            sqlType = SqlType.SELECT;
            return sql.substring(6);
        } else {
            throw new RuntimeException("sql maybe error");
        }
    }

    public HashMap<String, int[]> createTableParse(String subSql) {
        subSql = subSql.trim();
        // 获取内容
        String fields = subSql.substring(subSql.indexOf("(") + 1, subSql.indexOf(")"));

        String[] fieldStrs = fields.split(",");
        HashMap<String, int[]> fieldsMap = new HashMap<>();

        int len = fieldStrs.length;
        for (int i = 0; i < len; i++) {
            String field = fieldStrs[i].trim();
            String[] kv = field.split("\\s+"); // 分割键值对

            int[] val = new int[2];
            if (kv.length != 2) {
                throw new RuntimeException("sql maybe error");
            }

            if (kv[1].equalsIgnoreCase("int")) {
                val[0] = 1;
            } else if (kv[1].equalsIgnoreCase("text")) {
                val[0] = 0;
            } else {
                throw new RuntimeException("sql maybe error");
            }
            val[1] = i;
            fieldsMap.put(kv[0], val);
        }

        tableName = new String[]{subSql.substring(0, subSql.indexOf(" "))};
        return fieldsMap;
    }

    public String createIndexParse(String subSql) {
        subSql = subSql.trim();
        // 获取内容
        String fieldName = subSql.substring(subSql.indexOf("(") + 1, subSql.indexOf(")")).trim();
        tableName = new String[]{subSql.substring(0, subSql.indexOf("(")).split("\\s+")[2]}; // 分割键值对
        return fieldName;
    }

    public String[] insertParse(String subSql) {
        subSql = subSql.trim();
        String[] temp = subSql.split("\\s+");
        tableName = new String[]{temp[1]};

        String[] contents = subSql.substring(subSql.indexOf("(") + 1, subSql.indexOf(")")).split(","); // 插入内容

        return arrayTrim(contents);
    }

    public int[] selectParse(String subSql) {
        subSql = subSql.trim();

        if (subSql.charAt(0) == '*') { // 查询所有
            sqlType = SqlType.SINGLE_TABLE_SELECT_ALL;
            String[] sql = subSql.split("\\s+");
            tableName = new String[]{sql[sql.length - 1]};
            return null;
        } else {
//            String[] contents = splitIgnoreCase(subSql, "from");
//            if (contents.length == 0)
//                throw new RuntimeException("SQL syntax error");
//
//            String targetFieldsStr = contents[0].trim();
//
//            contents = splitIgnoreCase(contents[1], "where");
//            if (contents.length == 0)
//                throw new RuntimeException("SQL syntax error");
//
//            String targetTablesStr = contents[0].trim();
//            String selectConditionStr = contents[1].trim();
//            tableName = targetTablesParse(targetTablesStr);
//
//            if (tableName.length == 1) { // 单表查询
//                sqlType = SqlType.SINGLE_TABLE_SELECT;
//                TableFields tableFields = new TableFields(FileUtils.readFile(tableName[0] + ".type", true));
//                // 目标字段索引数组
//                int[] targetFieldsIndex = singleTargetFieldsParse(targetFieldsStr, tableFields);
//                singleSelectConditionParse(selectConditionStr, tableFields);
//
//                int tableIndex = tableFields.getTableIndex();
//                if (tableFields.getTableIndex() != -1) {
//
//                    if (getOrEqualsKey() != null && getOrNotEqualsKey() == null && getAndEqualsKey() == null && getAndNotEqualsKey() == null) {
//                        int count = 0;
//                        for (int key : getOrEqualsKey()) {
//                            if (key == tableIndex)
//                                count++;
//                            else
//                                break;
//                        }
//                        if (getOrEqualsKey().length == count) {
//                            sqlType = SqlType.SINGLE_TABLE_SELECT_INDEX;
//                        }
//                    }
//                }
//                return targetFieldsIndex;
//            } else {  // 两表查询
//
//            }
        }
        return null;
    }

    /*

    private int[] singleTargetFieldsParse(String targetFieldsStr, TableFields tableFields) {
        // tableName - Fields - HashMap
        String[] targetFields = targetFieldsStr.split(",");
        int len = targetFields.length;
        int[] indexs = new int[len];
        for (int i = 0; i < len; i++) {
            String targetField = targetFields[i].trim();
            int index = tableFields.indexOf(targetField);
            if (index == -1)
                throw new RuntimeException("SQL syntax error");
            else
                indexs[i] = index;
        }
        return indexs;
    }

    private String[] targetTablesParse(String targetTablesStr) {
        String[] tables = targetTablesStr.split(",");
        return arrayTrim(tables);
    }

    private void singleSelectConditionParse(String selectConditionStr, TableFields tableFields) {
        String[] contents = splitIgnoreCase(selectConditionStr, "or");
        ArrayList<String> orConditions = new ArrayList<>();
        ArrayList<String> andConditions = new ArrayList<>();
        for (String content : contents) {
            String[] andContents = splitIgnoreCase(content, "and");
            if (andContents.length == 1) {
                orConditions.add(content.trim());
            } else {
                for (String andContent : andContents) {
                    andConditions.add(andContent.trim());
                }
            }
        }
        equalsConditionParse(orConditions.toArray(new String[orConditions.size()]), tableFields, true);
        equalsConditionParse(andConditions.toArray(new String[andConditions.size()]), tableFields, false);
    }

    private void equalsConditionParse(String[] conditionStrs, TableFields tableFields, boolean isOr) {
        ArrayList<Integer> equalsKey = new ArrayList<>();
        ArrayList<String> equalsVal = new ArrayList<>();
        ArrayList<Integer> notEqualsKey = new ArrayList<>();
        ArrayList<String> notEqualsVal = new ArrayList<>();

        for (String conditionStr : conditionStrs) {
            String[] conditions = conditionStr.split("!=");
            if (conditions.length == 1) {
                conditions = conditions[0].split("=");

                String fieldName = conditions[0].trim();
                if (fieldName.indexOf(".") != -1)
                    fieldName = fieldName.split("\\.")[1];
                int index = tableFields.indexOf(fieldName); // 字段有点
                equalsKey.add(index);
                if (tableFields.isIntIn(index))
                    equalsVal.add(conditions[1].trim());
                else {
                    String val = conditions[1].trim();
                    equalsVal.add(val.substring(1, val.length() - 1));
                }
            } else {
                String fieldName = conditions[0].trim();
                if (fieldName.indexOf(".") != -1)
                    fieldName = fieldName.split("\\.")[1];
                int index = tableFields.indexOf(fieldName); // 字段有点
                notEqualsKey.add(index);
                if (tableFields.isIntIn(index))
                    notEqualsVal.add(conditions[1].trim());
                else {
                    String val = conditions[1].trim();
                    notEqualsVal.add(val.substring(1, val.length() - 1));
                }
            }
        }

        if (isOr) {
            orEqualsKey = equalsKey.toArray(new Integer[equalsKey.size()]);
            orEqualsVal = equalsVal.toArray(new String[equalsVal.size()]);
            orNotEqualsKey = notEqualsKey.toArray(new Integer[notEqualsKey.size()]);
            orNotEqualsVal = notEqualsVal.toArray(new String[notEqualsVal.size()]);
        } else {
            andEqualsKey = equalsKey.toArray(new Integer[equalsKey.size()]);
            andEqualsVal = equalsVal.toArray(new String[equalsVal.size()]);
            andNotEqualsKey = notEqualsKey.toArray(new Integer[notEqualsKey.size()]);
            andNotEqualsVal = notEqualsVal.toArray(new String[notEqualsVal.size()]);
        }
    }
*/

    private String[] splitIgnoreCase(String str, String target) {
        target = target.toLowerCase();
        int index = str.indexOf(target);
        if (index == -1) {
            target = target.toUpperCase();
            index = str.indexOf(target);
            if (index == -1)
                return new String[]{str};
            else
                return str.split(target);
        } else {
            return str.split(target);
        }
    }

    private String[] arrayTrim(String[] array) {
        int len = array.length;
        for (int i = 0; i < len; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }
}

/**
 * 文件IO工具类
 */
class FileUtils {
    public static void writeFile(String fileName, String content, boolean isAppend) {
        FileWriter fileWriter = null;
        try {
            //使用true，即进行append file
            fileWriter = new FileWriter(fileName, isAppend);
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readFile(String fileName, boolean isNewLine) {
        File file = new File(fileName);
        if (!file.exists())
            throw new RuntimeException("File does not exists");
        BufferedReader reader = null;
        StringBuilder sb = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (isNewLine)
                    sb.append(line + "\n");
                else
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
        return sb.toString();
    }
}
