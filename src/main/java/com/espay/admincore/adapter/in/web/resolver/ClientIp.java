package com.espay.admincore.adapter.in.web.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러의 문자열 매개변수에 실제 클라이언트 IP를 주입하도록 표시하는 입력 어댑터 애노테이션.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientIp {
}
