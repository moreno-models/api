package net.stepniak.morenomodels.serviceserverless.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionHandler implements ExceptionMapper<NotFoundException> {
    @Override
    public Response toResponse(NotFoundException exception) {
        return Response
                .status(404)
                .entity(Error.builder()
                        .statusCode(404)
                        .message(exception.getMessage())
                        .build()
                ).build();
    }
}
