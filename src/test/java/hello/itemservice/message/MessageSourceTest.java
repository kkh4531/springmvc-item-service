package hello.itemservice.message;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest // MessageSource는 스프링 빈에 등록돼 있기 때문에 빈을 사용해야하니 SpringBootTest를 써서 MessageSource 빈을 가져와야함.
public class MessageSourceTest {

    @Autowired
    MessageSource messageSource;

    @Test
    void helloMessage() {
        String message = messageSource.getMessage("hello", null, null);
        assertThat(message).isEqualTo("안녕");
    }

    @Test
    void notFoundMessageCode() {
        assertThatThrownBy(() -> messageSource.getMessage("no_code", null, null))
                .isInstanceOf(NoSuchMessageException.class);
    }

    @Test
    void notFoundMessageCodeDefaultMessage() {
        String result = messageSource.getMessage("no_code", null, "기본 메시지", null);
        assertThat(result).isEqualTo("기본 메시지");
    }

    @Test
    void argumentMessage() {
        String message = messageSource.getMessage("hello.name", new Object[]{"Spring"}, null);
        assertThat(message).isEqualTo("안녕 Spring");
    }

    @Test
    void defaultLang() { // 현재 application.properties의 spring.messages.basename=messages로 돼있음
        assertThat(messageSource.getMessage("hello", null, null)).isEqualTo("안녕");
        assertThat(messageSource.getMessage("hello", null, Locale.KOREA)).isEqualTo("안녕"); //message_ko.properties가 없기에 디폴트 파일에서 찾음.
    }

    @Test
    void enLang() { // 파라미터에 Locale.ENGLISH를 넘기게 되면 스프링은 자동으로 basename과 en이 조합된 파일을 찾게되어 해당파일의 언어를 가져옴. 만약 Locale.ENGLISH인데 파일이름을 ex.enn이라던가 지으면 오류가 난다. 이것은 관례를 따라야 함.
        assertThat(messageSource.getMessage("hello", null, Locale.ENGLISH)).isEqualTo("hello");
    }
}
