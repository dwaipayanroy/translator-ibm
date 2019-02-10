/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translateIBM;

/**
 * Copyright 2017 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
//package com.ibm.watson.developer_cloud.language_translator.v2;

import com.ibm.watson.developer_cloud.language_translator.v3.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslateOptions.Builder;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslationModels;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslationResult;
//import com.ibm.watson.developer_cloud.language_translator.v3.util.Language;
import com.ibm.watson.developer_cloud.service.exception.InternalServerErrorException;
import com.ibm.watson.developer_cloud.service.exception.ServiceResponseException;
import com.ibm.watson.developer_cloud.service.exception.ServiceUnavailableException;
//import com.ibm.watson.developer_cloud.service.security.IamOptions;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;

//import javax.json.Json;
//import javax.json.JsonArray;
//import javax.json.JsonObject;
//import javax.json.JsonReader;
//import javax.json.JsonValue;
//

/**
 * Example of how to translate a sentence from English to Spanish.
 */
public class TranslateIBM {

    LanguageTranslator service;
    TranslateOptions translateOptions;
    Builder translateBuilder;
    PrintWriter writer;

    public TranslateIBM() {
        service = new LanguageTranslator("2018-05-01");
    //    service.setUsernameAndPassword("<username>", "<password>");
//        service.setUsernameAndPassword("ec41784d-3587-4496-9213-51db818d2931", "KNWywewYYwq6");
        service.setUsernameAndPassword("3af257d4-f41a-4ef0-94b2-cfd3e99b9cab", "a6vJW8IMi6Wx"); // sumit's credentials

        translateBuilder = new TranslateOptions.Builder();
    }

    public void printModels() {
        TranslationModels models = service.listModels()
        .execute();

        System.out.println(models);
    }

    public String translate(String text, String langFrom, String langTo, int docNumber, int tryCount) throws InterruptedException, UnsupportedEncodingException {

        String translatedStr = "";

        translateOptions = new TranslateOptions.Builder()
    //        .addText("Amazon Translate removes the complexity of building real-time and batch translation capabilities into your applications with a simple API call.")
        .addText(text)
        .source(langFrom)
        .target(langTo)
        .build();

//        translateBuilder.addText(text).source(langFrom).target(langTo);
        try {
            TranslationResult translationResult;
            System.out.println(text.length() + ":" + text.getBytes("utf8").length);
            translationResult = service.translate(translateOptions).execute();

            translatedStr = translationResult.getTranslations().toString();
            translatedStr = translatedStr.substring(1, translatedStr.length()-1);
        }
        catch (ServiceUnavailableException | InternalServerErrorException ex) {
            if(tryCount>5) {
                System.err.println("TranslationFailed: "+docNumber);
                translatedStr = "";
            }
            else {
                Thread.sleep(10000*tryCount);
                translate(text, langFrom, langTo, docNumber, ++tryCount);
                translatedStr = translatedStr.substring(1, translatedStr.length()-1);
            }
        }
        catch (ServiceResponseException ex) {
            if(tryCount>5) {
                System.err.println("TranslationFailed-2: "+docNumber);
                translatedStr = "";
            }
            else {
                Thread.sleep(10000*tryCount);
                translate(text, langFrom, langTo, docNumber, ++tryCount);
                translatedStr = translatedStr.substring(1, translatedStr.length()-1);
            }            
        }
        return translatedStr;
    }

    public void writeToFile (String translatedStr) {

        JsonReader jsonReader;
        JsonObject jsonObject;

        try {
            jsonReader = Json.createReader(new StringReader(translatedStr));
            jsonObject = jsonReader.readObject();
            translatedStr = jsonObject.getString("translation");
            //System.out.println();
            writer.print(translatedStr);
        }
        catch (JsonException ex) {
            System.err.println("Error in JSON parsing:\n" + translatedStr);
            writer.print("<JSON-ERROR>");
        }
    }
    @SuppressWarnings("ConvertToTryWithResources")
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {

        JsonReader jsonReader;
        JsonObject jsonObject;
        String sentenceDelimiter = ". ";
        String sentenceDelimiterRegex = "\\. ";
        if(args.length != 4) {
            System.out.println("Usage: java LanguageTranslate <input-path> <lang-from> <lang-to> <output-path>");
//            System.exit(0);
        }
        TranslateIBM example = new TranslateIBM();

        FileInputStream fis = new FileInputStream("/home/dwaipayan/foo");
//        FileInputStream fis = new FileInputStream(args[0]);
        String langFrom =   "es"; //*/args[1];
        String langTo   =   "en";   //*/args[2];
        String outputPath = "/home/dwaipayan/translated.out"; //*/args[3];

	example.writer = new PrintWriter(outputPath, "UTF-8");

        if(langFrom.equalsIgnoreCase("hi"))
            sentenceDelimiter = "\u0964";

        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String line;
        String str = "";
        String translatedStr;
        int docNumber = 0;

        while ((line = br.readLine()) != null) {
            System.out.println(++docNumber+" : Translated document count: ");
            str = "";
            //System.out.println(line);
            System.out.println(line.getBytes("utf8").length);

            if(line.getBytes("utf8").length<47000) {
                translatedStr = example.translate(line, langFrom, langTo, docNumber, 1);
                    example.writeToFile(translatedStr);
                    str = "";
            }
            else {
                for(String sentence : line.split(sentenceDelimiterRegex)) {
                    if(((str.getBytes("utf8").length) + (sentence.getBytes("utf8").length))> 47000) {
                        translatedStr = example.translate(str, langFrom, langTo, docNumber, 1);
                        example.writeToFile(translatedStr);
                        str = sentence + sentenceDelimiter;
                    }
                    else
                        str = str + sentence + sentenceDelimiter;
                }
            }
            if(!str.isEmpty()) {
                translatedStr = example.translate(str, langFrom, langTo, docNumber, 1);
                example.writeToFile(translatedStr);
                str = "";
            }
            example.writer.println();
        }

        if(!str.isEmpty()) {
            translatedStr = example.translate(str, langFrom, langTo, docNumber, 1);
            example.writeToFile(translatedStr);
        }

        br.close();
        fis.close();
	example.writer.close();

//        example.translate("नमस्ते.", "hi", "en");
        /*
        TranslateOptions translateOptions = new TranslateOptions.Builder()
    //        .addText("Amazon Translate removes the complexity of building real-time and batch translation capabilities into your applications with a simple API call.")
        .addText("नमस्ते.")
        .source(Language.HINDI)
        .target(Language.ENGLISH)
        .build();
        TranslationResult translationResult = example.service.translate(translateOptions).execute();

        System.out.println(translationResult);
        */

        /*
        IamOptions options = new IamOptions.Builder()
        .apiKey("zvW56tWOu2j4ktvkeX8ZZBy-wcZEz3buPtq6ZV7CBgqq")
        .build();

        LanguageTranslator languageTranslator = new LanguageTranslator("2018-05-01", options);
        languageTranslator.setEndPoint("https://gateway.watsonplatform.net/language-translator/api");

        translateOptions = new TranslateOptions.Builder()
    //        .addText("Amazon Translate removes the complexity of building real-time and batch translation capabilities into your applications with a simple API call.")
        .addText("नमस्ते.")
        .source(Language.HINDI)
        .target(Language.ENGLISH)
        .build();
        translationResult = service.translate(translateOptions).execute();

        System.out.println(translationResult);
//        */
    }

}
//