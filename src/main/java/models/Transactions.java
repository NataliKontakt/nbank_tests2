package models;

public class Transactions extends BaseModel{
    private long id;
    private float amount;
    private String type;
    private String timestamp;
    private long relatedAccountId;
}
