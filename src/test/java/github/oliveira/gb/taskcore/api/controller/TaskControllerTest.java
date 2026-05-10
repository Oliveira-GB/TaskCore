package github.oliveira.gb.taskcore.api.controller;

import github.oliveira.gb.taskcore.api.dto.request.TaskFilter;
import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.api.exception.TaskNotFoundException;
import github.oliveira.gb.taskcore.domain.exception.BusinessRuleException;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import github.oliveira.gb.taskcore.domain.service.TaskService;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    private TaskRequestDTO taskRequestDTO;
    private TaskResponseDTO taskResponseDTO;
    private final String baseUrl = "/api/v1/tasks";

    @BeforeEach
    void setUp() {
        taskRequestDTO = new TaskRequestDTO(
                "Study Spring Boot",
                "Review Swagger annotations and apply to controller",
                LocalDateTime.now().plusDays(2),
                Collections.emptyList(),
                Collections.emptySet()
        );

        taskResponseDTO = new TaskResponseDTO(
                1L,
                "Study Spring Boot",
                "Review Swagger annotations and apply to controller",
                TaskStatus.PENDING,
                LocalDateTime.now().plusDays(2),
                Instant.now(),
                Instant.now(),
                Collections.emptyList(),
                Collections.emptySet()
        );
    }

    @Test
    @DisplayName("Should load context and inject MockMvc successfully")
    void sanityCheck() {
        Assertions.assertThat(mockMvc).isNotNull();
        Assertions.assertThat(taskService).isNotNull();
        Assertions.assertThat(objectMapper).isNotNull();
    }

    @Test
    @DisplayName("Happy Path: Should create a task successfully and return 201 Created")
    void shouldCreateTaskSuccessfully() throws Exception {
        // Setup: Mock the service behavior using BDDMockito
        given(taskService.createTask(any(TaskRequestDTO.class)))
                .willReturn(taskResponseDTO);

        // Action & Assertions: Perform POST and validate responses
        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.endsWith("/" + taskResponseDTO.id())))
                .andExpect(jsonPath("$.id").value(taskResponseDTO.id()))
                .andExpect(jsonPath("$.title").value(taskResponseDTO.title()))
                .andExpect(jsonPath("$.status").value(taskResponseDTO.status().name()));

        // Verification: Ensure the service was called exactly once using BDDMockito
        then(taskService).should(times(1)).createTask(any(TaskRequestDTO.class));
    }

    @Test
    @DisplayName("Validation Error: Should return 400 Bad Request when request data is invalid")
    void shouldReturn400WhenRequestDataIsInvalid() throws Exception {
        // Setup: Create an invalid request DTO (blank title and past due date)
        TaskRequestDTO invalidRequest = new TaskRequestDTO(
                "",
                "Description with invalid blank title and past date",
                LocalDateTime.now().minusDays(1),
                Collections.emptyList(),
                Collections.emptySet()
        );

        // Action & Assertions: Perform POST and expect 400 Bad Request
        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Verification: Ensure the service was NEVER called due to validation failure
        then(taskService).should(never()).createTask(any(TaskRequestDTO.class));
    }

    @Test
    @DisplayName("Business Rule Error: Should return 422 Unprocessable Entity when service throws BusinessRuleException")
    void shouldReturn422WhenBusinessRuleIsViolated() throws Exception {
        // Setup: Mock the service to throw a BusinessRuleException
        String errorMessage = "Task with this title already exists";
        given(taskService.createTask(any(TaskRequestDTO.class)))
                .willThrow(new BusinessRuleException(errorMessage));

        // Action & Assertions: Perform POST and expect 422 Unprocessable Entity
        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequestDTO)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.messages[0]").value(errorMessage));

        // Verification: Ensure the service was called exactly once before throwing the exception
        then(taskService).should(times(1)).createTask(any(TaskRequestDTO.class));
    }

    @Test
    @DisplayName("Happy Path: Should return 200 OK when task exists")
    void shouldReturn200WhenTaskExists() throws Exception {
        // Setup: Mock the service to return the existing task
        Long taskId = 1L;
        given(taskService.taskFindById(taskId)).willReturn(taskResponseDTO);

        // Action & Assertions: Perform GET and validate response
        mockMvc.perform(get(baseUrl + "/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskResponseDTO.id()))
                .andExpect(jsonPath("$.title").value(taskResponseDTO.title()))
                .andExpect(jsonPath("$.status").value(taskResponseDTO.status().name()));

        // Verification: Ensure the service was called once
        then(taskService).should(times(1)).taskFindById(taskId);
    }

    @Test
    @DisplayName("Not Found Error: Should return 404 Not Found when task does not exist")
    void shouldReturn404WhenTaskDoesNotExist() throws Exception {
        // Setup: Mock the service to throw TaskNotFoundException
        Long taskId = 999L;
        String errorMessage = "Task not found with ID: " + taskId;
        given(taskService.taskFindById(taskId)).willThrow(new TaskNotFoundException(errorMessage));

        // Action & Assertions: Perform GET and expect 404 Not Found
        mockMvc.perform(get(baseUrl + "/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]").value(errorMessage));

        // Verification: Ensure the service was called once
        then(taskService).should(times(1)).taskFindById(taskId);
    }

    @Test
    @DisplayName("Happy Path: Should update a task successfully and return 200 OK")
    void shouldUpdateTaskSuccessfully() throws Exception {
        // Setup: Mock the service to return the updated task
        Long taskId = 1L;
        given(taskService.updateTask(eq(taskId), any(TaskRequestDTO.class)))
                .willReturn(taskResponseDTO);

        // Action & Assertions: Perform PUT and validate response
        mockMvc.perform(put(baseUrl + "/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskResponseDTO.id()))
                .andExpect(jsonPath("$.title").value(taskResponseDTO.title()))
                .andExpect(jsonPath("$.status").value(taskResponseDTO.status().name()));

        // Verification: Ensure the service was called once
        then(taskService).should(times(1)).updateTask(eq(taskId), any(TaskRequestDTO.class));
    }

    @Test
    @DisplayName("Validation Error: Should return 400 Bad Request when update data is invalid")
    void shouldReturn400WhenUpdateDataIsInvalid() throws Exception {
        // Setup: Create an invalid request DTO
        Long taskId = 1L;
        TaskRequestDTO invalidRequest = new TaskRequestDTO(
                "",
                "Invalid data",
                LocalDateTime.now().minusDays(1),
                Collections.emptyList(),
                Collections.emptySet()
        );

        // Action & Assertions: Perform PUT and expect 400 Bad Request
        mockMvc.perform(put(baseUrl + "/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Verification: Ensure the service was NEVER called
        then(taskService).should(never()).updateTask(anyLong(), any(TaskRequestDTO.class));
    }

    @Test
    @DisplayName("Not Found Error: Should return 404 Not Found when updating non-existent task")
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        // Setup: Mock the service to throw TaskNotFoundException
        Long taskId = 999L;
        String errorMessage = "Task not found with ID: " + taskId;
        given(taskService.updateTask(eq(taskId), any(TaskRequestDTO.class)))
                .willThrow(new TaskNotFoundException(errorMessage));

        // Action & Assertions: Perform PUT and expect 404 Not Found
        mockMvc.perform(put(baseUrl + "/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]").value(errorMessage));

        // Verification: Ensure the service was called once
        then(taskService).should(times(1)).updateTask(eq(taskId), any(TaskRequestDTO.class));
    }

    @Test
    @DisplayName("Business Rule Error: Should return 422 Unprocessable Entity when business rule is violated on update")
    void shouldReturn422WhenUpdatingViolatesBusinessRule() throws Exception {
        // Setup: Mock the service to throw a BusinessRuleException
        Long taskId = 1L;
        String errorMessage = "Cannot update a completed task";
        given(taskService.updateTask(eq(taskId), any(TaskRequestDTO.class)))
                .willThrow(new BusinessRuleException(errorMessage));

        // Action & Assertions: Perform PUT and expect 422 Unprocessable Entity
        mockMvc.perform(put(baseUrl + "/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequestDTO)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.messages[0]").value(errorMessage));

        // Verification: Ensure the service was called once
        then(taskService).should(times(1)).updateTask(eq(taskId), any(TaskRequestDTO.class));
    }

    @Test
    @DisplayName("Happy Path: Should delete a task successfully and return 204 No Content")
    void shouldDeleteTaskSuccessfully() throws Exception {
        // Setup: Mock the service to do nothing (void method) when a valid ID is passed
        Long taskId = 1L;
        willDoNothing().given(taskService).deleteTask(taskId);

        // Action & Assertions: Perform DELETE and expect 204 No Content
        mockMvc.perform(delete(baseUrl + "/{id}", taskId))
                .andExpect(status().isNoContent());

        // Verification: Ensure the service was called exactly once
        then(taskService).should(times(1)).deleteTask(taskId);
    }

    @Test
    @DisplayName("Not Found Error: Should return 404 Not Found when deleting non-existent task")
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        // Setup: Mock the service to throw TaskNotFoundException for a void method
        Long taskId = 999L;
        String errorMessage = "Task not found with ID: " + taskId;
        willThrow(new TaskNotFoundException(errorMessage)).given(taskService).deleteTask(taskId);

        // Action & Assertions: Perform DELETE and expect 404 Not Found
        mockMvc.perform(delete(baseUrl + "/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]").value(errorMessage));

        // Verification: Ensure the service was called exactly once
        then(taskService).should(times(1)).deleteTask(taskId);
    }

    @Test
    @DisplayName("Paginated Search: Should return 200 OK and a page of tasks based on filters")
    void shouldReturn200AndPageOfTasks() throws Exception {
        // Setup: Create a mock page containing our response DTO
        Page<TaskResponseDTO> taskPage = new PageImpl<>(List.of(taskResponseDTO));

        // Mock the service behavior to return the page when findAll is called
        given(taskService.findAll(any(TaskFilter.class), any(Pageable.class)))
                .willReturn(taskPage);

        // Action & Assertions: Perform GET with query params for pagination and filtering
        mockMvc.perform(get(baseUrl)
                        .param("page", "0")
                        .param("size", "10")
                        .param("text", "Spring")
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(taskResponseDTO.id()))
                .andExpect(jsonPath("$.content[0].title").value(taskResponseDTO.title()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        // Verification: Ensure the service was called exactly once with the filters and pageable
        then(taskService).should(times(1)).findAll(any(TaskFilter.class), any(Pageable.class));
    }
}