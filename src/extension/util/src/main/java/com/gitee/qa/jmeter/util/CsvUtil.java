package com.gitee.qa.jmeter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class CsvUtil {

    private static final Logger log = LoggerFactory.getLogger(CsvUtil.class);

    private static final String DELIMITER = ",";

    private String filePath;

    public CsvUtil(String filePath){
        this.filePath = filePath;
    }

    public void write(ArrayList<String> headers, ArrayList<String> values){

        try {

            if (this.fileExist() && !this.fileEmpty()){
                // 校验csv表头
                if (!this.checkHeaders(headers)){
                    log.error("csv文件表头校验失败");
                    return;
                }
            }

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.filePath, true), StandardCharsets.UTF_8));

            // 写入表头
            if (this.fileEmpty()){
                for (int i = 0; i < headers.size(); i++) {
                    out.write(headers.get(i));
                    if (i == headers.size() - 1) break;  // 如果是最后一列
                    out.write(DELIMITER);
                }
                out.newLine();
            }

            // 写入数据
            for (int i = 0; i < values.size(); i++) {
                out.write(values.get(i));
                if (i == values.size() - 1) break;  // 如果是最后一列
                out.write(DELIMITER);
            }
            out.newLine();
            out.flush();
            out.close();
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            log.error("csv文件写入失败\n" + stringWriter.toString());
        }

    }

    // 判断文件是否存在
    private Boolean fileExist(){
        return new File(this.filePath).exists();
    }

    // 判断文件是否为空
    private Boolean fileEmpty() {
        return new File(this.filePath).length() == 0;
    }

    // 判断表头是否存在且正确
    private Boolean checkHeaders(ArrayList<String> headers){
        BufferedReader br = null;
        try {
             br = new BufferedReader(new InputStreamReader(new FileInputStream(this.filePath), StandardCharsets.UTF_8));
            String firstLine = br.readLine();
            return String.join(DELIMITER, headers).equals(firstLine);
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            log.error("csv文件读取失败\n" + stringWriter.toString());
            return false;
        } finally {
            if (br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    StringWriter stringWriter = new StringWriter();
                    e.printStackTrace(new PrintWriter(stringWriter));
                    log.error("csv文件读取失败\n" + stringWriter.toString());
                }
            }
        }
    }

    public static void main(String[] args) {
        new CsvUtil("C:\\Users\\Zheng\\Desktop\\data.csv").write(
                new ArrayList<>(Arrays.asList("h1", "h2", "h3")),
                new ArrayList<>(Arrays.asList("v1", "v2", "v3")));
    }

}
