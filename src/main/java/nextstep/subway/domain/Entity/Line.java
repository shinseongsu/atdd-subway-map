package nextstep.subway.domain.Entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Line extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(name = "color")
    private String color;

    @Column(name = "up_station_id")
    private Long upStationId;

    @Column(name = "down_station_id")
    private Long downStationId;

    @Column(name = "distance")
    private int distance;

    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private final List<Section> sections = new ArrayList<>();

    protected Line() {
        //
    }

    public Line(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public void updateLine(final String name, final String color) {
        this.color = color;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}
