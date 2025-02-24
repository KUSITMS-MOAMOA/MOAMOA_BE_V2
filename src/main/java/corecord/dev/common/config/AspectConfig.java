package corecord.dev.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration(proxyBeanMethods = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AspectConfig {
}
