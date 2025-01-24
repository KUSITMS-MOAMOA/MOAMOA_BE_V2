package corecord.dev.common.log.discord;

import corecord.dev.common.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DiscordLoggerAop {

    private final DiscordAlarmSender discordAlarmSender;

    @Pointcut("execution(* corecord.dev.common.exception.GeneralExceptionAdvice..*(..))")
    public void generalExceptionErrorLoggerExecute() {}

    @Before("generalExceptionErrorLoggerExecute()")
    public void serverErrorLogging(JoinPoint joinpoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Object[] args = joinpoint.getArgs();

        if (args[0] instanceof GeneralException exception) {
            if (exception.getErrorStatus().getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR)
                discordAlarmSender.sendDiscordAlarm(exception, request);
        } else {
            Exception exception = (Exception) args[0];
            discordAlarmSender.sendDiscordAlarm(exception, request);
        }
    }
}
