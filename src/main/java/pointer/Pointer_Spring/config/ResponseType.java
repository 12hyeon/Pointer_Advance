package pointer.Pointer_Spring.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import pointer.Pointer_Spring.validation.ExceptionCode;

@Getter
public class ResponseType {
    private final Integer status;
    private final String code;
    private final String message;

    @JsonCreator
    public ResponseType(@JsonProperty("status") int status,
                        @JsonProperty("code") String code,
                        @JsonProperty("message") String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public ResponseType(ExceptionCode exceptionCode) {
        this.status = exceptionCode.getStatus().getValue();
        this.code = exceptionCode.getCode();
        this.message = exceptionCode.getMessage();
    }
}
