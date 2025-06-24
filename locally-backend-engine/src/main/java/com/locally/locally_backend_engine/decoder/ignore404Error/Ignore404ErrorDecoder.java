package com.locally.locally_backend_engine.decoder.ignore404Error;

import feign.Response;
import feign.codec.ErrorDecoder;

public class Ignore404ErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        // Just return null for 404 → Feign returns the normal object
        if (response.status() == 404) {
            return null;
        }
        return defaultDecoder.decode(methodKey, response); // anything else = real error
    }
}