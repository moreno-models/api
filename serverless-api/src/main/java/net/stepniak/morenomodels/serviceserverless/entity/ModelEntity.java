package net.stepniak.morenomodels.serviceserverless.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "model")
@Table(name = "models")
public class ModelEntity implements Serializable {
    @Id
    @NotBlank
    @Column(name = "model_id", nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String modelId;

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelEntity that = (ModelEntity) o;
        return modelId.equals(that.modelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId);
    }
}
