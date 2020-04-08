package org.excellent.cancer.experimental.application.jdbc.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;

import java.util.Set;

/**
 * 表示存在过的项目，可以git项目，或者svn项目等
 *
 * @author XyParaCrim
 */
@Data
@AllArgsConstructor
public class Project {

    @Id @With
    private final Long id;

    private String name;

    private String description;

    private Set<OwnerLink> links;

    private Set<Document> docs;

}
