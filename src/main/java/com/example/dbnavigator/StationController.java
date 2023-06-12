package com.example.dbnavigator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;


@RestController
public class StationController {
    //     a datastructure to store the stations so that we can access them given the three parameters ril100, trainNumber and number
    XMLFileReader xmlFileReader = new XMLFileReader("src/main/resources/Wagenreihungsplan_RawData_201712112");


    @GetMapping("/station/{ril100}/train/{trainNumber}/waggon/{number}")
    public String getWaggonRailNumber(@PathVariable String ril100, @PathVariable int trainNumber, @PathVariable int number) {
        ril100 = ril100.toUpperCase();
        return xmlFileReader.getSection(ril100, trainNumber, number);
    }
}
