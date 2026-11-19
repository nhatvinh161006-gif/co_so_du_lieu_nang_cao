package com.doan10.db_normalization_tool;

import com.doan10.db_normalization_tool.algorithm.BaoDongAlgorithm;
import com.doan10.db_normalization_tool.algorithm.ChuanHoaAlgorithm;
import com.doan10.db_normalization_tool.algorithm.PhuToiThieuAlgorithm;
import com.doan10.db_normalization_tool.algorithm.TimKhoaAlgorithm;
import com.doan10.db_normalization_tool.api.AlgorithmController;
import com.doan10.db_normalization_tool.api.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlgorithmControllerTest {
    private final MockMvc mockMvc;

    AlgorithmControllerTest() {
        BaoDongAlgorithm baoDong = new BaoDongAlgorithm();
        TimKhoaAlgorithm timKhoa = new TimKhoaAlgorithm(baoDong);
        PhuToiThieuAlgorithm phuToiThieu = new PhuToiThieuAlgorithm(baoDong);
        ChuanHoaAlgorithm chuanHoa = new ChuanHoaAlgorithm(baoDong, timKhoa, phuToiThieu);
        AlgorithmController controller =
                new AlgorithmController(baoDong, timKhoa, phuToiThieu, chuanHoa);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void keysEndpointReturnsCandidateKeysAndSteps() throws Exception {
        mockMvc.perform(post("/api/algorithms/keys")
                        .contentType("application/json")
                        .content("""
                                {
                                  "universe": ["A", "B", "C"],
                                  "dependencies": [
                                    {"left": ["A"], "right": ["B"]},
                                    {"left": ["B"], "right": ["A"]}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys.length()").value(2))
                .andExpect(jsonPath("$.source[0]").value("C"))
                .andExpect(jsonPath("$.steps.length()").isNotEmpty());
    }

    @Test
    void closureAndImplicationEndpointsReturnClosureAndSteps() throws Exception {
        String request = """
                {
                  "universe": ["A", "B"],
                  "dependencies": [{"left": ["A"], "right": ["B"]}],
                  "x": ["A"],
                  "y": ["B"]
                }
                """;

        mockMvc.perform(post("/api/algorithms/closure")
                        .contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closure.length()").value(2))
                .andExpect(jsonPath("$.steps.length()").isNotEmpty());

        mockMvc.perform(post("/api/algorithms/implication")
                        .contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.implied").value(true))
                .andExpect(jsonPath("$.steps.length()").isNotEmpty());
    }

    @Test
    void rejectsAttributesOutsideUniverseWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/algorithms/minimal-cover")
                        .contentType("application/json")
                        .content("""
                                {
                                  "universe": ["A"],
                                  "dependencies": [{"left": ["A"], "right": ["B"]}]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("dependencies[0].right"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void exposesMinimalCoverNormalFormAnd3nfDecomposition() throws Exception {
        String request = """
                {
                  "universe": ["A", "B", "C"],
                  "dependencies": [
                    {"left": ["A"], "right": ["B"]},
                    {"left": ["B"], "right": ["C"]}
                  ]
                }
                """;

        mockMvc.perform(post("/api/algorithms/minimal-cover")
                        .contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minimalCover.length()").value(2))
                .andExpect(jsonPath("$.steps.length()").isNotEmpty());

        mockMvc.perform(post("/api/algorithms/check-nf")
                        .contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highestNormalForm").value("2NF"))
                .andExpect(jsonPath("$.violations.length()").isNotEmpty());

        mockMvc.perform(post("/api/algorithms/decompose-3nf")
                        .contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.relations.length()").value(2))
                .andExpect(jsonPath("$.steps.length()").isNotEmpty());
    }

    @Test
    void exposesHighPriorityEndpoints() throws Exception {
        // 1. Tương đương
        mockMvc.perform(post("/api/algorithms/equivalence")
                        .contentType("application/json")
                        .content("""
                                {
                                  "dependencies": [
                                    {"left": ["A"], "right": ["B"]},
                                    {"left": ["B"], "right": ["C"]}
                                  ],
                                  "dependencies2": [
                                    {"left": ["A"], "right": ["B"]},
                                    {"left": ["B"], "right": ["C"]},
                                    {"left": ["A"], "right": ["C"]}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equivalent").value(true))
                .andExpect(jsonPath("$.steps.length()").isNotEmpty());

        // 2. Siêu khóa
        mockMvc.perform(post("/api/algorithms/superkeys")
                        .contentType("application/json")
                        .content("""
                                {
                                  "universe": ["A", "B", "C"],
                                  "dependencies": [
                                    {"left": ["A"], "right": ["B"]},
                                    {"left": ["B"], "right": ["A"]}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateKeys.length()").value(2))
                .andExpect(jsonPath("$.totalCount").value("3"))
                .andExpect(jsonPath("$.sampleSuperkeys.length()").value(3))
                .andExpect(jsonPath("$.steps.length()").isNotEmpty());

        // 3. Phân rã BCNF
        mockMvc.perform(post("/api/algorithms/decompose-bcnf")
                        .contentType("application/json")
                        .content("""
                                {
                                  "universe": ["A", "B", "C"],
                                  "dependencies": [
                                    {"left": ["A"], "right": ["B"]},
                                    {"left": ["B"], "right": ["C"]}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.relations.length()").value(2))
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.steps.length()").isNotEmpty());

        // 4. Chase Lossless Join
        mockMvc.perform(post("/api/algorithms/chase")
                        .contentType("application/json")
                        .content("""
                                {
                                  "universe": ["A", "B", "C"],
                                  "dependencies": [
                                    {"left": ["A"], "right": ["B"]}
                                  ],
                                  "relations": [
                                    ["A", "B"],
                                    ["A", "C"]
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lossless").value(true))
                .andExpect(jsonPath("$.satisfyingRow").isNotEmpty())
                .andExpect(jsonPath("$.steps.length()").isNotEmpty());

        // 5. Bảo toàn phụ thuộc
        mockMvc.perform(post("/api/algorithms/check-preservation")
                        .contentType("application/json")
                        .content("""
                                {
                                  "universe": ["A", "B", "C"],
                                  "dependencies": [
                                    {"left": ["A"], "right": ["B"]},
                                    {"left": ["B"], "right": ["C"]}
                                  ],
                                  "relations": [
                                    ["A", "B"],
                                    ["B", "C"]
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preserved").value(true))
                .andExpect(jsonPath("$.lostDependencies.length()").value(0))
                .andExpect(jsonPath("$.steps.length()").isNotEmpty());
    }
}
