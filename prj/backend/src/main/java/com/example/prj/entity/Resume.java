import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import jakarta.validation.constraints.NotBlank; 
@Entity
@Table(name = "resumes",
indexes = @Index(name = "idx_user_id", columnList = "user_id"))
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Resume text is required")
    @Column(name="resume_text",nullable = false,columnDefinition = "LONGTEXT")
    private String resumeText;

    @NotBlank(message = "File name is required")
    @Column(name="file_name",nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name="status",nullable = false)
    private ResumeStatus status=ResumeStatus.PROCESSING;

    @Column(name="created_at",nullable = false,updatable = false)
    private Instant createdAt=Instant.now();

    @ElementCollection
    @CollectionTable(name = "resume_skills", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();  

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeChunk> chunks = new ArrayList<>();
    protected Resume() {
        // Default constructor for JPA
    }
    public Resume(User user, String resumeText, String fileName) {
        this.user = user;
        this.resumeText = resumeText;
        this.fileName = fileName;
    }
    
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getResumeText() {
        return resumeText;
    }

    public String getFileName() {
        return fileName;
    }

    public ResumeStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<String> getSkills() {
        return Collections.unmodifiableSet(skills);
    }

    public List<ResumeChunk> getChunks() {
        return Collections.unmodifiableList(chunks);
    }
    public void markCompleted() {
        if (this.status != ResumeStatus.PROCESSING) {
            throw new IllegalStateException("Only processing resumes can be marked completed");
        }
        this.status = ResumeStatus.COMPLETED;
    }
    public void markFailed() {
        if (this.status != ResumeStatus.PROCESSING) {
            throw new IllegalStateException("Only processing resumes can be marked failed");
        }
        this.status = ResumeStatus.FAILED;
    }
    public void addChunk(ResumeChunk chunk) {
        if (chunk.getResume() != this) {
        throw new IllegalArgumentException("Chunk belongs to a different resume");
    }
        chunks.add(chunk);
        
    }
    public void retry() {
    if (this.status != ResumeStatus.FAILED) {
        throw new IllegalStateException("Only failed resumes can be retried");
    }
    this.status = ResumeStatus.PROCESSING;
}
    
    
}
public enum ResumeStatus {
    PROCESSING,
    COMPLETED,
    FAILED
}