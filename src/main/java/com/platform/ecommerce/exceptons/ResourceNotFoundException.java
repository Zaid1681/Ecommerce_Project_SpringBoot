package com.platform.ecommerce.exceptons;

public class ResourceNotFoundException extends  RuntimeException{
    public ResourceNotFoundException(String message){
        super(message);
    }
}
