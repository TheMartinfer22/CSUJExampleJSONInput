package br.com.foursys.csu;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainOperations {
    public static void main(String[] args) throws IOException {
        System.out.println("OpenAPI 3.0.1 - CSU DATA Exporter" + System.lineSeparator());
        System.setProperty("file.encoding", "UTF-8");

        Gson gson = new Gson();

        List<Map.Entry<String, JsonElement>> componentsReplace = new ArrayList<>();
        List<Map.Entry<String, JsonElement>> pathsReplace = new ArrayList<>();

        // Database
        Reader toRead = Files.newBufferedReader(Paths.get("PortalAPI-DATA.json"));
        JsonObject objToRead = gson.fromJson(toRead, JsonObject.class);

        if (objToRead.get("components").getAsJsonObject().has("schemas")){
            componentsReplace.addAll(objToRead.get("components").getAsJsonObject().get("schemas").getAsJsonObject().entrySet());
            pathsReplace.addAll(objToRead.get("paths").getAsJsonObject().entrySet());
            System.out.println("Tipo de data: Utilizando schemas");
        } else {
            componentsReplace.addAll(objToRead.get("components").getAsJsonObject().entrySet());
            System.out.println("Tipo de data: Não utilizando schemas");
        }

        // Target
        Reader toWrite = Files.newBufferedReader(Paths.get("aws.json"));
        JsonObject objToWrite = gson.fromJson(toWrite, JsonObject.class);


        // Abstractions
        JsonObject componentsModel = objToWrite.get("components").getAsJsonObject();
        JsonObject schemasModel = componentsModel.get("schemas").getAsJsonObject();
        JsonObject pathsModel = objToWrite.get("paths").getAsJsonObject();

        // Replace if exist Components
        replaceIfExist(componentsReplace, schemasModel);

        // Replace if exist paths
        replaceIfExist(pathsReplace, pathsModel);

        BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("PortalAPI-DATA.json"), StandardCharsets.UTF_8));

        Gson writerJson = new Gson();

        writerJson.toJson(objToWrite, fw);
        fw.flush();
        fw.close();

        // Organize JSON File
        writerJson.newBuilder().setPrettyPrinting().create();

        System.out.println("Exportado com sucesso!");
    }

    private static void replaceIfExist(List<Map.Entry<String, JsonElement>> targetReplaceList, JsonObject model) {
        for (Map.Entry<String, JsonElement> entry : targetReplaceList) {
            if (!model.has(entry.getKey())) {
                model.remove(entry.getKey());
            } else {
                model.remove(entry.getKey());
                model.add(entry.getKey(), entry.getValue());
            }
        }
    }
}
