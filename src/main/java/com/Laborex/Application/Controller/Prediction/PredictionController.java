package com.Laborex.Application.Controller.Prediction;


import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Laborex.Application.Model.PredictionDTO.DemandePredictionDTO;
import com.Laborex.Application.Service.Prediction.PredictionService;

@RestController
@RequestMapping("/api/prediction")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @GetMapping("/nextByMonth")
    public Map<Integer, List<DemandePredictionDTO>> getPredictionByMonth(@RequestParam(defaultValue = "3") int months) {
        List<DemandePredictionDTO> predictions = predictionService.predictNextMonths(months);

        Map<Integer, List<DemandePredictionDTO>> predictionsParMois = predictions.stream()
                .collect(Collectors.groupingBy(DemandePredictionDTO::getMois, Collectors.toList()));

        predictionsParMois.forEach((mois, liste) ->
                liste.sort(Comparator.comparing(DemandePredictionDTO::getLibelle))
        );

        return new TreeMap<>(predictionsParMois); // mois triés
    }
    

}


