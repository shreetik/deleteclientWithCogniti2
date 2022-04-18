package com.amazonaws.lambda.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

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
			
		case "DELETE":{
			
			//generate random number
			Random rnd = new Random();
			int number = rnd.nextInt(9999);
			
			String code = String.format("%04d", number);
			
			//update db with code
			DynamoDB dynamoDB1 = new DynamoDB(dbclient);

			Table table1 = dynamoDB1.getTable("ClientData");
			
			UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("email_id",request.getEmail())
					.withUpdateExpression("set auth_code = :a")
					.withValueMap(new ValueMap().withString(":a", code));
			
			UpdateItemOutcome outcome = table1.updateItem(updateItemSpec);
			
			//sending mail to user
			String FROM = "shreetikkumar@gmail.com";
			String TO = request.getEmail();
			String SUBJECT = "Confirmation code";
			String TEXTBODY = "Your confirmation code is "+code;
			
			try {
				AmazonSimpleEmailService eclient = AmazonSimpleEmailServiceClientBuilder
						.standard()
						.withRegion(Regions.AP_SOUTH_1)
						.build();
				SendEmailRequest emailRequest = new SendEmailRequest()
						.withDestination(new Destination().withToAddresses(TO))
						.withMessage(new Message()
								.withBody(new Body()
								.withText(new Content().withCharset("UTF-8").withData(TEXTBODY)))
								.withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
						.withSource(FROM);
				
				eclient.sendEmail(emailRequest);
				
				res.setStatus("200");
				res.setMsg("Mail sent successfully..");
				res.setClients(null);
				
				return res;
				                
			} catch (Exception e) {
				res.setStatus("404");
				res.setMsg("Mail not sent");
				res.setClients(null);
				
				return res;
			}
			
		}

		case "CONFIRMDELETE"	:{
			
			String auth_code = request.getConfirmation_code();
			String email = request.getClient_email();
			String admin_email = request.getEmail();
			String poolID = "ap-south-1_tKm1HEzWU";
			
			try {
				
				DynamoDBMapper mapper = new DynamoDBMapper(dbclient);
				ClientData retriveData = mapper.load(ClientData.class, admin_email);
				
				String actual_code = retriveData.getAuth_code();
				
				if(actual_code.equals(auth_code)) {
				
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
				}else {
					res.setStatus("400");
					res.setMsg("invalid code..");
					res.setClients(null);
					return res;
				}
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
