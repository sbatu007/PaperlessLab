package documents;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String filename;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Instant uploadedAt = Instant.now();

    public Document() {
        this.uploadedAt = Instant.now();
    }

    public Document(String filename, String description) {
        this.filename = filename;
        this.description = description;
        this.uploadedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Instant getUploadedAt() {
        return uploadedAt;
    }
    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
