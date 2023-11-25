package com.eskcti.validador;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Map;

public class Handler implements RequestHandler<S3Event, String> {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String handleRequest(S3Event event, Context context) {
        LambdaLogger logger = context.getLogger();

        var record = event.getRecords().get(0);
        String nomeObjeto = record.getS3().getObject().getUrlDecodedKey();
        String bucket = record.getS3().getBucket().getName();

        logger.log("OBJETO: " + nomeObjeto);
        logger.log("BUCKET: " + bucket);

        String[] tipos = System.getenv().get("tipos").split(",");

        var tipoObjeto = nomeObjeto.split("\\.")[1].toLowerCase();
        boolean valido = Arrays.stream(tipos).anyMatch(tipoObjeto::equals);

        if (!valido) {
            try {
                AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, nomeObjeto);
                s3Client.deleteObject(deleteObjectRequest);

                logger.log("************** OBJETO INVÁLIDO *****************");
                logger.log("Objeto excluido com sucesso");

                return "Arquivo: " + nomeObjeto + " excluido com sucesso";
            } catch (Exception e) {
                logger.log(e.getMessage());
                throw new RuntimeException();
            }
        }

        logger.log("***************** OBJETO VáLIDO *********************");

        return "Arquivo: " + nomeObjeto + " validado";
    }
}
