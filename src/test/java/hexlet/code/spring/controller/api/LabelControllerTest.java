package hexlet.code.spring.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.spring.dto.label.LabelDTO;
import hexlet.code.spring.mapper.LabelMainMapper;
import hexlet.code.spring.model.Label;
import hexlet.code.spring.model.Task;
import hexlet.code.spring.model.User;
import hexlet.code.spring.repository.LabelRepository;
import hexlet.code.spring.repository.TaskRepository;
import hexlet.code.spring.repository.TaskStatusRepository;
import hexlet.code.spring.repository.UserRepository;
import hexlet.code.spring.service.LabelService;
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
public class LabelControllerTest {

    private final WebApplicationContext wac;
    private final LabelMainMapper mapper;
    private final LabelRepository repository;
    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskRepository taskRepository;
    private final ModelGenerator modelGenerator;
    private final ObjectMapper om;
    private final LabelService service;
    private final TestUtils testUtils;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private User testUser;
    private Task testTask;
    private Label testLabel;
    private MockMvc mockMvc;

    private final String basePath = "/api/labels";

    @Autowired
    @SuppressWarnings("checkstyle:ParameterNumber")
    public LabelControllerTest(@NonNull final WebApplicationContext wacD, @NonNull  final LabelMainMapper mapperD,
                            @NonNull final TaskRepository taskRepositoryD,
                            @NonNull final UserRepository userRepositoryD,
                            @NonNull final TaskStatusRepository taskStatusRepositoryD,
                            @NonNull final ModelGenerator modelGeneratorD,
                            @NonNull final ObjectMapper omD, @NonNull final LabelService serviceD,
                            @NonNull final TestUtils testUtilsD, @NonNull final LabelRepository labelRepositoryD) {
        this.wac = wacD;
        this.mapper = mapperD;
        this.taskRepository = taskRepositoryD;
        this.userRepository = userRepositoryD;
        this.taskStatusRepository = taskStatusRepositoryD;
        this.modelGenerator = modelGeneratorD;
        this.om = omD;
        this.service = serviceD;
        this.testUtils = testUtilsD;
        this.repository = labelRepositoryD;
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
        taskRepository.save(testTask);

        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        repository.save(testLabel);
    }

    @Test
    public void testCreateSuccess() throws Exception {
        var label = Instancio.of(modelGenerator.getLabelModel()).create();

        var dtoRequest = mapper.mapToDTO(label);
        var request = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest));

        var responseBody = mockMvc.perform(request).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        LabelDTO dtoResponse = om.readValue(responseBody, new TypeReference<>() { });


        var dtoFromBase = service.findById(dtoResponse.getId());

        assertNotNull(dtoFromBase);
        assertEquals(dtoFromBase.getName(), dtoResponse.getName());
    }

    @Test
    public void testCreateFailture() throws Exception {
        // Case: Label.name = 2 symbols length
        var label1 = Instancio.of(modelGenerator.getLabelModel()).create();
        label1.setName("ab");

        var dtoRequest1 = mapper.mapToDTO(label1);
        var request1 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest1));
        mockMvc.perform(request1).andExpect(status().isBadRequest());

        // Case: Label.name = null
        var label2 = new Label();
        label2.setName(null);
        var dtoRequest2 = mapper.mapToDTO(label2);
        var request2 = post(basePath).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest2));
        mockMvc.perform(request2).andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        var labelWithDataToUpdate = Instancio.of(modelGenerator.getLabelModel()).create();

        var dtoRequest = mapper.mapToDTO(labelWithDataToUpdate);
        var request = put(basePath + "/" + testLabel.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest));

        var responseBody = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();

        var updatedLabel = repository.findById(testLabel.getId());
        var updatedLabelDTO = mapper.mapToDTO(updatedLabel.get());

        assertThatJson(responseBody).and(v -> v.node("name").isEqualTo(updatedLabelDTO.getName()));
    }

    @Test
    public void testUpdateFailture() throws Exception {
        // Case: Label.name = 2 symbols length
        var label1 = Instancio.of(modelGenerator.getLabelModel()).create();
        label1.setName("ab");

        var dtoRequest1 = mapper.mapToDTO(label1);
        var request1 = put(basePath + "/" + testLabel.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest1));
        mockMvc.perform(request1).andExpect(status().isBadRequest());

        // Case: Label.name = null
        var label2 = new Label();
        label2.setName(null);
        var dtoRequest2 = mapper.mapToDTO(label2);
        var request2 = put(basePath + "/" + testLabel.getId()).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest2));
        mockMvc.perform(request2).andExpect(status().isBadRequest());


        //Case: Updating a non-existent label
        var nonExistentIdLabel = testUtils.getNonExistentId(repository, Label::getId);
        var dtoRequest3 = mapper.mapToDTO(testLabel);
        var request3 = put(basePath + "/" + nonExistentIdLabel).with(token).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dtoRequest3));
        mockMvc.perform(request3).andExpect(status().isNotFound());
    }

    @Test
    public void testIndexSuccess() throws Exception {
        var request = get(basePath).with(token);
        var responseBody = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        List<LabelDTO> labelsDTOS = om.readValue(responseBody, new TypeReference<>() {
        });
        var actual = labelsDTOS.stream().map(LabelDTO::getId).toList();
        var expected = repository.findAll().stream().map(Label::getId).toList();
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
        var request = get(basePath + "/" + testLabel.getId()).with(jwt());
        var responseBody = mockMvc.perform(request).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        assertThatJson(responseBody).and(v -> v.node("name").isEqualTo(testLabel.getName()));
    }

    @Test
    public void testShowFailture() throws Exception {
        var nonExistentIdTask = testUtils.getNonExistentId(repository, Label::getId);
        var request = get(basePath + "/" + nonExistentIdTask).with(jwt());
        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        var id = testLabel.getId();
        var label = repository.findById(id);
        assertTrue(label.isPresent());

        var request = delete(basePath + "/" + id).with(jwt());
        mockMvc.perform(request).andExpect(status().isNoContent());

        var labelDeleted = repository.findById(id);
        assertTrue(labelDeleted.isEmpty());
    }

    @Test
    public void testDeleteFailture() throws Exception {
        // Case label non-exist
        var nonExistentIdLabel = testUtils.getNonExistentId(repository, Label::getId);
        var request1 = delete(basePath + "/" + nonExistentIdLabel).with(jwt());
        mockMvc.perform(request1).andExpect(status().isNotFound());

        // Cast label uses in task
        testTask.getLabels().add(testLabel);
        taskRepository.save(testTask);
        var request2 = delete(basePath + "/" + testLabel.getId()).with(jwt());
        mockMvc.perform(request2).andExpect(status().isMethodNotAllowed());
    }
}
