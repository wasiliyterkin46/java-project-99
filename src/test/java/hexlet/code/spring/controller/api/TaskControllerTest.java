package hexlet.code.spring.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.spring.dto.task.TaskCreateDTO;
import hexlet.code.spring.dto.task.TaskDTO;
import hexlet.code.spring.mapper.TaskMainMapper;
import hexlet.code.spring.model.Label;
import hexlet.code.spring.model.Task;
import hexlet.code.spring.model.User;
import hexlet.code.spring.repository.LabelRepository;
import hexlet.code.spring.repository.TaskRepository;
import hexlet.code.spring.repository.TaskStatusRepository;
import hexlet.code.spring.repository.UserRepository;
import hexlet.code.spring.service.TaskService;
import hexlet.code.spring.util.ModelGenerator;
import hexlet.code.spring.util.TestUtils;
import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskControllerTest {
    private final WebApplicationContext wac;
    private final TaskMainMapper mapper;
    private final TaskRepository repository;
    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final ModelGenerator modelGenerator;
    private final ObjectMapper om;
    private final TaskService service;
    private final TestUtils testUtils;
    private final LabelRepository labelRepository;

    private MockMvc mockMvc;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private User testUser;
    private Task testTask;
    private Label testLabel;
    private final String basePath = "/api/tasks";

    @Autowired
    @SuppressWarnings("checkstyle:ParameterNumber")
    public TaskControllerTest(@NonNull final WebApplicationContext wacD, @NonNull  final TaskMainMapper mapperD,
                              @NonNull final TaskRepository repositoryD, @NonNull final UserRepository userRepositoryD,
                              @NonNull final TaskStatusRepository taskStatusRepositoryD,
                              @NonNull final ModelGenerator modelGeneratorD,
                              @NonNull final ObjectMapper omD, @NonNull final TaskService serviceD,
                              @NonNull final TestUtils testUtilsD, @NonNull final LabelRepository labelRepositoryD) {
        this.wac = wacD;
        this.mapper = mapperD;
        this.repository = repositoryD;
        this.userRepository = userRepositoryD;
        this.taskStatusRepository = taskStatusRepositoryD;
        this.modelGenerator = modelGeneratorD;
        this.om = omD;
        this.service = serviceD;
        this.testUtils = testUtilsD;
        this.labelRepository = labelRepositoryD;
    }

    @BeforeEach
    public void setUp() {
        testUtils.clearAllRepository();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac).defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity()).build();

        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        var taskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(taskStatus);
        testTask.setTaskStatus(taskStatus);

        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);
        testTask.addLabel(testLabel);

        repository.save(testTask);
    }

    @Test
    public void testCreateSuccess() throws Exception {
        var dtoRequest = getTaskCreateDTO();
        var request = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest));

        var responseBody = mockMvc.perform(request).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        TaskDTO dtoResponse = om.readValue(responseBody, new TypeReference<>() { });
        var dtoFromBase = service.findById(dtoResponse.getId());

        assertNotNull(dtoFromBase);
        assertEquals(dtoFromBase.getStatus(), dtoResponse.getStatus());
        assertEquals(dtoFromBase.getTitle(), dtoResponse.getTitle());
        assertEquals(dtoFromBase.getTaskLabelIds(), dtoResponse.getTaskLabelIds());
    }

    @Test
    public void testCreateFailture() throws Exception {
        // Case: Task.name = ""
        var dtoRequest1 = getTaskCreateDTO();
        dtoRequest1.setTitle("");
        var request1 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest1));
        mockMvc.perform(request1).andExpect(status().isBadRequest());

        // Case: Task.status = null
        var dtoRequest2 = getTaskCreateDTO();
        dtoRequest2.setStatus(null);
        var request2 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest2));
        mockMvc.perform(request2).andExpect(status().isBadRequest());

        // Case: Task.status = not existing
        var dtoRequest3 = getTaskCreateDTO();
        dtoRequest3.setStatus("zzz");
        var request3 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest3));
        mockMvc.perform(request3).andExpect(status().isNotFound());

        // Case: Task.user non-exist
        var dtoRequest4 = getTaskCreateDTO();
        var nonExistUser = testUtils.getNonExistentId(userRepository, User::getId);
        dtoRequest4.setAssigneeId(nonExistUser);
        var request4 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest4));
        mockMvc.perform(request4).andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        var dtoRequest = getTaskCreateDTO();
        var request = put(basePath + "/" + testTask.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest));

        mockMvc.perform(request).andExpect(status().isOk());

        var actual = service.findById(testTask.getId());

        assertEquals(dtoRequest.getTitle(), actual.getTitle());
        assertEquals(dtoRequest.getTaskLabelIds(), actual.getTaskLabelIds());
        assertEquals(dtoRequest.getAssigneeId(), actual.getAssigneeId());
        assertEquals(dtoRequest.getStatus(), actual.getStatus());
    }

    @Test
    public void testUpdateFailture() throws Exception {
        // Case: Task.name = ""
        var dtoRequest1 = getTaskCreateDTO();
        dtoRequest1.setTitle("");
        var request1 = put(basePath + "/" + testTask.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest1));
        mockMvc.perform(request1).andExpect(status().isBadRequest());

        // Case: Task.status = null
        var dtoRequest2 = "{\"title\":\"zopa\", \"status\":null}";
        var request2 = put(basePath + "/" + testTask.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(dtoRequest2);
        mockMvc.perform(request2).andExpect(status().isBadRequest());

        // Case: Task.status = not existing
        var dtoRequest3 = getTaskCreateDTO();
        dtoRequest3.setStatus("zzz");
        var request3 = put(basePath + "/" + testTask.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest3));
        mockMvc.perform(request3).andExpect(status().isNotFound());

        //Case: Updating a non-existent task
        var nonExistentIdTask = testUtils.getNonExistentId(repository, Task::getId);
        var dtoRequest4 = getTaskCreateDTO();
        var request4 = put(basePath + "/" + nonExistentIdTask).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest4));
        mockMvc.perform(request4).andExpect(status().isNotFound());

        // Case: Task.user = not existing
        var nonExistUserId = testUtils.getNonExistentId(userRepository, User::getId);
        var dtoRequest5 = getTaskCreateDTO();
        dtoRequest5.setAssigneeId(nonExistUserId);
        var request5 = put(basePath + "/" + testTask.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest5));
        var responseBody = mockMvc.perform(request5).andExpect(status().isNotFound());
    }

    @Test
    public void testIndexSuccessAll() throws Exception {
        var request = get(basePath).with(token);
        var responseBody = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        List<TaskDTO> tasksDTOS = om.readValue(responseBody, new TypeReference<>() {
        });
        var actual = tasksDTOS.stream().map(TaskDTO::getId).toList();
        var expected = repository.findAll().stream().map(Task::getId).toList();
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testIndexSuccessAllWithFiltersFind() throws Exception {
        var request = get(basePath).with(token).param("titleCont", testTask.getName().substring(2))
                .param("status", testTask.getTaskStatus().getSlug());
        var responseBody = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        List<TaskDTO> tasksDTOS = om.readValue(responseBody, new TypeReference<>() {
        });
        var actual = tasksDTOS.stream().map(TaskDTO::getId).toList();
        var expected = repository.findAll().stream().map(Task::getId).toList();
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testIndexSuccessAllWithFiltersNotFind() throws Exception {
        var request = get(basePath).with(token).param("titleCont", testTask.getName() + "i")
                .param("status", testTask.getTaskStatus().getSlug());
        var responseBody = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        List<TaskDTO> tasksDTOS = om.readValue(responseBody, new TypeReference<>() {
        });
        assertTrue(tasksDTOS.isEmpty());
    }
    @Test
    public void testIndexFailtureAll() throws Exception {
        var request1 = get(basePath).with(token).queryParam("_sort", "i")
                .queryParam("_order", "ASC").queryParam("_start", "0")
                .queryParam("_end", "25");
        var responseBody1 = mockMvc.perform(request1).andExpect(status().isBadRequest());

        var request2 = get(basePath).with(token).queryParam("_sort", "id")
                .queryParam("_order", "AS").queryParam("_start", "0")
                .queryParam("_end", "25");
        var responseBody2 = mockMvc.perform(request2).andExpect(status().isBadRequest());
    }

    @Test
    public void testShowSuccess() throws Exception {
        var request = get(basePath + "/" + testTask.getId()).with(jwt());
        var responseBody = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        assertThatJson(responseBody).and(v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()));
    }

    @Test
    public void testShowFailture() throws Exception {
        var nonExistentIdTask = testUtils.getNonExistentId(repository, Task::getId);
        var request = get(basePath + "/" + nonExistentIdTask).with(jwt());
        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        var id = testTask.getId();
        var task = repository.findById(id);
        assertTrue(task.isPresent());

        var request = delete(basePath + "/" + id).with(jwt());
        mockMvc.perform(request).andExpect(status().isNoContent());

        var taskDeleted = repository.findById(id);
        assertTrue(taskDeleted.isEmpty());
    }

    @Test
    public void testDeleteFailture() throws Exception {
        var nonExistentIdTask = testUtils.getNonExistentId(repository, Task::getId);
        var request = delete(basePath + "/" + nonExistentIdTask).with(jwt());
        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    private TaskCreateDTO getTaskCreateDTO() {
        var task = Instancio.of(modelGenerator.getTaskModel()).create();

        var taskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(taskStatus);

        var label1 = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(label1);
        var label2 = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(label2);


        var dto = new TaskCreateDTO();
        dto.setStatus(taskStatus.getSlug());
        dto.setAssigneeId(testUser.getId());
        dto.setTitle(task.getName());
        dto.setTaskLabelIds(new HashSet<>(Arrays.asList(label1.getId(), label2.getId())));

        return dto;
    }
}
