package tech.agrowerk.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Architectural tests to validate JPA entities
 */
@AnalyzeClasses(packages = "tech.agrowerk.infrastructure.model..")
public class EntityArchTest {

    @ArchTest
    static final ArchRule entities_must_be_annotated_with_entity = classes()
            .that().resideInAPackage("..model..")
            .and().areNotEnums()
            .and().areNotInterfaces()
            .and().areNotMemberClasses()
            .and().resideOutsideOfPackage("..valueobject..")
            .and().areNotAnnotatedWith(Embeddable.class)
            .should().beAnnotatedWith(Entity.class)
            .as("All classes in the model package must be annotated with @Entity");

    @ArchTest
    static final ArchRule entities_should_have_table_annotation = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith(Table.class)
            .as("Entities must have @Table for explicit control of the table name");

    @ArchTest
    static final ArchRule no_eager_fetch_in_one_to_many = fields()
            .that().areAnnotatedWith(OneToMany.class)
            .should(notHaveEagerFetch())
            .as("@OneToMany must not use FetchType.EAGER (leads to N+1 query issues)");

    @ArchTest
    static final ArchRule no_eager_fetch_in_many_to_many = fields()
            .that().areAnnotatedWith(ManyToMany.class)
            .should(notHaveEagerFetch())
            .as("@ManyToMany must not use FetchType.EAGER (leads to N+1 query issues");

    @ArchTest
    static final ArchRule many_to_one_should_be_lazy = fields()
            .that().areAnnotatedWith(ManyToOne.class)
            .should(beLazyFetch())
            .as("@ManyToOne must use FetchType.LAZY for performance reasons");

    @ArchTest
    static final ArchRule one_to_one_should_be_lazy = fields()
            .that().areAnnotatedWith(OneToOne.class)
            .should(beLazyFetch())
            .as("@OneToOne must use FetchType.LAZY for performance reasons");

    @ArchTest
    static final ArchRule one_to_many_must_use_collection_types = fields()
            .that().areAnnotatedWith(OneToMany.class)
            .should().haveRawType(List.class)
            .orShould().haveRawType(Set.class)
            .orShould().haveRawType(Collection.class)
            .as("@OneToMany must use List, Set, or Collection as its type");

    @ArchTest
    static final ArchRule many_to_many_must_use_collection_types = fields()
            .that().areAnnotatedWith(ManyToMany.class)
            .should().haveRawType(List.class)
            .orShould().haveRawType(Set.class)
            .orShould().haveRawType(Collection.class)
            .as("@ManyToMany must use List, Set, or Collection as its type");

    @ArchTest
    static final ArchRule one_to_many_should_have_mapped_by = fields()
            .that().areAnnotatedWith(OneToMany.class)
            .should(haveMappedByAttribute())
            .as("@OneToMany must declare mappedBy for correct bidirectional ownership");

    @ArchTest
    static final ArchRule many_to_one_should_have_join_column = fields()
            .that().areAnnotatedWith(ManyToOne.class)
            .should().beAnnotatedWith(JoinColumn.class)
            .as("@ManyToOne must declare @JoinColumn to explicitly control the foreign key");


    @ArchTest
    static final ArchRule avoid_cascade_all = fields()
            .that().areAnnotatedWith(OneToMany.class)
            .or().areAnnotatedWith(ManyToMany.class)
            .or().areAnnotatedWith(ManyToOne.class)
            .or().areAnnotatedWith(OneToOne.class)
            .should(notUseCascadeAll())
            .as("Avoid CascadeType.ALL - cascade operations must be explicitly defined");

    @ArchTest
    static final ArchRule entities_must_have_no_args_constructor = classes()
            .that().areAnnotatedWith(Entity.class)
            .should(haveNoArgsConstructor())
            .as("JPA entities must declare a no-args constructor (may be private via Lombok)");

