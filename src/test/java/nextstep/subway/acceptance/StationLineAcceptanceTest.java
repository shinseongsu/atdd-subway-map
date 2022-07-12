package nextstep.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.applicaion.dto.StationLineRequest;
import nextstep.subway.applicaion.dto.StationLineResponse;
import nextstep.subway.applicaion.dto.StationResponse;
import nextstep.subway.utils.DatabaseCleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StationLineAcceptanceTest {

    @Autowired
    DatabaseCleanup databaseCleanup;

    @LocalServerPort
    int port;

    @BeforeEach
    public void steup() {
        RestAssured.port = port;
        databaseCleanup.execute();
    }

    /**
     * when 지하철 노션을 생성하면
     * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다.
     */
    @DisplayName("지하철노선 생성")
    @Test
    void createStationLine() {
        createStationLineAndValidate("2호선",
                "bg-red-600",
                createStationAndValidate("강남역"),
                createStationAndValidate("역삼역"));
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @DisplayName("지하철노선 목록 조회")
    @Test
    void findAllStationLineTest() {
        createStationLineAndValidate("1호선",
                "bg-blue-600",
                createStationAndValidate("수원역"),
                createStationAndValidate("세류역"));

        createStationLineAndValidate("2호선",
                "bg-red-600",
                createStationAndValidate("강남역"),
                createStationAndValidate("역삼역"));

        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .when().get("/lines")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getList("$")).hasSize(2);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @DisplayName("지하철노선 조회")
    @Test
    void findByStationLineByIdTest() {
        ExtractableResponse<Response> response = createStationLineAndValidate("2호선",
                "bg-red-600",
                createStationAndValidate("강남역"),
                createStationAndValidate("역삼역"));

        ExtractableResponse<Response> createStationLine = RestAssured
                .given().log().all()
                .when().get("/lines/{id}", response.jsonPath().getLong("id"))
                .then().log().all()
                .extract();

        assertThat(createStationLine.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다
     */
    @DisplayName("지하철노선 수정")
    @Test
    void updateByStationLine() {
        ExtractableResponse<Response> response = createStationLineAndValidate("2호선",
                "bg-red-600",
                createStationAndValidate("강남역"),
                createStationAndValidate("역삼역"));

        ExtractableResponse<Response> updateStationLine = RestAssured
                .given().log().all()
                .body(StationLineRequest.builder()
                        .name("신분당선")
                        .color("bg-yellow-300")
                        .build())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().put("/lines/{id}", response.jsonPath().getLong("id"))
                .then().log().all()
                .extract();
        assertThat(updateStationLine.statusCode()).isEqualTo(HttpStatus.OK.value());

        ExtractableResponse<Response> findStationLine = RestAssured
                .given().log().all()
                .when().get("/lines/{id}", response.jsonPath().getLong("id"))
                .then().log().all()
                .extract();
        assertThat(findStationLine.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(findStationLine.jsonPath().getString("name")).isEqualTo("신분당선");
        assertThat(findStationLine.jsonPath().getString("color")).isEqualTo("bg-yellow-300");
    }

    private ExtractableResponse<Response> createStationLineAndValidate(String stationLineName, String color, Long upStationId, Long downStationId) {
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .body(StationLineRequest.builder()
                        .name(stationLineName)
                        .color(color)
                        .upStationId(upStationId)
                        .downStationId(downStationId)
                        .distance(10)
                        .build())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.jsonPath().getString("name")).isEqualTo(stationLineName);
        assertThat(response.jsonPath().getString("color")).isEqualTo(color);

        return response;
    }

    private Long createStationAndValidate(String name) {
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .body(Collections.singletonMap("name", name))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/stations")
                .then().log().all()
                .extract();
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        return response.jsonPath().getLong("id");
    }

}
