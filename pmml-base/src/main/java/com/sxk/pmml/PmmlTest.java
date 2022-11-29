package com.sxk.pmml;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

public class PmmlTest {

  public static void main(String[] args) throws Exception {

    String filePath = "/Users/sunxiongkun/Documents/feature/mode/lgb_20220825_1.pmml";
    //String filePath2 = "/Users/sunxiongkun/Documents/feature/mode/xgb_changba_sing_3_1.pmml";
    String testPath = "/Users/sunxiongkun/Documents/feature/data/huice24_10_1.csv";

//    filePath = "/Users/sunxiongkun/Documents/feature/mode/poup_lgb_1.pmml";
//    testPath = "/Users/sunxiongkun/Documents/feature/data/test_data_20.csv";

    List<String> list = FileUtils.readLines(new File(testPath), StandardCharsets.UTF_8);
    String[] headerArray = list.get(0).split(",");

    Map<String, Integer> headMap = new HashMap<>();
    for (int i = 0; i < headerArray.length; i++) {
      headMap.put(headerArray[i], i);
    }

    PmmlBean pmmlBean = new PmmlBean(filePath);

    int total = 0;
    for (int i = 1; i < list.size(); i++) {
      String[] featureValue = list.get(i).split(",");

      Map<String, Object> featureMap = new HashMap<>();
      for (int j = 1; j < headerArray.length; j++) {
        String name = headerArray[j];
        String value = featureValue[j];
        featureMap.put(name, Double.valueOf(value));
      }
      long start = System.currentTimeMillis();
      Double score = pmmlBean.predictProba(featureMap);
      total += System.currentTimeMillis() - start;
      System.out.println(featureValue[0] + "   " + score);
    }

    System.out.println("use time:" + total);


  }

}
