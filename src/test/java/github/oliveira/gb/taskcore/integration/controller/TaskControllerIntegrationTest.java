package github.oliveira.gb.taskcore.integration.controller;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import github.oliveira.gb.taskcore.domain.repository.TaskRepository;
import github.oliveira.gb.taskcore.integration.IntegrationTestBase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TaskControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    private TaskRequestDTO validRequest;
    private final String baseUrl = "/api/v1/tasks";

    @BeforeEach
    void setUp() {
        validRequest = new TaskRequestDTO(
                "Study Spring Boot",
                "Review Swagger annotations and apply to controller",
                LocalDateTime.now().plusDays(2),
                Collections.emptyList(),
                Collections.emptySet()
        );
    }

    @Test
    @DisplayName("E2E: Should create task with valid payload and return 201 Created")
    void shouldCreateTaskSuccessfully() throws Exception {
        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Study Spring Boot"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 400 Bad Request for blank title")
    void shouldReturn400ForBlankTitle() throws Exception {
        TaskRequestDTO invalidRequest = new TaskRequestDTO(
                "",
                "Description",
                LocalDateTime.now().plusDays(2),
                Collections.emptyList(),
                Collections.emptySet()
        );

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 400 Bad Request for past due date")
    void shouldReturn400ForPastDueDate() throws Exception {
        TaskRequestDTO invalidRequest = new TaskRequestDTO(
                "Valid Title",
                "Description",
                LocalDateTime.now().minusDays(1),
                Collections.emptyList(),
                Collections.emptySet()
        );

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 400 Bad Request for title too short")
    void shouldReturn400ForTitleTooShort() throws Exception {
        TaskRequestDTO invalidRequest = new TaskRequestDTO(
                "AB",
                "Description",
                LocalDateTime.now().plusDays(2),
                Collections.emptyList(),
                Collections.emptySet()
        );

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should find task by ID and return 200 OK")
    void shouldFindTaskById() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO(
                "Find Me",
                "Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet()
        );

        MvcResult createResult = mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = extractIdFromLocation(createResult.getResponse().getHeader("Location"));

        mockMvc.perform(get(baseUrl + "/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Find Me"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 404 Not Found for non-existent task")
    void shouldReturn404ForNonExistentTask() throws Exception {
        mockMvc.perform(get(baseUrl + "/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should update task and return 200 OK")
    void shouldUpdateTaskSuccessfully() throws Exception {
        TaskRequestDTO createRequest = new TaskRequestDTO(
                "Original Title",
                "Original Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet()
        );

        MvcResult createResult = mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = extractIdFromLocation(createResult.getResponse().getHeader("Location"));

        TaskRequestDTO updateRequest = new TaskRequestDTO(
                "Updated Title",
                "Updated Description",
                LocalDateTime.now().plusDays(10),
                Collections.emptyList(),
                Collections.emptySet()
        );

        mockMvc.perform(put(baseUrl + "/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 404 Not Found when updating non-existent task")
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        TaskRequestDTO updateRequest = new TaskRequestDTO(
                "Updated Title",
                "Updated Description",
                LocalDateTime.now().plusDays(10),
                Collections.emptyList(),
                Collections.emptySet()
        );

        mockMvc.perform(put(baseUrl + "/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should delete task and return 204 No Content")
    void shouldDeleteTaskSuccessfully() throws Exception {
        TaskRequestDTO createRequest = new TaskRequestDTO(
                "To Delete",
                "Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet()
        );

        MvcResult createResult = mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = extractIdFromLocation(createResult.getResponse().getHeader("Location"));

        mockMvc.perform(delete(baseUrl + "/{id}", taskId))
                .andExpect(status().isNoContent())
                .andDo(print());

        Optional<Task> deletedTask = taskRepository.findById(taskId);
        Assertions.assertThat(deletedTask).isEmpty();
    }

    @Test
    @DisplayName("E2E: Should return 404 Not Found when deleting non-existent task")
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        mockMvc.perform(delete(baseUrl + "/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return paginated tasks with 200 OK")
    void shouldReturnPaginatedTasks() throws Exception {
        for (int i = 0; i < 3; i++) {
            TaskRequestDTO request = new TaskRequestDTO(
                    "Task " + i,
                    "Description " + i,
                    LocalDateTime.now().plusDays(i + 1),
                    Collections.emptyList(),
                    Collections.emptySet()
            );
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get(baseUrl)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andDo(print());
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Long extractIdFromLocation(String location) {
        String id = location.substring(location.lastIndexOf("/") + 1);
        return Long.parseLong(id);
    }
}