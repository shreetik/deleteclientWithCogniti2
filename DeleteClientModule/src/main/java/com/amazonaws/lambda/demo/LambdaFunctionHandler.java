package com.amazonaws.lambda.demo;

import com.amazonaws.lambda.entities.Request;
import com.amazonaws.lambda.entities.Response;
import com.amazonaws.lambda.service.RequestProcess;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler implements RequestHandler<Request, Response> {

	Response response = new Response();
	RequestProcess process = new RequestProcess();
	
    @Override
    public Response handleRequest(Request input, Context context) {
        context.getLogger().log("Input: " + input);
        try {
			return process.doProcess(input);
		} catch (Exception e) {
			response.setStatus("400");
			response.setMsg(e.getMessage());
			response.setClients(null);
			return response;
		}
        
    }

}
