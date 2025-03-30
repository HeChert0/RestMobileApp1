package app.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Логируем вход и выход из всех методов в пакетах controller, service, dao
    @Pointcut("within(app.controller..*) || within(app.service..*) || within(app.dao..*)")
    public void applicationPackagePointcut() {}

    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("Entering: {} with arguments = {}",
                joinPoint.getSignature().toShortString(), joinPoint.getArgs());
        try {
            Object result = joinPoint.proceed();
            logger.info("Exiting: {} with result = {}",
                    joinPoint.getSignature().toShortString(), result);
            return result;
        } catch (IllegalArgumentException e) {
            logger.error("Illegal argument: {} in {}",
                    joinPoint.getArgs(), joinPoint.getSignature().toShortString());
            throw e;
        }
    }

    // Логируем исключения
    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        logger.error("Exception in {} with cause = {}", joinPoint.getSignature().toShortString(),
                (error.getCause() != null ? error.getCause() : "NULL"), error);
    }
}