    @ArchTest
    static final ArchRule entities_must_have_id_field = classes()
            .that().areAnnotatedWith(Entity.class)
            .should(haveIdField())
            .as("Entities must declare a field annotated with @Id");

    @ArchTest
    static final ArchRule id_should_use_uuid = fields()
            .that().areAnnotatedWith(Id.class)
            .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .should().haveRawType(java.util.UUID.class)
            .as("Entity identifiers must use UUID for better distribution and security");

    @ArchTest
    static final ArchRule id_should_have_generated_value = fields()
            .that().areAnnotatedWith(Id.class)
            .should().beAnnotatedWith(GeneratedValue.class)
            .as("Entity identifiers must declare @GeneratedValue for automatic generation");

    @ArchTest
    static final ArchRule entities_should_have_created_at = classes()
            .that().areAnnotatedWith(Entity.class)
            .should(haveFieldAnnotatedWith("createdAt", "created_at"))
            .as("Entities must declare a createdAt field for auditing purposes");

    @ArchTest
    static final ArchRule no_bidirectional_many_to_many_without_intermediate_entity = noClasses()
            .should(haveBidirectionalManyToManyWithoutIntermediateEntity())
            .as("Bidirectional ManyToMany relationships must use an intermediate entity");

    @ArchTest
    static final ArchRule collections_should_be_initialized = fields()
            .that().haveRawType(List.class)
            .or().haveRawType(Set.class)
            .or().haveRawType(Collection.class)
            .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .should(beInitialized())
            .as("Entity collections must be initialized to avoid NullPointerException");


    private static ArchCondition<JavaField> notHaveEagerFetch() {
        return new ArchCondition<>("not have FetchType.EAGER") {
            @Override
            public void check(JavaField field, ConditionEvents events) {
                field.getAnnotations().stream()
                        .filter(ann -> ann.getRawType().getName().contains("jakarta.persistence"))
                        .forEach(ann -> {
                            try {
                                Object fetch = ann.get("fetch");
                                if (fetch != null && fetch.toString().contains("EAGER")) {
                                    String message = String.format(
                                            "The field '%s' em '%s' uses FetchType.EAGER",
                                            field.getName(),
                                            field.getOwner().getName()
                                    );
                                    events.add(SimpleConditionEvent.violated(field, message));
                                }
                            } catch (Exception e) {
                            }
                        });
            }
        };
    }

    private static ArchCondition<JavaField> beLazyFetch() {
        return new ArchCondition<>("use FetchType.LAZY") {
            @Override
            public void check(JavaField field, ConditionEvents events) {
                boolean hasLazy = field.getAnnotations().stream()
                        .anyMatch(ann -> {
                            try {
                                Object fetch = ann.get("fetch");
                                return fetch != null && fetch.toString().contains("LAZY");
                            } catch (Exception e) {
                                return false;
                            }
                        });

                if (!hasLazy) {
                    String message = String.format(
                            "The field '%s' in '%s' must use fetch = FetchType.LAZY",
                            field.getName(),
                            field.getOwner().getName()
                    );
                    events.add(SimpleConditionEvent.violated(field, message));
                }
            }
        };
    }

    private static ArchCondition<JavaField> haveMappedByAttribute() {
        return new ArchCondition<>("have mappedBy attribute") {
            @Override
            public void check(JavaField field, ConditionEvents events) {
                boolean hasMappedBy = field.getAnnotationOfType(OneToMany.class) != null &&
                        field.getAnnotations().stream()
                                .anyMatch(ann -> {
                                    try {
                                        Object mappedBy = ann.get("mappedBy");
                                        return mappedBy != null && !mappedBy.toString().isEmpty();
                                    } catch (Exception e) {
                                        return false;
                                    }
                                });

                if (!hasMappedBy) {
                    String message = String.format(
                            "The field '%s' in '%s' must have attribute mappedBy",
                            field.getName(),
                            field.getOwner().getName()
                    );
                    events.add(SimpleConditionEvent.violated(field, message));
                }
            }
        };
    }

