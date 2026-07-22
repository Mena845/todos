package hei.school.todos.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UUIDValidator.class)
@Documented
public @interface ValidUUID {

    String message() default "id doit etre un UUID valide";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}