package Learning.Management.System.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(columnDefinition = "Text")
    private String description;

    @Column(nullable = false)
    private String term;

    @Column(name = "school_term")
    private SchoolTerm schoolTerm;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

}
