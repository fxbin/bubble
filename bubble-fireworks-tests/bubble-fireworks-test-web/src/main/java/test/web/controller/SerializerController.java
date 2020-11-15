package test.web.controller;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SerializerController
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/16 2:23
 */
@RestController
@RequestMapping("/serializer")
public class SerializerController {

    @GetMapping("/long")
    public LongTest getLong() {
        return new LongTest(Long.MAX_VALUE, "123456", Long.MIN_VALUE);
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class LongTest {
    private Long l;

    private String a;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long b;
}