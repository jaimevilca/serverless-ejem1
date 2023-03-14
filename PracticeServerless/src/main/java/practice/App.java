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

    private final ReviewRepository reviewRepository = new ReviewRepository();

    private final ObjectMapper mapper = new ObjectMapper();

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        switch (event.getHttpMethod()) {
            case "GET":
                if (event.getPathParameters() != null && event.getPathParameters().containsKey("bookid")) {
                    String bookId = event.getPathParameters().get("bookid");
                    return getBookById(bookId);
                } else {
                    return getAllBooks();
                }
            case "POST":
                return addBookOrComment(event.getPathParameters().get("bookid"),event.getBody());
            case "PUT":
                return updateBook(event.getPathParameters().get("bookid"), event.getBody());
            case "DELETE":
                return deleteBook(event.getPathParameters().get("bookid"));
            default:
                return createResponse(400, "Unsupported method " + event.getHttpMethod());
        }
    }

    private APIGatewayProxyResponseEvent getBookById(String bookid) {

        try {
            Map<String, Object>  res = bookRepository.getBookById(bookid);

            String responseBody = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(res);

            return createResponse(200, responseBody);
        } catch (Exception e) {
            System.out.println(e);
            return createResponse(500, e.getMessage());
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

    private APIGatewayProxyResponseEvent addBookOrComment(String bookId, String data) {

        try {
            Map<String,Object> requestMap = mapper.readValue(data, new TypeReference<HashMap<String, Object>>() {});

            if (requestMap.containsKey("title") && requestMap.containsKey("author") && requestMap.containsKey("resume")) {
                // Si se han proporcionado los datos de un libro, agrega el libro
                Item item = bookRepository.addBook(
                        (String)user.requestMap("title"),
                        (String)user.requestMap("resume"),
                        (String)user.requestMap("author"),
                        (String)user.requestMap("publishing"),
                        (String)user.requestMap("year"));


                String id = (String) item.get("bookid");

                String responseBody = mapper.writeValueAsString(id);
                return createResponse(201, responseBody);

            } else if (requestMap.containsKey("user") && requestMap.containsKey("text")) {
                // Si se han proporcionado los datos de un comentario, agrega el comentario al libro con el ID bookId
                // Aquí puedes agregar el comentario al libro con el ID bookId
                Item item = reviewRepository.addReview(
                        (String)requestMap.get("bookid"),
                        (String)requestMap.get("user"),
                        (String)requestMap.get("text"),
                        (String)requestMap.get("punctuation") );

                // Devuelve una respuesta con el código de estado 201 (creado)
                String id = (String) item.get("bookid");

                String responseBody = mapper.writeValueAsString(id);
                return createResponse(201, responseBody);
            } else {
                // Si no se han proporcionado los datos necesarios, devuelve una respuesta con el código de estado 400 (solicitud incorrecta)
                APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
                response.setStatusCode(400);
                response.setBody("Bad request: missing required parameters");
                return response;
            }

        } catch (Exception e) {
            System.out.println(e);
            return createResponse(500, e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent updateBook(String userid, String data) {

        try {

            Map<String,Object> user = mapper.readValue(data, new TypeReference<HashMap<String, Object>>() {});

            UpdateItemOutcome res = bookRepository.updateBook(
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
            DeleteItemOutcome res = bookRepository.deleteBook(bookid);
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
