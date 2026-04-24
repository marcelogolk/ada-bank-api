package br.com.ada.quarkus.exception;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UnauthorizedExceptionMapper
        implements ExceptionMapper<NotAuthorizedException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(NotAuthorizedException exception) {

        ErrorResponse error = new ErrorResponse(
                Response.Status.UNAUTHORIZED.getStatusCode(),
                Response.Status.UNAUTHORIZED.getReasonPhrase(),
                exception.getMessage(),
                uriInfo.getPath()
        );

        return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(error)
                .build();
    }
}