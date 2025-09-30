package dev.xiyo.pokerhole.core.application;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use Case를 나타내는 커스텀 애노테이션
 * Application 레이어의 Use Case 구현체에 사용됩니다.
 * 
 * @Service 애노테이션을 메타-애노테이션으로 포함하여
 * Spring 컴포넌트로 자동 등록됩니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface UseCase {
}
