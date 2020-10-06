package org.acme.exceptions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.validation.ValidationException;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.status;

@Provider
public class BeanValidationExceptionMapper implements ExceptionMapper<ValidationException> {
	@Override
	public Response toResponse(ValidationException exception) {
		if (exception instanceof ConstraintViolationException) {
			return manageConstraintViolations((ConstraintViolationException) exception);
		}
		if (exception instanceof WrongUserOrPasswordException) {
			return status(Response.Status.UNAUTHORIZED).entity(exception.getMessage()).build();
		}
		if (exception instanceof InvalidTokenException) {
			String message=exception.getMessage();
			if (exception.getCause() instanceof IndexOutOfBoundsException) {
				message="Unparsable Token";
			}
			if (exception.getCause() instanceof IllegalArgumentException) {
				message="Token structure wrong";
			}
			return status(Response.Status.BAD_REQUEST).entity(message).build();
		}
		if (exception instanceof MalformedBasicAuthenticationException) {
			String message=exception.getMessage();
			if (exception.getCause() instanceof IndexOutOfBoundsException) {
				message="Unparsable Credentials";
			}
			
			return status(Response.Status.BAD_REQUEST).entity(message).build();
		}
		
		
		return status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
	}
	
	
    Response manageConstraintViolations(ConstraintViolationException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getConstraintViolations()
                .forEach(v -> 
                    errors.put(lastFieldName(v.getPropertyPath().iterator()), v.getMessage())
                );
        
        return status(Response.Status.BAD_REQUEST).entity(errors).build();
    }
    String lastFieldName(Iterator<Path.Node> nodes) {
        Path.Node last = null;
        while (nodes.hasNext()) {
            last = nodes.next();
        }
        return last.getName();
    }
	
}
