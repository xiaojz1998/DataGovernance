package com.atguigu.dga.governance.assessor.spec;


import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiaojz
 *
 * 正则表达式: 一个又特定格式、特定含义的字符串，可以用于匹配给定字符串是否满足某种规范。
 * 元子符:
 *   1)   \     : 转义符，用于转义特殊字符
 *   2)   ^     : 表示从头匹配
 *   3)   $     : 表示匹配到尾
 *   4)   *     : 表示0到多次  {0,}
 *   5)   +     : 表示1到多次  {1,}
 *   6)   ?     : 表示0次或1次 {0,1}
 *   7)   {}    : 用于表示次数， {n} 表示n次 , {n,m}表示n到m次 , {n,} 表示n次及n次以上
 *   8)   []    : 用于匹配字符 ， [abc]表示匹配abc任意一个字符  [a-zA-Z0-9]表示匹配字母和数字
 *   9)   ()    : 用于匹配字符串， (abc) 表示匹配abc完整的字符串
 *   10)  |     : 表示或   ， (abc|xyz) 表示匹配abc 或者 xyz的字符串
 *   11)  .     : 表示匹配任意字符
 *   12)  \w    : 匹配字母、数字、下划线。等价于[A-Za-z0-9_]
 *   13)  \W    : 匹配非字母、数字、下划线。等价于 [^A-Za-z0-9_]
 *   14)  \d    : 匹配一个数字字符。等价于 [0-9]
 *   15)  \D    : 匹配非数字字符。 等价于 [^0-9]
 *
 *
 */
@Component("TABLE_NAME_STANDARD")
public class TableNameStandardAssessor extends Assessor {
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException {
        System.out.println("TableNameStandardAssessor 查找问题..... ");

        //定义每层用于判断表名是否合规的正则
        Pattern odsPattern = Pattern.compile("^ods_\\w+_(inc|full)$");
        Pattern dimPattern = Pattern.compile("^dim_\\w+(_(zip|full))?$");
        Pattern dwdPattern = Pattern.compile("^dwd_\\w+_\\w+_(inc|full|acc)$");
        Pattern dwsPattern = Pattern.compile("^dws_\\w+_\\w+_\\w+_(1d|nd|td)$");
        Pattern adsPattern = Pattern.compile("^ads_\\w+$");
        Pattern dmPattern = Pattern.compile("^dm_\\w+$");

        //使用正则表达式来判断表名是否合规
        //获取表名
        String tableName = assessParam.getTableMetaInfo().getTableName();
        //所在层级
        String dwLevel = assessParam.getTableMetaInfo().getTableMetaInfoExtra().getDwLevel();

        // 记录匹配结果
        Matcher matcher = null ;

        if(DgaConstant.DW_LEVEL_ODS.equals( dwLevel )){
            matcher = odsPattern.matcher( tableName );
        }else if (DgaConstant.DW_LEVEL_DIM.equals( dwLevel )){
            matcher = dimPattern.matcher( tableName );
        }else if(DgaConstant.DW_LEVEL_DWD.equals(dwLevel)){
            matcher = dwdPattern.matcher(tableName);
        }else if(DgaConstant.DW_LEVEL_DWS.equals(dwLevel)){
            matcher = dwsPattern.matcher(tableName);
        }else if(DgaConstant.DW_LEVEL_ADS.equals(dwLevel)){
            matcher = adsPattern.matcher(tableName);
        }else if(DgaConstant.DW_LEVEL_DM.equals(dwLevel)){
            matcher = dmPattern.matcher(tableName);
        }else{
            //未纳入分层
            //给分
            governanceAssessDetail.setAssessScore(BigDecimal.valueOf(5L));
            //问题项
            governanceAssessDetail.setAssessProblem("未纳入分层");
            return ;
        }
        if(!matcher.matches()){
            // 给分
            governanceAssessDetail.setAssessScore( BigDecimal.ZERO );
            //问题项
            governanceAssessDetail.setAssessProblem("表名不合规");
        }

    }
}
