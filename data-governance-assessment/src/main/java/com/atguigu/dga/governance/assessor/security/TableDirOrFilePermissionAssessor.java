package com.atguigu.dga.governance.assessor.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component("FILE_ACCESS_PERMISSION")
public class TableDirOrFilePermissionAssessor extends Assessor {
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException, URISyntaxException, IOException, InterruptedException {
        System.out.println("TableDirOrFilePermissionAssessor 查找问题..... ");
        //判断当前表路径下所有的文件和目录是否越权

        // 取指标参数
        String metricParamsJson = assessParam.getGovernanceMetric().getMetricParamsJson();
        JSONObject jsonObject = JSON.parseObject(metricParamsJson);
        String dir_permission = jsonObject.getString("dir_permission");
        String file_permission = jsonObject.getString("file_permission");

        //表路径
        String tableFsPath = assessParam.getTableMetaInfo().getTableFsPath();

        // 文件系统对象
        FileSystem fileSystem = FileSystem.get(new URI(tableFsPath), new Configuration(), assessParam.getTableMetaInfo().getTableFsOwner());
        //递归每个目录和文件是否越权
        FileStatus[] fileStatuses = fileSystem.listStatus(new Path(tableFsPath));

        //创建集合，记录越权的文件和目录
        List<String> filePermissionList = new ArrayList<>();
        List<String> dirPermissionList = new ArrayList<>();

        // 检查是否越权函数
        checkDirOrFilePermission(fileSystem , fileStatuses , dir_permission , file_permission ,dirPermissionList , filePermissionList);
        //判断集合中是否有越权的文件或者目录
        if(filePermissionList.size()>0 || dirPermissionList.size()>0){
            //给分
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            //问题项
            governanceAssessDetail.setAssessProblem("目录或者文件越权");
            //备注
            governanceAssessDetail.setAssessComment("越权的文件: " + filePermissionList + " , 越权的目录: " + dirPermissionList);

        }
    }
    /**
     * 递归判断目录和文件是否越权
     */
    private void checkDirOrFilePermission(FileSystem fileSystem, FileStatus[] fileStatuses, String dir_permission, String file_permission, List<String> dirPermissionList, List<String> filePermissionList) throws IOException{
        for (FileStatus fileStatus : fileStatuses) {
            if(fileStatus.isFile()){
                // 获取当前文件的权限
                FsPermission currentFilePermission = fileStatus.getPermission();
                boolean b = checkPermission(currentFilePermission, file_permission);
                if(b){
                    //记录越权的文件
                    filePermissionList.add(fileStatus.getPath().toString());
                }
            } else {
                //判断当前目录是否越权
                FsPermission currentDirPermission = fileStatus.getPermission();
                boolean b = checkPermission(currentDirPermission, dir_permission);
                if(b){
                    //记录越权的目录
                    dirPermissionList.add(fileStatus.getPath().toString());
                }
                //递归当前目录下的内容
                FileStatus[] subFileStatuses = fileSystem.listStatus(fileStatus.getPath());
                checkDirOrFilePermission(fileSystem,subFileStatuses,dir_permission,file_permission,dirPermissionList,filePermissionList);
            }
        }
    }
    private boolean checkPermission(FsPermission currentFileOrDirPermission, String paramFileOrDirPermission){
        //定义标准权限字典
        HashMap<String, List<String>> permissionMap = new HashMap<>();
        permissionMap.put("1" , Arrays.asList("X"));
        permissionMap.put("2" , Arrays.asList("W"));
        permissionMap.put("3" , Arrays.asList("W" , "X"));
        permissionMap.put("4" , Arrays.asList("R"));
        permissionMap.put("5" , Arrays.asList("R" , "X"));
        permissionMap.put("6" , Arrays.asList("R" , "W"));
        permissionMap.put("7" , Arrays.asList("R" , "W" , "X"));

        // 取标准权限
        String userRWX = paramFileOrDirPermission.charAt(0) + "";
        String groupRWX = paramFileOrDirPermission.charAt(1) + "";
        String otherRWX = paramFileOrDirPermission.charAt(2) + "";

        // 取文件或目录权限
        String fileOrDirUserRWX = currentFileOrDirPermission.getUserAction().ordinal() + "";
        String fileOrDirGroupRWX = currentFileOrDirPermission.getGroupAction().ordinal() + "";
        String fileOrDirOtherRWX = currentFileOrDirPermission.getOtherAction().ordinal() + "";

        //判断
        if(CollectionUtils.subtract( permissionMap.get(fileOrDirUserRWX) , permissionMap.get(userRWX)).size() > 0  ){
            return true ;
        }else if (CollectionUtils.subtract( permissionMap.get(fileOrDirGroupRWX) , permissionMap.get(groupRWX)).size() > 0){
            return true ;
        }else if (CollectionUtils.subtract( permissionMap.get(fileOrDirOtherRWX) , permissionMap.get(otherRWX)).size() > 0){
            return true ;
        }
        return false ;
    }
}
