package com.abc.product.app.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Abhishek on 3/18/2018.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZipCodeResponse extends BaseResponse{
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
