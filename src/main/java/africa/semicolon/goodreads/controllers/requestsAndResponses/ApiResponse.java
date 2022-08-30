package africa.semicolon.goodreads.controllers.requestsAndResponses;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiResponse {
    private String status;
    private String message;
    private Object data;
    private int result;
}
