package com.atguigu.dga.util;

import org.apache.hadoop.hive.ql.lib.DefaultGraphWalker;
import org.apache.hadoop.hive.ql.lib.Dispatcher;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.SemanticException;

import java.util.Collections;
import java.util.Stack;

public class SqlUtil {
    /**
     * SQL解析方法
     * 使用Hive提供的SQL解析工具， 完成将给定的SQL转换成语法树，同时对语法树进行遍历处理（ 转换和遍历都有现成的实现）
     * 我们要做的是在遍历到每个节点的时候，对当前节点进行处理。
     *
     * dispatcher 是对节点进行处理的处理对象，因为每次写sql处理都不同
     */
    public static void parseSql(String sql, Dispatcher dispatcher){
        try {
            // 将给定的sql解析成语法树
            ParseDriver parseDriver = new ParseDriver();
            ASTNode parse = parseDriver.parse(sql);

            // 找到tok_query节点 作为根节点
            while(parse.getType() != HiveParser.TOK_QUERY){
                parse = (ASTNode) parse.getChild(0);
            }
            System.out.println(parse);
            // 对语法树进行遍历处理
            DefaultGraphWalker defaultGraphWalker = new DefaultGraphWalker(dispatcher);
            // 开始遍历
            // 传入一个集合，因为只有一个节点，所以用singleton方法，后面是返回，不用传，直接返回到dispatcher中
            defaultGraphWalker.startWalking(Collections.singleton(parse),null);
        }catch(Exception e){
            throw new RuntimeException("sql解析失败");
        }
    }

    public static void main(String[] args) {
        String sql = " select t1.a , t1.b , cc(c) from  test.A t1  join  B t2  on t1.id = t2.id where t1.a = '1234' and c = '456' group by t1.a , t1.b " ;

        parseSql(sql, new Dispatcher() {
            @Override
            public Object dispatch(Node node, Stack<Node> stack, Object... objects) throws SemanticException {
                return null;
            }
        });
    }



    //  下面是sql过滤方法，防止sql注入
    public static String filterUnsafeSql(String input) {
        if (input == null) {
            return null;
        }

        // 替换 MySQL 中可能导致 SQL 注入的特殊字符
        return input.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\u001A", "\\Z")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}

