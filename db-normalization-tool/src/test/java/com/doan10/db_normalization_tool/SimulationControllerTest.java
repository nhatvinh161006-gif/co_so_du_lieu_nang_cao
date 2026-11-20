package com.doan10.db_normalization_tool;

import com.doan10.db_normalization_tool.controller.SimulationController;
import com.doan10.db_normalization_tool.service.LichSuChuanHoaService;
import com.doan10.db_normalization_tool.service.SimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SimulationControllerTest {

    private MockMvc mockMvc;
    private SimulationService simulationService;

    @BeforeEach
    void setUp() {
        LichSuChuanHoaService lichSuService = new LichSuChuanHoaService();
        lichSuService.init();
        simulationService = new SimulationService(lichSuService);
        simulationService.ensureInitialized();
        SimulationController controller = new SimulationController(simulationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetStudents() throws Exception {
        mockMvc.perform(get("/api/simulation/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNotEmpty())
                .andExpect(jsonPath("$[0].maSV").value("SV001"));
    }

    @Test
    void testGetCourses() throws Exception {
        mockMvc.perform(get("/api/simulation/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNotEmpty())
                .andExpect(jsonPath("$[0].maLHP").value("LHP00"));
    }

    @Test
    void testGetQueries() throws Exception {
        mockMvc.perform(get("/api/simulation/queries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    void testRegisterWithRollbackWhenPrerequisiteFailed() throws Exception {
        // SV004 chưa đạt môn CSDL101, đăng ký LHP01 (CSDL Nâng Cao) phải bị Rollback
        mockMvc.perform(post("/api/simulation/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "maSV": "SV004",
                                  "maLHP": "LHP01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ROLLBACK"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.steps.length()").isNotEmpty());
    }
}
