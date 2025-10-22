package hexlet.code.spring.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.spring.dto.taskstatus.TaskStatusDTO;
import hexlet.code.spring.mapper.TaskStatusMainMapper;
import hexlet.code.spring.model.Task;
import hexlet.code.spring.model.TaskStatus;
import hexlet.code.spring.model.User;
import hexlet.code.spring.repository.TaskRepository;
import hexlet.code.spring.repository.TaskStatusRepository;
import hexlet.code.spring.repository.UserRepository;
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
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
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
public class TaskStatusControllerTest {

    private final WebApplicationContext wac;
    private final TaskStatusMainMapper mapper;
    private final TaskStatusRepository repository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ModelGenerator modelGenerator;
    private final ObjectMapper om;
    private final TestUtils testUtils;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private User testUser;
    private MockMvc mockMvc;

    private final String basePath = "/api/task_statuses";

    @Autowired
    @SuppressWarnings("checkstyle:ParameterNumber")
    public TaskStatusControllerTest(@NonNull final WebApplicationContext wacD,
                                @NonNull  final TaskStatusMainMapper mapperD,
                                @NonNull final TaskRepository taskRepositoryD,
                                @NonNull final UserRepository userRepositoryD,
                                @NonNull final TaskStatusRepository taskStatusRepositoryD,
                                @NonNull final ModelGenerator modelGeneratorD, @NonNull final ObjectMapper omD,
                                @NonNull final TestUtils testUtilsD) {
        this.wac = wacD;
        this.mapper = mapperD;
        this.taskRepository = taskRepositoryD;
        this.userRepository = userRepositoryD;
        this.repository = taskStatusRepositoryD;
        this.modelGenerator = modelGeneratorD;
        this.om = omD;
        this.testUtils = testUtilsD;
    }

    @BeforeEach
    public void setUp() {
        testUtils.clearAllRepository();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac).defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity()).build();

        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @Test
    public void testCreateSuccess() throws Exception {
        var data = Instancio.of(modelGenerator.getTaskStatusModel()).create();

        var request = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request).andExpect(status().isCreated());

        var taskStatus = repository.findBySlug(data.getSlug()).orElse(null);

        assertNotNull(taskStatus);
        assertThat(taskStatus.getSlug()).isEqualTo(data.getSlug());
        assertThat(taskStatus.getName()).isEqualTo(data.getName());
    }

    @Test
    public void testCreateFailture() throws Exception {
        var data = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        repository.save(data);

        var data1 = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        data1.setSlug("");

        var request1 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data1));
        mockMvc.perform(request1).andExpect(status().isBadRequest());

        var data2 = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        data2.setName("");

        var request2 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data2));
        mockMvc.perform(request2).andExpect(status().isBadRequest());

        var testTaskStatus = new TaskStatus();
        testTaskStatus.setSlug(data.getSlug());
        var request3 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testTaskStatus));
        mockMvc.perform(request3).andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        var data = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        repository.save(data);

        var updateData = new TaskStatus();
        updateData.setSlug("cavabanga");
        updateData.setName("name1");

        var request = put(basePath + "/" + data.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));
        mockMvc.perform(request).andExpect(status().isOk());

        var updatedTaskStatus = repository.findById(data.getId()).get();

        assertEquals(updatedTaskStatus.getSlug(), updateData.getSlug());
        assertEquals(updatedTaskStatus.getName(), updateData.getName());
    }

    @Test
    public void testUpdateFailture() throws Exception {
        var data1 = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        repository.save(data1);

        var updateData = new TaskStatus();
        updateData.setSlug("");

        var request1 = put(basePath + "/" + data1.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));
        mockMvc.perform(request1).andExpect(status().isBadRequest());

        var data2 = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        repository.save(data2);
        data2.setSlug(data1.getSlug());

        var request2 = put(basePath + "/" + data2.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data2));
        mockMvc.perform(request2).andExpect(status().isBadRequest());
    }

    @Test
    public void testIndexSuccess() throws Exception {
        List<String> list = Arrays.asList("draft", "to_review", "to_be_fixed", "to_publish", "published");
        for (String elem : list) {
            var taskStatus = new TaskStatus();
            taskStatus.setName(elem);
            taskStatus.setSlug(elem);
            repository.save(taskStatus);
        }

        var request = get(basePath).with(token);
        var responseBody = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        List<TaskStatusDTO> taskStatusesDTOS = om.readValue(responseBody, new TypeReference<>() {
        });
        var actual = taskStatusesDTOS.stream().map(mapper::mapToModel).map(TaskStatus::getId).toList();
        var expected = repository.findAll().stream().map(TaskStatus::getId).toList();
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testIndexFailture() throws Exception {
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
        var testTaskStatus = new TaskStatus();
        testTaskStatus.setSlug("draft");
        testTaskStatus.setName("draft");
        repository.save(testTaskStatus);

        var request = get(basePath + "/" + testTaskStatus.getId()).with(jwt());
        var responseBody = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        assertThatJson(responseBody).and(v -> v.node("slug").isEqualTo(testTaskStatus.getSlug()),
                v -> v.node("name").isEqualTo(testTaskStatus.getName()));
    }

    @Test
    public void testShowFailture() throws Exception {
        var nonExistentIdTaskStatus = testUtils.getNonExistentId(repository, TaskStatus::getId);
        var request = get(basePath + "/" + nonExistentIdTaskStatus).with(jwt());
        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        var testTaskStatus = new TaskStatus();
        testTaskStatus.setSlug("draft");
        testTaskStatus.setName("draft");
        repository.save(testTaskStatus);

        var id = testTaskStatus.getId();
        var request = delete(basePath + "/" + id).with(jwt());
        mockMvc.perform(request).andExpect(status().isNoContent());

        var taskStatus = repository.findById(id);
        assertTrue(taskStatus.isEmpty());
    }

    @Test
    public void testDeleteFailture() throws Exception {
        // Case delete non-exists status
        var nonExistentIdTaskStatus = testUtils.getNonExistentId(repository, TaskStatus::getId);
        var request1 = delete(basePath + "/" + nonExistentIdTaskStatus).with(jwt());
        mockMvc.perform(request1).andExpect(status().isNotFound());

        // Case delete status in related task
        var task = new Task();
        task.setAssignee(testUser);
        task.setName("Task");

        var taskStatus = new TaskStatus();
        taskStatus.setSlug("draft888");
        taskStatus.setName("draft888");
        repository.save(taskStatus);
        task.setTaskStatus(taskStatus);

        taskRepository.save(task);

        var request2 = delete(basePath + "/" + taskStatus.getId()).with(jwt());
        mockMvc.perform(request2).andExpect(status().isMethodNotAllowed());
    }
}
