package hexlet.code.spring.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import hexlet.code.spring.dto.user.UserDTO;
import hexlet.code.spring.mapper.UserMainMapper;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public final class UserControllerTest {

    private final WebApplicationContext wac;
    private final UserMainMapper mapper;
    private final UserRepository repository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskRepository taskRepository;
    private final ModelGenerator modelGenerator;
    private final ObjectMapper om;
    private final TestUtils testUtils;

    private JwtRequestPostProcessor token;

    private User testUser;
    private MockMvc mockMvc;

    private String basePath = "/api/users";

    @Autowired
    @SuppressWarnings("checkstyle:ParameterNumber")
    public UserControllerTest(@NonNull final WebApplicationContext wacD,
                                    @NonNull  final UserMainMapper mapperD,
                                    @NonNull final TaskRepository taskRepositoryD,
                                    @NonNull final UserRepository userRepositoryD,
                                    @NonNull final TaskStatusRepository taskStatusRepositoryD,
                                    @NonNull final ModelGenerator modelGeneratorD, @NonNull final ObjectMapper omD,
                                    @NonNull final TestUtils testUtilsD) {
        this.wac = wacD;
        this.mapper = mapperD;
        this.taskRepository = taskRepositoryD;
        this.taskStatusRepository = taskStatusRepositoryD;
        this.repository = userRepositoryD;
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
        repository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @Test
    public void testAuthority() throws Exception {
        var request = get(basePath);
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateSuccess() throws Exception {
        var data = Instancio.of(modelGenerator.getUserModel()).create();

        var request = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request).andExpect(status().isCreated());

        var user = repository.findByEmail(data.getEmail()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(data.getFirstName());
        assertThat(user.getLastName()).isEqualTo(data.getLastName());
    }

    @Test
    public void testCreateFailture() throws Exception {
        var data1 = Instancio.of(modelGenerator.getUserModel()).create();
        data1.setPasswordDigest("qq");

        var request1 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data1));
        mockMvc.perform(request1).andExpect(status().isBadRequest());

        var data2 = Instancio.of(modelGenerator.getUserModel()).create();
        data2.setEmail("qq");

        var request2 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data2));
        mockMvc.perform(request2).andExpect(status().isBadRequest());

        var data3 = Instancio.of(modelGenerator.getUserModel()).create();
        data3.setEmail(testUser.getEmail());

        var request3 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data3));
        mockMvc.perform(request3).andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        var data = Instancio.of(modelGenerator.getUserModel()).create();
        repository.save(data);

        var updateData = new User();
        updateData.setEmail("cavabanga@mail.ru");
        updateData.setFirstName("name1");
        updateData.setLastName("name2");

        var request = put(basePath + "/" + data.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));
        mockMvc.perform(request).andExpect(status().isOk());

        var updatedUser = repository.findById(data.getId()).get();

        assertEquals(updatedUser.getEmail(), updateData.getEmail());
        assertEquals(updatedUser.getFirstName(), updateData.getFirstName());
        assertEquals(updatedUser.getLastName(), updateData.getLastName());
    }

    @Test
    public void testUpdateFailture() throws Exception {
        var data1 = Instancio.of(modelGenerator.getUserModel()).create();
        repository.save(data1);

        var updateData = new User();
        updateData.setEmail("cav");

        var request1 = put(basePath + "/" + data1.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));
        mockMvc.perform(request1).andExpect(status().isBadRequest());

        var data2 = Instancio.of(modelGenerator.getUserModel()).create();
        repository.save(data2);

        data2.setEmail(data1.getEmail());

        var request2 = put(basePath + "/" + data2.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data2));
        mockMvc.perform(request2).andExpect(status().isBadRequest());
    }

    @Test
    public void testIndexSuccess() throws Exception {
        var request = get(basePath).with(token);
        var responseBody = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        List<UserDTO> userDTOS = om.readValue(responseBody, new TypeReference<>() {
        });
        var actual = userDTOS.stream().map(mapper::mapToModel).map(u -> u.getId()).toList();
        var expected = repository.findAll().stream().map(u -> u.getId()).toList();
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
        var request = get(basePath + "/" + testUser.getId()).with(jwt());
        var responseBody = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        assertThatJson(responseBody).and(v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()));
    }

    @Test
    public void testShowFailture() throws Exception {
        var nonExistentIdUser = testUtils.getNonExistentId(repository, User::getId);
        var request = get(basePath + "/" + nonExistentIdUser).with(jwt());
        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        var id = testUser.getId();
        var request = delete(basePath + "/" + id).with(jwt());
        mockMvc.perform(request).andExpect(status().isNoContent());

        var user = repository.findById(id);
        assertTrue(user.isEmpty());
    }

    @Test
    public void testDeleteFailture() throws Exception {
        // Case delete non-exists user
        var nonExistentIdUser = testUtils.getNonExistentId(repository, User::getId);
        var request = delete(basePath + "/" + nonExistentIdUser).with(jwt());
        mockMvc.perform(request).andExpect(status().isNotFound());

        // Case delete user assigned on task
        var task = new Task();
        task.setAssignee(testUser);
        task.setName("Task");

        var taskStatus = new TaskStatus();
        taskStatus.setSlug("draft777");
        taskStatus.setName("draft777");
        taskStatusRepository.save(taskStatus);
        task.setTaskStatus(taskStatus);

        taskRepository.save(task);

        var request2 = delete(basePath + "/" + testUser.getId()).with(jwt());
        mockMvc.perform(request2).andExpect(status().isMethodNotAllowed());
    }
}
