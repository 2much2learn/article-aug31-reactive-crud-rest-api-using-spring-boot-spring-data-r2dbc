package com.toomuch2learn.reactive.crud.catalogue.model;

import com.toomuch2learn.reactive.crud.catalogue.validation.IEnumValidator;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@NoArgsConstructor
@Table("CATALOGUE_ITEMS")
public class CatalogueItem {

    @Id
    @Column("ID")
    private Long id;

    @NotEmpty(message = "SKU cannot be null or empty")
    @NonNull
    @Column("SKU_NUMBER")
    private String sku;

    @NotEmpty(message = "Name cannot be null or empty")
    @NonNull
    @Column("ITEM_NAME")
    private String name;

    @NotEmpty(message = "Description cannot be null or empty")
    @NonNull
    @Column("DESCRIPTION")
    private String description;

    @NonNull
    @Column("CATEGORY")
    @IEnumValidator(
        enumClazz = Category.class,
        message = "Invalid category provided"
    )
    private String category;

    @NotNull(message = "Price cannot be null or empty")
    @NonNull
    @Column("PRICE")
    private Double price;

    @NotNull(message = "Inventory cannot be null or empty")
    @NonNull
    @Column("INVENTORY")
    private Integer inventory;

    @NonNull
    @Column("CREATED_ON")
    private Instant createdOn;

    @Column("UPDATED_ON")
    private Instant updatedOn;
}
