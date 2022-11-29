package com.sxk.pmml;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.apache.commons.io.FileUtils;

public class PmmlBatchTest {


  public static void main(String... args) throws Exception {
    //recBatchTest();
    popBatchTest();
  }

  public static void recBatchTest() throws Exception {
    String filePath = "/Users/sunxiongkun/Documents/feature/mode/lightgbm_20221125.pmml";
    String testPath = "/Users/sunxiongkun/Documents/feature/data/rank_test.txt";

    PmmlBean pmmlBean = new PmmlBean(filePath);

    List<Map<String, ?>> data = loadData(new File(testPath));

    int warmUpBatchSize = 10000;

    System.out.println("| Configuration | Time (sec) | Time per row (microsec) |");

    evaluate(pmmlBean, data, warmUpBatchSize);

    evaluate(pmmlBean, data, 10000);

  }

  public static void popBatchTest() throws Exception {

    String filePath = "/Users/sunxiongkun/Documents/feature/mode/popup_lgb_20220919.pmml";
    String testPath = "/Users/sunxiongkun/Documents/feature/data/test_data_20.csv";

    PmmlBean pmmlBean = new PmmlBean(filePath);

    List<Map<String, ?>> data = loadData(new File(testPath));

    int warmUpBatchSize = 10000;

    System.out.println("| Configuration | Time (sec) | Time per row (microsec) |");

    evaluate(pmmlBean, data, warmUpBatchSize);

    evaluate(pmmlBean, data, 10000);

  }

  static
  private void evaluate(PmmlBean pmmlBean, List<Map<String, ?>> data, int limit) {
    List<Map<String, ?>> sample = makeSample(data, limit);

    long startTime = System.currentTimeMillis();

    for (Map<String, ?> row : sample) {
      pmmlBean.predictProba((Map<String, Object>) row);
    }

    long endTime = System.currentTimeMillis();

    printSummary("1 * " + sample.size(), (endTime - startTime), sample.size());
  }


  static
  private void printSummary(String config, long elapsedTime, int numScored) {
    System.out.println(
        String.format(Locale.US, "| %1s | %2$.6f | %3$.3f |", config, elapsedTime / 1.0,
            (elapsedTime * 1.0) / numScored));
  }

  static
  private List<Map<String, ?>> loadData(File csvFile) throws IOException {
    List<Map<String, ?>> table = new ArrayList<>();

    List<String> strings = FileUtils.readLines(csvFile);
    String[] headArray = strings.get(0).split(",");

    for (int i = 1; i < strings.size(); i++) {
      String[] valueArray = strings.get(i).split(",");

      Map<String, Object> map = new HashMap<>();
      for (int i1 = 0; i1 < headArray.length; i1++) {
        map.put(headArray[i1], Double.valueOf(valueArray[i1]));
      }
      table.add(map);
    }

    return table;
  }

  static
  private <E> List<E> makeSample(List<E> list, int size) {
    List<E> result = new ArrayList<>(size);

    Random random = new Random();

    for (int i = 0; i < size; i++) {
      result.add(list.get(random.nextInt(list.size())));
    }

    return result;
  }
}