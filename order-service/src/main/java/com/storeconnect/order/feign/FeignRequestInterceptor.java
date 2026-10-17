package com.storeconnect.order.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        template.header("X-Service-Name", "order-service");
        template.header("X-User-Role", "SERVICE");
    }
}
