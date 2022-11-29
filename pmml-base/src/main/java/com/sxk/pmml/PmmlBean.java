package com.sxk.pmml;

import com.alibaba.fastjson.JSON;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningFunction;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.TargetField;


/**
 * @author sunxiongkun
 */
@Slf4j
public class PmmlBean {

  private static final String DIRTY_NUM_PREFIX = "src_";

  private final String pmmlFileName;

  public PmmlBean(String pmmlFileName) {
    this.pmmlFileName = pmmlFileName;
    init();
  }

  @Getter
  private Evaluator evaluator;
  @Getter
  private List<InputField> inputFields;

  private List<TargetField> targetFields;

  private List<OutputField> outputFields;

  private String targetName = null;

  public void init() {
    try {
      InputStream is = Files.newInputStream(Paths.get(pmmlFileName));

//      PMML unmarshal = PMMLUtil.unmarshal(is);
//      ModelEvaluatorBuilder modelEvaluatorBuilder = new ModelEvaluatorBuilder(unmarshal);
//      evaluator = modelEvaluatorBuilder.build();
      evaluator = new LoadingModelEvaluatorBuilder()
          .load(is)
          .build();

      log.info("pmml load model success:{}", pmmlFileName);
      initFieldsAndAnalysisImportance();
    } catch (Exception e) {
      log.error("pmml load error:{},:", pmmlFileName, e);
    }
  }

  private void initFieldsAndAnalysisImportance() {
    inputFields = evaluator.getInputFields();
    targetFields = evaluator.getTargetFields();
    outputFields = evaluator.getOutputFields();

    MiningFunction miningFunction = evaluator.getMiningFunction();
    switch (miningFunction) {
      case REGRESSION:
        targetName = targetFields.get(0).getName();
        break;
      case CLASSIFICATION:
        targetName = outputFields.get(1).getName();
        break;
      default:
        log.error("pmml load error:{},miningFunction:{}", pmmlFileName, miningFunction);
    }
    List<MiningField> collect = inputFields
        .stream()
        .map(InputField::getMiningField)
        .filter(f -> f != null && f.getImportance() != null)
        .sorted(
            Comparator.comparing(e -> e.getImportance().doubleValue(), Comparator.reverseOrder()))
        .collect(Collectors.toList());
    log.info("pmml:{},fields:{}", pmmlFileName, JSON.toJSONString(collect));
  }


  private Double getProbabilityScore(Map<String, Object> arguments, Double fillNumDefault,
      boolean numberCheck) {
    if (numberCheck) {
      for (InputField inputField : inputFields) {
        String fieldName = inputField.getFieldName();
        Object value = arguments.get(fieldName);
        if (arguments.containsKey(fieldName)) {
          //如果不是number，直接用默认值覆盖，以免报错
          if (!NumberUtils.isCreatable(String.valueOf(value))) {
            arguments.put(DIRTY_NUM_PREFIX + fieldName, value);
            arguments.put(fieldName, fillNumDefault);
          }
        } else {
          arguments.put(fieldName, fillNumDefault);
        }
      }
    } else {
      for (InputField inputField : inputFields) {
        String fieldName = inputField.getFieldName();
        if (!arguments.containsKey(fieldName)) {
          arguments.put(fieldName, fillNumDefault);
        }
      }
    }
    long startTime = System.currentTimeMillis();
    Map<String, ?> evaluateResult = evaluator.evaluate(arguments);
    System.out.println("use time:" + (System.currentTimeMillis() - startTime));
    evaluateResult = EvaluatorUtil.decodeAll(evaluateResult);
    return (Double) evaluateResult.get(targetName);
  }

  /**
   * 预测概率
   *
   * @param arguments
   * @return
   */
  public Double predictProba(Map<String, Object> arguments) {
    return predictProba(arguments, 0d, false);
  }

  public Double predictProba(Map<String, Object> arguments, Double fillNumDefault) {
    return predictProba(arguments, fillNumDefault, false);
  }

  public Double predictProba(Map<String, Object> arguments, Double fillNumDefault,
      boolean numberCheck) {
    if (arguments == null || arguments.isEmpty()) {
      return 0d;
    }
    return getProbabilityScore(arguments, fillNumDefault, numberCheck);
  }
}
