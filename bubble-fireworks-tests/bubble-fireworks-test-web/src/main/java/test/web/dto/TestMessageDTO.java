package test.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * TestMessageDTO
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/16 23:52
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestMessageDTO implements Serializable {

    private static final long serialVersionUID = 1874791859019775820L;


    @NotNull(message = "{id.not.null}")
    private Integer id;
}
