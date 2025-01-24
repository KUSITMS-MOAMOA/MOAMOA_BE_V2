package corecord.dev.common.log.discord;

import corecord.dev.common.log.discord.dto.DiscordDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Component
public class DiscordUtil {
    public DiscordDto.MessageDto createMessage(Exception exception, HttpServletRequest httpServletRequest) {
        return DiscordDto.MessageDto.builder()
                .content("# 🚨 서버 에러 발생 🚨")
                .embeds(List.of(DiscordDto.EmbedDto.builder()
                                .title("에러 정보")
                                .description("### 에러 발생 시간\n"
                                        + ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분 ss초"))
                                        + "\n"
                                        + "### 요청 엔드포인트\n"
                                        + getEndPoint(httpServletRequest)
                                        + "\n"
                                        + "### 요청 클라이언트\n"
                                        + getClient(httpServletRequest)
                                        +"\n"
                                        + "### 에러 스택 트레이스\n"
                                        + "```\n"
                                        + getStackTrace(exception).substring(0, 1000)
                                        + "\n```")
                                .build()
                        )
                ).build();
    }

    private String getClient(HttpServletRequest httpServletRequest) {
        String ip = httpServletRequest.getRemoteAddr();

        Principal principal = httpServletRequest.getUserPrincipal();
        if (principal != null) {
            return "[IP] : " + ip + " / [Id] : " + principal.getName();
        }
        return "[IP] : " + ip;
    }

    private String getEndPoint(HttpServletRequest httpServletRequest) {
        String method = httpServletRequest.getMethod();
        String url = httpServletRequest.getRequestURI();
        return method + " " + url;
    }

    private String getStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
