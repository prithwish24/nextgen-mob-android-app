package com.abc.product.app.bo;

/**
 * Created by Abhishek on 3/18/2018.
 */

public class ZipCodeResponse {
    private String id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String businessName;
    private String zipcode ;
    private String phone;
    private boolean pickupFromNearestLocation;
    private String sessionId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