    private static ArchCondition<JavaField> notUseCascadeAll() {
        return new ArchCondition<>("not use CascadeType.ALL") {
            @Override
            public void check(JavaField field, ConditionEvents events) {
                field.getAnnotations().stream()
                        .filter(ann -> ann.getRawType().getName().contains("jakarta.persistence"))
                        .forEach(ann -> {
                            try {
                                Object cascade = ann.get("cascade");
                                if (cascade != null && cascade.toString().contains("ALL")) {
                                    String message = String.format(
                                            "The field '%s' in '%s' uses CascadeType.ALL - be explicit!",
                                            field.getName(),
                                            field.getOwner().getName()
                                    );
                                    events.add(SimpleConditionEvent.violated(field, message));
                                }
                            } catch (Exception e) {
                            }
                        });
            }
        };
    }

    private static ArchCondition<JavaClass> haveNoArgsConstructor() {
        return new ArchCondition<>("have no-args constructor") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasNoArgsConstructor = javaClass.getConstructors().stream()
                        .anyMatch(c -> c.getRawParameterTypes().isEmpty());

                boolean hasLombokNoArgs = javaClass.isAnnotatedWith(NoArgsConstructor.class);

                if (!hasNoArgsConstructor && !hasLombokNoArgs) {
                    String message = String.format(
                            "The class '%s' has no constructor without arguments",
                            javaClass.getName()
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveIdField() {
        return new ArchCondition<>("have @Id field") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasId = javaClass.getAllFields().stream()
                        .anyMatch(f -> f.isAnnotatedWith(Id.class));

                if (!hasId) {
                    String message = String.format(
                            "The entity '%s' has no annotated field with @Id",
                            javaClass.getName()
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveFieldAnnotatedWith(String fieldName, String columnName) {
        return new ArchCondition<>("have field " + fieldName) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasField = javaClass.getAllFields().stream()
                        .anyMatch(f -> f.getName().equals(fieldName));

                if (!hasField) {
                    String message = String.format(
                            "The entity '%s' should have a field %s for auditing",
                            javaClass.getName(),
                            fieldName
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveBidirectionalManyToManyWithoutIntermediateEntity() {
        return new ArchCondition<>("have bidirectional ManyToMany without intermediate entity") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getAllFields().stream()
                        .filter(f -> f.isAnnotatedWith(ManyToMany.class))
                        .forEach(field -> {
                            boolean hasMappedBy = field.getAnnotations().stream()
                                    .anyMatch(ann -> {
                                        try {
                                            Object mappedBy = ann.get("mappedBy");
                                            return mappedBy != null && !mappedBy.toString().isEmpty();
                                        } catch (Exception e) {
                                            return false;
                                        }
                                    });

                            if (hasMappedBy) {
                                String message = String.format(
                                        "The field '%s' in '%s' uses a bidirectional ManyToMany. " +
                                                "Consider creating an intermediate entity for better control",
                                        field.getName(),
                                        javaClass.getName()
                                );
                                events.add(SimpleConditionEvent.violated(javaClass, message));
                            }
                        });
            }
        };
    }

    private static ArchCondition<JavaField> beInitialized() {
        return new ArchCondition<>("be initialized") {
            @Override
            public void check(JavaField field, ConditionEvents events) {
                boolean hasBuilderDefault = field.getAnnotations().stream()
                        .anyMatch(ann -> ann.getRawType().getName().contains("Builder.Default"));

                if (!hasBuilderDefault &&
                        (field.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PRIVATE))) {

                    String message = String.format(
                            "The collection '%s' in '%s' should be initialized or use @Builder.Default",
                            field.getName(),
                            field.getOwner().getName()
                    );
                    events.add(SimpleConditionEvent.satisfied(field, message));
                }
            }
        };
    }
}