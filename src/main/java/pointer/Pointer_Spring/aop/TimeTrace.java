package pointer.Pointer_Spring.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TimeTrace {

    // 특정 2개의 메소드에 대해서 시간 비교
/*    @Around("execution(* pointer.Pointer_Spring.user.service.UserServiceImpl.updateNmBatch(..)) " +
            "|| execution(* pointer.Pointer_Spring.user.service.UserServiceImpl.updateNm(..))")*/
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("Start: " + joinPoint.toString());
        try {
            return joinPoint.proceed(); // 다음 로직으로 넘어간다.
        } finally {
            long finish = System.currentTimeMillis();
            long timeMs = finish - start;
            log.info("End: " + joinPoint.toString() + " " + timeMs + "ms");
        }
    }
}
