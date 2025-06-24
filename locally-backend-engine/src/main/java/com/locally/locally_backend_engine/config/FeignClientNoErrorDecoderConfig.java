package com.locally.locally_backend_engine.config;

import com.locally.locally_backend_engine.decoder.ignore404Error.Ignore404ErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class FeignClientNoErrorDecoderConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new Ignore404ErrorDecoder();
    }        // This disables throwing exceptions on non-2xx
}