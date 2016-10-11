package org.tg.webinar.pa;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.EntityProbabilityDistribution;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.NeuralNetworkEvaluator;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by asoni on 10/11/16.
 */

@RestController
@RequestMapping("/win-predictor")
public class RootController {

    static Logger logger = Logger.getLogger(RootController.class.getName());
    static PMML model = null;
    static NeuralNetworkEvaluator neuralNetEvaluator;

    static {
        try {
            InputStream is = new FileInputStream(new File("./model/kohli_nnet_model.pmml"));
            InputSource source = new InputSource(is);
            SAXSource transformedSource = ImportFilter.apply(source);
            model = JAXBUtil.unmarshalPMML(transformedSource);
            neuralNetEvaluator = new NeuralNetworkEvaluator(model);

            List<FieldName> activeFields = neuralNetEvaluator.getActiveFields();
            List<FieldName> targetFields = neuralNetEvaluator.getTargetFields();

            logger.info("Active Fields: " + activeFields);
            logger.info("Target Fields:" + targetFields);
        }
        catch (Exception e){
            throw new RuntimeException("Error while reading the Machine Learning Model file");
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> predictMatchResult(@RequestBody String scoreStats){

        JsonParser parser = new JsonParser();
        JsonObject matchScoreStats = parser.parse(scoreStats).getAsJsonObject();

        String opposite_team =  matchScoreStats.get("opposite_team").getAsString();
        String chasing = matchScoreStats.get("chasing").getAsString();
        int batsmanScoreIn10Overs=matchScoreStats.get("batsmanScoreIn10Overs").getAsInt();
        int indiaScoreIn10Overs=matchScoreStats.get("indiaScoreIn10Overs").getAsInt();
        int batsmanScoreIn20Overs=matchScoreStats.get("batsmanScoreIn20Overs").getAsInt();
        int indiaScoreIn20Overs=matchScoreStats.get("indiaScoreIn20Overs").getAsInt();
        int batsmanScoreIn30Overs=matchScoreStats.get("batsmanScoreIn30Overs").getAsInt();
        int indiaScoreIn30Overs=matchScoreStats.get("indiaScoreIn30Overs").getAsInt();

        Map<FieldName, FieldValue> paramMap = new LinkedHashMap<FieldName, FieldValue>();

        addStringParam(paramMap,"opposite_team", opposite_team);
        addStringParam(paramMap, "chasing", chasing);
        addIntParam(paramMap,"batsmanScoreIn10Overs",batsmanScoreIn10Overs);
        addIntParam(paramMap,"indiaScoreIn10Overs",indiaScoreIn10Overs);
        addIntParam(paramMap,"batsmanScoreIn20Overs",batsmanScoreIn20Overs);
        addIntParam(paramMap,"indiaScoreIn20Overs",indiaScoreIn20Overs);
        addIntParam(paramMap,"batsmanScoreIn30Overs",batsmanScoreIn30Overs);
        addIntParam(paramMap, "indiaScoreIn30Overs", indiaScoreIn30Overs);

        Map<FieldName, ?> results = neuralNetEvaluator.evaluate(paramMap);
        FieldName targetName = neuralNetEvaluator.getTargetField();
        EntityProbabilityDistribution targetValue = (EntityProbabilityDistribution)results.get(targetName);
        System.out.println("[Neural Net Model]:> Probability Distribution is: "+targetValue);
        System.out.println("[Neural Net Model]:> Predicted Value is: "+targetValue.getResult());

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("result", targetValue.getResult().toString());
        return new ResponseEntity<String>(responseJson.toString(), HttpStatus.OK);
    }

    private static void addIntParam(Map<FieldName, FieldValue> paramMap, String fieldNameStr, int fieldValueInt) {
        FieldName fn = new FieldName(fieldNameStr);
        FieldValue fv = neuralNetEvaluator.prepare(fn, fieldValueInt);
        paramMap.put(fn, fv);
    }

    private static void addStringParam(Map<FieldName, FieldValue> paramMap, String fieldNameStr, String fieldValueString) {
        FieldName fn = new FieldName(fieldNameStr);
        FieldValue fv = neuralNetEvaluator.prepare(fn, fieldValueString);
        paramMap.put(fn, fv);
    }
}
