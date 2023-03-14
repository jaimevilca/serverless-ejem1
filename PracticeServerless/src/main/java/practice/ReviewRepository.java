package practice;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.UUID;

public class ReviewRepository {
    private static final String TABLE_NAME = "reviews";
    private final Table table;
    private AmazonDynamoDB dynamoDB;

    public ReviewRepository() {

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

    public ScanResult getAllReviews() {

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(TABLE_NAME);

        return dynamoDB.scan(scanRequest);
    }

    public Item addReview(String user, String text, String punctuation , String bookId) {
        Item item = new Item()
                .withPrimaryKey("reviewid", UUID.randomUUID().toString())
                .withString("bookId", bookId)
                .withString("user", user)
                .withString("text", text)
                .withString("punctuation", punctuation);
        table.putItem(item);

        return item;
    }

    public UpdateItemOutcome updateReview(String reviewid, String user, String text, String punctuation) {
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("reviewid", reviewid)
                .withUpdateExpression("set #ti = :t, text = :r, punctuation = :a, publishing = :p, year = :y")
                .withNameMap(new NameMap().with("#ti", "user"))
                .withValueMap(new ValueMap()
                        .withString(":t", user)
                        .withString(":r", text)
                        .withString(":a", punctuation))
                .withReturnValues("ALL_OLD");
        return table.updateItem(updateItemSpec);
    }

    public DeleteItemOutcome deleteReview(String reviewid) {
        DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey("reviewid", reviewid)
                .withReturnValues(ReturnValue.ALL_OLD);
        return table.deleteItem(deleteItemSpec);
    }

}

