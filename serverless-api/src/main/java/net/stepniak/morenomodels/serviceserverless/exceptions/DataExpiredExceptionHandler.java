package net.stepniak.morenomodels.serviceserverless.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DataExpiredExceptionHandler implements ExceptionMapper<DataExpiredException> {
    @Override
    public Response toResponse(DataExpiredException exception) {
        return Response
                .status(409)
                .entity(Error.builder()
                        .statusCode(409)
                        .message(exception.getMessage())
                        .build()
                ).build();
    }
}
