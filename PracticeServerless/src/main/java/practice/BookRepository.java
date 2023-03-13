package practice;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.UUID;

public class BookRepository {
    private static final String TABLE_NAME = "books";
    private final Table table;
    private AmazonDynamoDB dynamoDB;

    public BookRepository() {

        String dynamoDbEndpoint = System.getenv("DYNAMODB_ENDPOINT");

        if (dynamoDbEndpoint != null && !dynamoDbEndpoint.equals("")) {

            System.out.println("Local DynamoDB");
            System.out.println("DYNAMODB_ENDPOINT="+dynamoDbEndpoint);

            dynamoDB = AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(
                                    dynamoDbEndpoint, "us-east-1"))
                    .build();
        } else {
            System.out.println("AWS DynamoDB");
            dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
        }

        table = new DynamoDB(dynamoDB).getTable(TABLE_NAME);
    }

    public ScanResult getAllBooks() {

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(TABLE_NAME);

        return dynamoDB.scan(scanRequest);
    }

    public Item addUser(String title, String resume, String author, String publishing, String year) {
        Item item = new Item()
                .withPrimaryKey("bookid", UUID.randomUUID().toString())
                .withString("title", title)
                .withString("resume", resume)
                .withString("author", author)
                .withString("publishing", publishing)
                .withString("year", year);
        table.putItem(item);

        return item;
    }

    public UpdateItemOutcome updateUser(String bookid, String title, String resume, String author, String publishing, String year) {
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("bookid", bookid)
                .withUpdateExpression("set #ti = :t, resume = :r, author = :a, publishing = :p, year = :y")
                .withNameMap(new NameMap().with("#ti", "title"))
                .withValueMap(new ValueMap()
                        .withString(":t", title)
                        .withString(":r", resume)
                        .withString(":a", author)
                        .withString(":p", publishing)
                        .withString(":y", year))
                .withReturnValues("ALL_OLD");
        return table.updateItem(updateItemSpec);
    }

    public DeleteItemOutcome deleteUser(String bookid) {
        DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey("userid", bookid)
                //.withConditionExpression("userid = :userid")
                //.withValueMap(new ValueMap().withString(":userid", userid))
                .withReturnValues(ReturnValue.ALL_OLD);
        return table.deleteItem(deleteItemSpec);
    }

}

