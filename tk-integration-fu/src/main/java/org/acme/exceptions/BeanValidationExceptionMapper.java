package org.acme.exceptions;

import static javax.ws.rs.core.Response.status;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
@Provider
public class BeanValidationExceptionMapper implements ExceptionMapper<ValidationException> {
	
    static final Logger LOG = Logger.getLogger(BeanValidationExceptionMapper.class);
	
	@Override
	public Response toResponse(ValidationException exception) {
		if (exception instanceof ConstraintViolationException) {
			return manageConstraintViolations((ConstraintViolationException) exception);
		}
		if (exception instanceof InvalidJWTException) {
			return status(Response.Status.UNAUTHORIZED).entity(exception.getMessage()).build();
		}
		if (exception instanceof ProcessIdNotFoundException) {
			return status(Response.Status.NOT_FOUND).entity(exception.getMessage()).build();
		}
			
		LOG.fatal("Internal Server Error. "+exception.getMessage() );
		
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
