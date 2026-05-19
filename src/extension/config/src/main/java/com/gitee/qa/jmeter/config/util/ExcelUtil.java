package com.gitee.qa.jmeter.config.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;

public class ExcelUtil {

    static Map<Integer, String> HEADER = null;
    static List<Map<Integer, String>> ALL_DATA = ListUtils.newArrayList();

    static class NoModelDataListener extends AnalysisEventListener<Map<Integer, String>> {
        /**
         * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
         */
        private static final int BATCH_COUNT = 100;
        private List<Map<Integer, String>> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

        @Override
        public void invoke(Map<Integer, String> data, AnalysisContext context) {
//            System.out.println("解析到一条数据:" + data);
            ALL_DATA.add(data);
            cachedDataList.add(data);
            if (cachedDataList.size() >= BATCH_COUNT) {
                saveData();
                cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
            }
        }

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
//            System.out.println("解析到一条头数据:" + headMap);
            HEADER = headMap;
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            saveData();
//            System.out.println("所有数据解析完成！");
        }

        /**
         * 加上存储数据库
         */
        private void saveData() {
//            System.out.println(cachedDataList.size() + "条数据，开始存储数据库！");
//            System.out.println("存储数据库成功！");
        }
    }

    public static void readExcel(String filePath){
        ALL_DATA.clear();
        EasyExcel.read(filePath, new NoModelDataListener()).sheet().doRead();
    }

    public static void readExcel(String filePath, int headRowNumber){
        ALL_DATA.clear();
        EasyExcel.read(filePath, new NoModelDataListener()).sheet().headRowNumber(headRowNumber).doRead();
    }

    public static Map<Integer, String> getHeader(){
        return HEADER;
    }

    public static String[] getHeaderAsArray(){
        if (HEADER == null) return null;
        return HEADER.values().toArray(new String[0]);
    }

    public static List<Map<Integer, String>> getAllData(){
        return ALL_DATA;
    }

    public static void main(String[] args) {
        // Test Code
        String path = "C:\\Users\\Zheng\\Desktop\\QA团队.xlsx";
        ExcelUtil.readExcel(path, 0);
    }
}
