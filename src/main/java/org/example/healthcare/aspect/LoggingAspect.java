package org.example.healthcare.aspect;

import org.example.healthcare.aspect.annotation.LogAppointment;
import org.example.healthcare.aspect.annotation.LogDoctor;
import org.example.healthcare.aspect.annotation.LogPrescription;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // ==================== APPOINTMENT ====================

    @Around("@annotation(logAppointment)")
    public Object logAppointmentAction(ProceedingJoinPoint joinPoint, LogAppointment logAppointment) throws Throwable {
        String action = logAppointment.action();
        Object[] args = joinPoint.getArgs();

        log.info("[APPOINTMENT] Attempting to {} | Args: {}", action, args);

        try {
            Object result = joinPoint.proceed();
            log.info("[APPOINTMENT] {} successful | Result: {}", action, result);
            return result;
        } catch (Exception e) {
            log.error("[APPOINTMENT] {} failed | Error: {}", action, e.getMessage());
            throw e;
        }
    }

    // ==================== PRESCRIPTION ====================

    @Around("@annotation(logPrescription)")
    public Object logPrescriptionAction(ProceedingJoinPoint joinPoint, LogPrescription logPrescription) throws Throwable {
        String action = logPrescription.action();
        Object[] args = joinPoint.getArgs();

        log.info("[PRESCRIPTION] Attempting to {} | Args: {}", action, args);

        try {
            Object result = joinPoint.proceed();
            log.info("[PRESCRIPTION] {} successful | Result: {}", action, result);
            return result;
        } catch (Exception e) {
            log.error("[PRESCRIPTION] {} failed | Error: {}", action, e.getMessage());
            throw e;
        }
    }

    // ==================== DOCTOR ====================

    @Around("@annotation(logDoctor)")
    public Object logDoctorAction(ProceedingJoinPoint joinPoint, LogDoctor logDoctor) throws Throwable {
        String action = logDoctor.action();
        String cacheAction = logDoctor.cacheAction();
        Object[] args = joinPoint.getArgs();

        // Log cache context BEFORE executing
        if ("MISS".equals(cacheAction)) {
            log.info("[DOCTOR] [CACHE MISS] {} — fetching from database | Args: {}", action, args);
        } else if ("EVICT".equals(cacheAction)) {
            log.info("[DOCTOR] [CACHE EVICT] {} — cache will be cleared | Args: {}", action, args);
        } else {
            log.info("[DOCTOR] Attempting to {} | Args: {}", action, args);
        }

        try {
            Object result = joinPoint.proceed();
            log.info("[DOCTOR] {} successful | Result: {}", action, result);
            return result;
        } catch (Exception e) {
            log.error("[DOCTOR] {} failed | Error: {}", action, e.getMessage());
            throw e;
        }
    }
    // ==================== PERFORMANCE (All Services) ====================

    @Around("execution(* org.example.healthcare.service.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long duration = System.currentTimeMillis() - start;

        if (duration > 500) {
            log.warn("[PERFORMANCE] {}.{}() took {}ms (SLOW)",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    duration);
        } else {
            log.debug("[PERFORMANCE] {}.{}() took {}ms",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    duration);
        }

        return result;
    }
}