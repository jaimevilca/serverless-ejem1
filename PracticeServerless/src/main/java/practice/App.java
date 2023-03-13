package practice;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final BookRepository bookRepository = new BookRepository();

    private final ObjectMapper mapper = new ObjectMapper();

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        switch (event.getHttpMethod()) {
            case "GET":
                return getAllBooks();
            case "POST":
                return addBook(event.getBody());
            case "PUT":
                return updateBook(event.getPathParameters().get("bookid"), event.getBody());
            case "DELETE":
                return deleteBook(event.getPathParameters().get("bookid"));
            default:
                return createResponse(400, "Unsupported method " + event.getHttpMethod());
        }
    }

    private APIGatewayProxyResponseEvent getAllBooks() {

        try {
            ScanResult res = bookRepository.getAllBooks();

            List<Item> itemList = ItemUtils.toItemList(res.getItems());

            List<Map<String,Object>> values = new ArrayList<>();
            for(Item item: itemList){

                var value = new HashMap<String,Object>();
                value.put("bookid", item.get("bookid"));
                value.put("title", item.get("title"));
                value.put("resume", item.get("resume"));
                value.put("author", item.get("author"));
                value.put("publishing", item.get("publishing"));
                value.put("year", item.get("year"));
                values.add(value);
            }

            String responseBody = mapper.writeValueAsString(values);
            return createResponse(200, responseBody);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return createResponse(500, e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent addBook(String data) {

        try {
            System.out.println("Esto sale en pantalla\n");
            System.out.println(data + "\n");

            Map<String,Object> user = mapper.readValue(data, new TypeReference<HashMap<String, Object>>() {});

            Item item = bookRepository.addUser(
                    (String)user.get("title"),
                    (String)user.get("resume"),
                    (String)user.get("author"),
                    (String)user.get("publishing"),
                    (String)user.get("year"));


            String id = (String) item.get("bookid");

            String responseBody = mapper.writeValueAsString(id);

            return createResponse(201, responseBody);
        } catch (Exception e) {
            System.out.println(e);
            return createResponse(500, e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent updateBook(String userid, String data) {

        try {

            Map<String,Object> user = mapper.readValue(data, new TypeReference<HashMap<String, Object>>() {});

            UpdateItemOutcome res = bookRepository.updateUser(
                    (String)user.get("bookid"),
                    (String)user.get("title"),
                    (String)user.get("resume"),
                    (String)user.get("author"),
                    (String)user.get("publishing"),
                    (String)user.get("year"));

            String responseBody = mapper.writeValueAsString(res.getUpdateItemResult().getAttributes());
            return createResponse(200, responseBody);
        } catch (Exception e) {
            System.out.println(e);
            return createResponse(500, e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent deleteBook(String bookid) {

        try {
            DeleteItemOutcome res = bookRepository.deleteUser(bookid);
            String responseBody = mapper.writeValueAsString(res.getDeleteItemResult());
            return createResponse(200, responseBody);
        } catch (Exception e) {
            System.out.println(e);
            return createResponse(500, e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String responseBody) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(responseBody);
        return response;
    }
}
