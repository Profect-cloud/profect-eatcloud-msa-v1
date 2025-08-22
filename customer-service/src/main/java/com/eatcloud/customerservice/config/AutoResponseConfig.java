package com.eatcloud.customerservice.config;

import org.springframework.context.annotation.Configuration;
import com.eatcloud.autoresponse.annotation.EnableAutoResponse;

@Configuration
@EnableAutoResponse //  AutoResponseConfiguration를 @Import 하며, ExceptionHandler를 컴포넌트 스캔 등록
public class AutoResponseConfig {

}
