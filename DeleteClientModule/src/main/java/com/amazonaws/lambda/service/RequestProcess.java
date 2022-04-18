package com.amazonaws.lambda.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.lambda.entities.ClientData;
import com.amazonaws.lambda.entities.Request;
import com.amazonaws.lambda.entities.Response;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserResult;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

public class RequestProcess {

	AmazonDynamoDB dbclient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();

	AWSCognitoIdentityProvider cognitoProvider = AWSCognitoIdentityProviderClientBuilder
			.standard()
			.withRegion(Regions.AP_SOUTH_1)
			.build();

	Response res = new Response();
	List<ClientData> cList = new ArrayList<ClientData>();
	

	public Response doProcess(Request request) {

		switch (request.getHttpMethod()) {

		case "GET":

			DynamoDB dynamoDB = new DynamoDB(dbclient);

			Table table = dynamoDB.getTable("ClientData");

			ScanSpec scanSpec = new ScanSpec()
					.withProjectionExpression("email_id,company_name,deleteBy,phone_no,client_name,client_status,id");

			try {

				ItemCollection<ScanOutcome> items = table.scan(scanSpec);

				Iterator<Item> iter = items.iterator();
				while (iter.hasNext()) {
					Item item = iter.next();
					ClientData cobj = new ClientData();
					
					cobj.setEmail_id(item.getString("email_id"));
					cobj.setClient_name(item.getString("client_name"));
					cobj.setCompany_name(item.getString("company_name"));
					cobj.setPhone_no(item.getString("phone_no"));
					cobj.setStatus(item.getString("client_status"));
					cobj.setId(item.getString("id"));
					System.out.println(cobj);
					cList.add(cobj);
				}

				res.setStatus("200");
				res.setMsg("All Data retrieved successfully");
				res.setClients(cList);
				return res;
			} catch (Exception e) {
				res.setStatus("400");
				res.setMsg(e.getMessage());
				res.setClients(null);
				return res;
			}

		case "POST"	:{
			
			String email = request.getEmail();
			String poolID = "ap-south-1_tKm1HEzWU";
			try {
				
				
				  AdminDeleteUserRequest adminDeleteUserRequest = new AdminDeleteUserRequest()
				  .withUsername(email).withUserPoolId(poolID);
				  
				  
				 AdminDeleteUserResult result = cognitoProvider.adminDeleteUser(adminDeleteUserRequest);
				 	
				 
				
				DynamoDB dynamoDB1 = new DynamoDB(dbclient);

				Table table1 = dynamoDB1.getTable("ClientData");
				
				UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("email_id",email)
						.withUpdateExpression("set client_status = :c")
						.withValueMap(new ValueMap().withString(":c", "Inactive"));
				
				UpdateItemOutcome outcome = table1.updateItem(updateItemSpec);
													

				
					res.setStatus("200");
					res.setMsg("success");
					res.setClients(null);
					return res;
				
			} catch (Exception e) {
				res.setStatus("400");
				res.setMsg(e.getMessage());
				res.setClients(null);
				return res;
			}
		}
		default:
			res.setStatus("400");
			res.setMsg("invalid Action");
			res.setClients(null);
			return res;
		}
	}
}
