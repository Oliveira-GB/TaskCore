package github.oliveira.gb.taskcore.api.exception;

import github.oliveira.gb.taskcore.domain.exception.BusinessRuleException;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    @DisplayName("Caminho Feliz: Deve mapear BusinessRuleException para 422 Unprocessable Content")
    void shouldHandleBusinessRuleException() {
        String errorMessage = "Invalid business rule";
        String requestUri = "/api/v1/tasks";
        BusinessRuleException exception = new BusinessRuleException(errorMessage);

        given(request.getRequestURI()).willReturn(requestUri);

        ResponseEntity<ErrorResponseDTO> response = handler.handleBusinessRuleException(exception, request);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY); 

        ErrorResponseDTO body = response.getBody();
        Assertions.assertThat(body).isNotNull();
        Assertions.assertThat(body.status()).isEqualTo(422); 
        Assertions.assertThat(body.error()).isEqualTo("Unprocessable Entity"); 
        Assertions.assertThat(body.messages()).containsExactly(errorMessage); 
        Assertions.assertThat(body.path()).isEqualTo(requestUri); 
        Assertions.assertThat(body.timestamp()).isNotNull(); 
    }

    @Test
    @DisplayName("Caminho Feliz: Deve mapear TaskNotFoundException para 404 Not Found")
    void shouldHandleTaskNotFoundException() {
        String errorMessage = "Task with ID 99 not found";
        String requestUri = "/api/v1/tasks/99";
        TaskNotFoundException exception = new TaskNotFoundException(errorMessage); 

        given(request.getRequestURI()).willReturn(requestUri);

        ResponseEntity<ErrorResponseDTO> response = handler.handleTaskNotFoundException(exception, request); 

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND); 

        ErrorResponseDTO body = response.getBody();
        Assertions.assertThat(body).isNotNull();
        Assertions.assertThat(body.status()).isEqualTo(404); 
        Assertions.assertThat(body.error()).isEqualTo("Task not found!"); 
        Assertions.assertThat(body.messages()).containsExactly(errorMessage); 
        Assertions.assertThat(body.path()).isEqualTo(requestUri); 
        Assertions.assertThat(body.timestamp()).isNotNull(); 
    }

    @Test
    @DisplayName("Caminho Feliz: Deve mapear MethodArgumentNotValidException para 400 Bad Request com multiplos erros")
    void shouldHandleMethodArgumentNotValidException() {
        String requestUri = "/api/v1/tasks";
        org.springframework.validation.BindingResult bindingResult = org.mockito.Mockito.mock(org.springframework.validation.BindingResult.class);

        org.springframework.validation.FieldError error1 = new org.springframework.validation.FieldError(
                "taskRequestDTO",
                "title",
                "It is not possible to create a task without a TITLE"
        );
        org.springframework.validation.FieldError error2 = new org.springframework.validation.FieldError(
                "taskRequestDTO",
                "dueDate",
                "Every date must have a due date in the present or future!"
        );

        given(bindingResult.getFieldErrors()).willReturn(java.util.List.of(error1, error2));

        org.springframework.web.bind.MethodArgumentNotValidException exception = org.mockito.Mockito.mock(org.springframework.web.bind.MethodArgumentNotValidException.class);
        given(exception.getBindingResult()).willReturn(bindingResult);
        given(request.getRequestURI()).willReturn(requestUri);

        ResponseEntity<ErrorResponseDTO> response = handler.handleMethodArgumentNotValidException(exception, request);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ErrorResponseDTO body = response.getBody();
        Assertions.assertThat(body).isNotNull();
        Assertions.assertThat(body.status()).isEqualTo(400);
        Assertions.assertThat(body.error()).isEqualTo("Bad Request");
        Assertions.assertThat(body.messages()).containsExactly(
                "title: It is not possible to create a task without a TITLE",
                "dueDate: Every date must have a due date in the present or future!"
        );
        Assertions.assertThat(body.path()).isEqualTo(requestUri);
        Assertions.assertThat(body.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Caminho Feliz: Deve mapear MethodArgumentTypeMismatchException para 400 Bad Request")
    void shouldHandleMethodArgumentTypeMismatchException() {
        String requestUri = "/api/v1/tasks";
        String parameterName = "deadline";
        String invalidValue = "INVALID_FILTER";

        MethodArgumentTypeMismatchException exception = org.mockito.Mockito.mock(MethodArgumentTypeMismatchException.class);
        given(exception.getName()).willReturn(parameterName);
        given(exception.getValue()).willReturn(invalidValue);
        given(exception.getRequiredType()).willReturn(null);
        given(request.getRequestURI()).willReturn(requestUri);

        ResponseEntity<ErrorResponseDTO> response = handler.handleMethodArgumentTypeMismatchException(exception, request);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ErrorResponseDTO body = response.getBody();
        Assertions.assertThat(body).isNotNull();
        Assertions.assertThat(body.status()).isEqualTo(400);
        Assertions.assertThat(body.error()).isEqualTo("Bad Request");
        Assertions.assertThat(body.messages().get(0)).contains("Invalid value for deadline");
        Assertions.assertThat(body.path()).isEqualTo(requestUri);
        Assertions.assertThat(body.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve incluir valores permitidos no erro quando o tipo for enum")
    void shouldIncludeEnumValuesInErrorMessage() {
        String requestUri = "/api/v1/tasks";
        String parameterName = "deadline";
        String invalidValue = "INVALID";

        MethodArgumentTypeMismatchException exception = org.mockito.Mockito.mock(MethodArgumentTypeMismatchException.class);
        given(exception.getName()).willReturn(parameterName);
        given(exception.getValue()).willReturn(invalidValue);
        org.mockito.Mockito.<Class<?>>when(exception.getRequiredType()).thenReturn(github.oliveira.gb.taskcore.domain.model.DeadlineFilter.class);
        given(request.getRequestURI()).willReturn(requestUri);

        ResponseEntity<ErrorResponseDTO> response = handler.handleMethodArgumentTypeMismatchException(exception, request);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ErrorResponseDTO body = response.getBody();
        Assertions.assertThat(body).isNotNull();
        Assertions.assertThat(body.messages().get(0)).contains("OVERDUE", "TODAY", "THIS_WEEK");
    }
}