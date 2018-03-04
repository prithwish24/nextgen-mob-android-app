package com.abc.product.app.service;

import android.util.Log;

import com.abc.product.app.bo.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Prithwish on 3/4/2018.
 */

public enum RestClient {
    INSTANCE;

    private final String TAG = RestClient.class.getName();
    private final int DEFAULT_SERVICE_TIMEOUT = 30 * 1000;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    RestClient() {
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory(){
            public void setConnectTimeout(int connectTimeout) {
                super.setConnectTimeout(DEFAULT_SERVICE_TIMEOUT);
            }
            public void setReadTimeout(int readTimeout) {
                super.setReadTimeout(DEFAULT_SERVICE_TIMEOUT);
            }
        });

        mapper = new ObjectMapper();
    }

    private BaseResponse makeRequestWithFormData(String url, MultiValueMap<String, String> formData, HttpMethod method) {
        Log.d(TAG,"In RestClient.makeRequestWithFormData ::");
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        //requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MultiValueMap> httpEntity = new HttpEntity<MultiValueMap>(formData, requestHeaders);
        ResponseEntity<BaseResponse> response = restTemplate.exchange(url, method, httpEntity, BaseResponse.class);
        return response.getBody();
    }


    public <T> BaseResponse<T> getRequest(String url, MultiValueMap<String, String> formData, Class<T> clazz) throws IOException {
        Log.d(TAG,"In RestServiceClient.getRequestForm ::");
        BaseResponse br = makeRequestWithFormData (url, formData, HttpMethod.GET);
        if (br.isSuccess()) {
            String temp = mapper.writeValueAsString(br.getResponse());
            T type = mapper.readValue(temp, clazz);
            br.setResponse(type);
        }
        return br;
    }
    public <T> BaseResponse<T>  postRequest(String url, MultiValueMap<String, String> formData, Class<T> clazz) throws IOException {
        Log.d(TAG,"In RestServiceClient.postRequestForm ::");
        BaseResponse br = makeRequestWithFormData (url, formData, HttpMethod.POST);
        if (br.isSuccess()) {
            String temp = mapper.writeValueAsString(br.getResponse());
            T type = mapper.readValue(temp, clazz);
            br.setResponse(type);
        }
        return br;
    }

}
